package com.liou.diversion.node;

import com.liou.diversion.container.Config;
import com.liou.diversion.container.Destroyable;
import com.liou.diversion.container.Initialization;
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

/**
 * TODO 添加收敛分析及控制
 *
 * @author liou
 */
public class DiversionCluster implements ChildrenChangeHandler, AccessService, Initialization, Destroyable {
    private static Logger logger = LoggerFactory.getLogger(DiversionCluster.class);

    @Config("diversion.nodes")
    private String nodes;
    @Config("diversion.listenport")
    private int listenPort;
    private final String localHost;
    private DiversionNode localNode;

    @Config("diversion.replictions")
    private int replictions;
    private Set<DiversionNode> nodeSet;
    private TreeMap<Integer, VirtualNode> circle;
    private Lock circleLock;

    private ChannelFactory channelFactory;

    @Config("diversion.zookeeper.root")
    private String regRoot;

    public DiversionCluster() throws SocketException {
        this.localHost = getLocalHost();
        this.nodeSet = new HashSet<>();
        this.circle = new TreeMap<>();
        this.circleLock = new ReentrantLock();
    }

    @Override
    public boolean access(DiversionNode diversionNode) {
        return addOrClose(diversionNode);
    }

    @Override
    public void init() throws Exception {
        /**
         * 开放接入端口
         */
        try {
            channelFactory.acceptOn(listenPort, this);
        } catch (Exception e) {
            throw new SocketException(String.format("fail to listen on %d", listenPort));
        }
        logger.debug("开放接入端口{}", listenPort);

        if (StringUtils.isNotBlank(nodes)) {
            createAndConnectNodes(nodes);
        }
        // add local
        localNode = new DiversionNode(localHost, listenPort);
        addNode(localNode);
//        if (nodeSet.size() == 1) {
//            logger.warn("当前节点集中只包含本地节点!");
//        }
    }

    /**
     * 创建nodes并连接
     *
     * @param nodes
     */
    private void createAndConnectNodes(String nodes) {
        // InetSocketAddress localAddress = new InetSocketAddress(localPort);
        String[] array = nodes.split(";");
        for (String node : array) {
            if (node != null) {
                try {
                    doCreateAndConnect(node);
                } catch (IOException e) {
                    logger.error("节点未连接:{}", node, e);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException(String.format("正确配置nodes:%s", nodes), e);
                }
            }
        }
    }

    private void doCreateAndConnect(String node) throws IOException {
        int index = node.indexOf("->");
        String host = node.substring(0, index);
        String portStr = node.substring(index + 2);
        int remotePort = Integer.valueOf(portStr);
        DiversionNode diversionNode = new DiversionNode(host, remotePort);
        IoChannel channel = channelFactory.createChannel(host, remotePort, null);
        diversionNode.channel(channel);
        addOrClose(diversionNode);
        channel.fireInited();
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
            ioChannel.addAttribute("cluster", this);
        }
        ioChannel.fireInited();
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
        throw new SocketException("Can't get local ip address, interfaces are: " + interfaces);
    }

    /**
     * 添加到cluster，添加失败则关闭node的channel
     *
     * @param diversionNode
     * @return
     */
    public boolean addOrClose(DiversionNode diversionNode) {
        boolean added = addNode(diversionNode);
        if (!added) {
            diversionNode.closeChannel();
        }
        return added;
    }

    /**
     * 添加节点到节点集 同名节点不能重复添加
     *
     * @param node
     * @return
     */
    public boolean addNode(DiversionNode node) {
        circleLock.lock();
        try {
            Iterator<DiversionNode> it = nodeSet.iterator();
            while (it.hasNext()) {
                if (it.next().equals(node)) {
                    return false;
                }
            }
            if (!isLocalNode(node)) {
                node.channel().addAttribute("cluster", this);
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
        Iterator<DiversionNode> it = nodeSet.iterator();
        while (it.hasNext()) {
            DiversionNode next = it.next();
            if (next.getKey().equals(sign)) {
                return next;
            }
        }
        return null;
    }

    /**
     * 移除节点及其虚拟节点
     *
     * @param node
     * @param close 是否关闭channel
     */
    public void removeNode(DiversionNode node, boolean close) {
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
     * 移除并关闭node
     *
     * @param node
     */
    public void removeNode(DiversionNode node) {
        removeNode(node, true);
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
                if (isReady(actual)) {
                    break;
                } else {
                    logger.error("非有效连接节点{}", actual);
                    removeNode(actual, false);
                    tailMap = circle.tailMap(key);
                }
            }
        } finally {
            circleLock.unlock();
        }
        return actual;
    }

    public boolean isLocalNode(DiversionNode diversionNode) {
        return diversionNode == localNode;
    }

    /**
     * @param node
     * @return true 是本地节点或者节点连接就绪
     */
    public boolean isReady(DiversionNode node) {
        return node != null && (isLocalNode(node) || node.isReady());
    }

    public void setChannelFactory(ChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
    }

    public Set<DiversionNode> getNodeSet() {
        return nodeSet;
    }

    public String getLocalNodeString() {
        return localNode.getKey();
    }

    @Override
    public void destroy() {
        Iterator<DiversionNode> it = nodeSet.iterator();
        while (it.hasNext()) {
            DiversionNode diversionNode = it.next();
            if (!isLocalNode(diversionNode) && diversionNode.isReady()) {
                diversionNode.closeChannel();
            }
        }
        channelFactory.shutdown();
    }

    @Override
    public String getPath() {
        return regRoot;
    }

    @Override
    public void handleChildRemoved(String child) {
        int index = child.indexOf('_');
        if (index > 0) {
            String nodeName = child.substring(0, index);
            DiversionNode node = getNode(nodeName);
            if (node != null) {
                removeNode(node);
            }
        }
    }

    @Override
    public void handleChildAdded(String child) {
        if (StringUtils.isNotBlank(child)) {
            try {
                doCreateAndConnect(child);
            } catch (Exception e) {
                logger.error("节点未连接:{}", child, e);
            }
        }
    }
}
