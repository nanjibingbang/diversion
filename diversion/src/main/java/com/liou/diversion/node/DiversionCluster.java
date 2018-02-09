package com.liou.diversion.node;

import com.alibaba.fastjson.JSONObject;
import com.liou.diversion.container.Config;
import com.liou.diversion.container.Destroyable;
import com.liou.diversion.transport.ChannelFactory;
import com.liou.diversion.transport.IoChannel;
import com.liou.diversion.zk.ChildrenChangeHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * TODO 添加收敛分析及控制
 *
 * @author liou
 */
public class DiversionCluster implements ChildrenChangeHandler, Destroyable {
    private static Logger logger = LoggerFactory.getLogger(DiversionCluster.class);

    @Config("listenport")
    private int listenPort;
    private final String localHost;
    private DiversionNode localNode;

    @Config("replictions")
    private int replictions;
    private Set<DiversionNode> nodeSet;
    private TreeMap<Integer, VirtualNode> circle;
    private Lock circleLock;

    private ChannelFactory channelFactory;

    @Config("zookeeper.root")
    private String regRoot;

    public DiversionCluster() throws SocketException {
        this.localHost = getLocalHost();
        this.nodeSet = new HashSet<>();
        this.circle = new TreeMap<>();
        this.circleLock = new ReentrantLock();
    }

    @Override
    public void build(List<String> nodes) throws Exception {
        /**
         * 开放接入端口
         */
        try {
            channelFactory.acceptOn(listenPort, this);
        } catch (Exception e) {
            throw new SocketException(String.format("fail to listen on %d", listenPort));
        }
        logger.info("开放接入端口{}", listenPort);

        // 添加所有节点
        if (nodes != null && nodes.size() > 0) {
            nodes.forEach(node -> addNode(new DiversionNode(node)));
        }
        // add local
        localNode = new DiversionNode(String.format("%s->%s", localHost, listenPort));
        addNode(localNode);
    }

    /**
     * 排除IPv6地址
     */
    private static String getLocalHost() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                continue;
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if ((addr instanceof Inet6Address) || addr.isLoopbackAddress()) {
                    continue;
                }
                return addr.getHostAddress();
            }
        }
        throw new SocketException("获取不到本地IP地址，确认拥有IPv4网卡, 所有网卡: " + interfaces);
    }

    public boolean isLocalNode(DiversionNode diversionNode) {
        return diversionNode == localNode;
    }

    /**
     * 添加节点到节点集 同名节点不能重复添加
     *
     * @param node
     * @return
     */
    private boolean addNode(DiversionNode node) {
        circleLock.lock();
        try {
            Iterator<DiversionNode> it = nodeSet.iterator();
            while (it.hasNext()) {
                if (it.next().equals(node)) {
                    return false;
                }
            }
            nodeSet.add(node);
            int hash = node.hashCode();
            circle.put(hash, node);
            for (int i = 1; i < replictions; i++) {
                VirtualNode virtualNode = new VirtualNode(String.format("%s_V%d", node.getKey(), i), node);
                circle.put(virtualNode.hashCode(), virtualNode);
            }
        } finally {
            circleLock.unlock();
        }
        logger.info("添加节点{}", node);
        return true;
    }

    public DiversionNode getNode(String sign) {
        List<DiversionNode> collect = nodeSet.stream().filter(node -> node.getKey().equals(sign))
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            return collect.get(0);
        }
        return null;
    }

    /**
     * 移除节点及其虚拟节点
     *
     * @param node
     * @param close 是否关闭channel
     */
    private void removeNode(DiversionNode node, boolean close) {
        circleLock.lock();
        try {
            if (close) {
                node.closeChannel();
            }
            Iterator<Entry<Integer, VirtualNode>> it = circle.entrySet().iterator();
            while (it.hasNext()) {
                VirtualNode virtualNode = it.next().getValue().getActual();
                if (virtualNode.equals(node)) {
                    it.remove();
                }
            }
            nodeSet.remove(node);
        } finally {
            circleLock.unlock();
        }
        logger.warn("移除 {}", node);
    }

    /**
     * 通过一致性hash 获取执行节点<br>
     * 当选择的节点不可达时 将会从节点集移除<br>
     * 因为包含本地节点 所以总会选择到一个可用节点
     *
     * @param hash
     * @return
     */
    public DiversionNode select(int hash) {
        DiversionNode actual = null;
        int key = hash;
        circleLock.lock();
        try {
            SortedMap<Integer, VirtualNode> tailMap = circle.tailMap(key);
            while (true) {
                if (tailMap.isEmpty()) {
                    actual = circle.firstEntry().getValue().getActual();
                } else {
                    actual = tailMap.get(tailMap.firstKey()).getActual();
                }
                try {
                    if (!isReady(actual)) {
                        connectNode(actual);
                    }
                    return actual;
                } catch (IOException e) {
                    logger.error("fail connect to {}", actual, e);
                    removeNode(actual, false);
                    tailMap = circle.tailMap(key);
                }
            }
        } finally {
            circleLock.unlock();
        }
    }

    /**
     * @param node
     * @return true 是本地节点或者节点连接就绪
     */
    public boolean isReady(DiversionNode node) {
        return node != null && (isLocalNode(node) || node.isReady());
    }

    /**
     * 不可达节点处理
     * @param diversionNode
     * @param close 是否主动关闭
     */
    public void nodeUnreachable(DiversionNode diversionNode, boolean close) {
        removeNode(diversionNode, close);
    }

    private void connectNode(DiversionNode diversionNode) throws IOException {
        String nodeSign = diversionNode.getKey();
        int index = nodeSign.indexOf("->");
        String host = nodeSign.substring(0, index);
        String portStr = nodeSign.substring(index + 2);
        int remotePort = Integer.valueOf(portStr);
        IoChannel channel = channelFactory.createChannel(host, remotePort, null);
        channel.addAttribute("cluster", this);
        channel.fireInited();
        diversionNode.channel(channel);
    }

    /**
     * 节点重新连接并添加到节点集
     *
     * @param diversionNode
     * @return
     * @throws IOException
     */
    public void nodeReconnect(DiversionNode diversionNode) throws IOException {
        IoChannel ioChannel = channelFactory.reconnect(diversionNode.channel(), null);
        diversionNode.channel(ioChannel);
        if (!addNode(diversionNode)) { // 节点集已包含只是替换io channel
            // 在channel记录cluster属性
            ioChannel.addAttribute("cluster", this);
        }
        ioChannel.fireInited();
    }

    public void setChannelFactory(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public String getLocalNodeString() {
        return localNode.getKey();
    }

    /**
     * 节点集信息
     *
     * @return
     */
    public String clusterInfo() {
        final JSONObject jo = new JSONObject();
        nodeSet.forEach(node -> jo.put(node.getKey(), isReady(node)
                ? "ready" : "not ready"));
        return jo.toString();
    }

    @Override
    public void destroy() {
        circleLock.lock();
        try {
            nodeSet.forEach(diversionNode -> {
                if (!isLocalNode(diversionNode) && diversionNode.isReady()) {
                    diversionNode.closeChannel();
                }
            });
        } finally {
            circleLock.unlock();
        }
        // 确保在netty关闭前所有对端都能接收到channel断开信息所以channelFactory在此关闭
        channelFactory.shutdown();
    }

    @Override
    public String getPath() {
        return regRoot;
    }

    @Override
    public void handleChildRemoved(String child) {
        circleLock.lock();
        try {
            List<DiversionNode> collect = nodeSet.stream()
                    .filter(node -> node.getKey().equals(child)).collect(Collectors.toList());
            nodeSet.removeAll(collect);
        } finally {
            circleLock.unlock();
        }
    }

    @Override
    public void handleChildAdded(String child) {
        if (StringUtils.isNotBlank(child)) {
            if (!addNode(new DiversionNode(child))) {
                logger.warn("node exist {}", child);
            }
        }
    }
}
