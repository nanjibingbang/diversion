package com.liou.diversion.container;

import java.util.HashMap;
import java.util.Map;

import com.liou.diversion.transport.Charset;

public class DiversionConfig {

    public enum Configs {
        /**
         * 当前节点名称
         */
        NAME("diversion.localname", "", String.class),
        /**
         * 节点集合（name_ip[:port]）
         */
        NODES("diversion.nodes", "", String.class),
        /**
         * 监听端口 默认33585
         */
        LISTEN_PORT("diversion.listenport", 33585, Integer.class),
        /**
         * 连接端口 采用随机分配
         */
        LOCAL_PORT("diversion.localport", 7183, Integer.class),
        /**
         * 统一传输编码
         */
        CHARSET("diversion.io.charset", Charset.UTF8, Charset.class),
        /**
         * 虚拟节点因子 默认160
         */
        VIRTUAL_REPLICTIONS("diversion.replictions", 160, Integer.class),
        /**
         * 连接重试次数
         */
        CONNECT_ATTEMPTS("diversion.io.attempts", 3, Integer.class),
        /**
         * io线程数
         */
        IO_THREAD_COUNT("diversion.io.threadcount", 8, Integer.class),
        /**
         * 连接最大读闲置时间(毫秒) 超时断开
         */
        READER_IDLE_TIME("diversion.io.readidle", 5000, Integer.class),
        /**
         * 连接最大写闲置时间(毫秒) 超时发送心跳包
         */
        WRITER_IDLE_TIME("diversion.io.writidle", 4000, Integer.class),
        /**
         * 缓存暂留时间(毫秒)
         */
        TRANSIENT_TIME("diversion.cache.transienttime", 60000, Integer.class),
        /**
         * 请求超时设置(毫秒)
         */
        REQUEST_TIMEOUT("diversion.request.timeout", 3000, Integer.class),
        /**
         * zk根节点名称
         */
        REG_ROOT("diversion.zookeeper.root", "/diversion", String.class),
        /**
         * zk服务列表
         */
        ZOOKEEPER_SERVERS("diversion.zookeeper.servers", "", String.class),
        /**
         * zk session超时
         */
        ZOOKEEPER_SESSION_TIMEOUT("diversion.zookeeper.sessiontimeout", 300000, Integer.class),
        /**
         * zk 连接超时
         */
        ZOOKEEPER_CONNECTION_TIMEOUT("diversion.zookeeper.connecttimeout", 3000, Integer.class),
        /**
         * zk重连最大重试次数
         */
        ZOOKEEPER_MAX_ATTEMPTS("diversion.zookeeper.attempts", 1000, Integer.class);

        private String sign;
        private Object defValue;
        private Class<?> clazz;

        private Configs(String sign, Object defValue, Class<?> clazz) {
            if (defValue == null) {
                throw new NullPointerException("defValue");
            }
            this.sign = sign;
            this.defValue = defValue;
            this.clazz = clazz;
        }

        /**
         * 配置标识
         * @return
         */
        public String sign() {
            return sign;
        }

        public Object defValue() {
            return defValue;
        }

        public Class<?> clazz() {
            return clazz;
        }

        public static Configs formSign(String sign) {
            Configs[] values = Configs.values();
            for (Configs config : values) {
                if (config.sign.equals(sign)) {
                    return config;
                }
            }
            return null;
        }
    }

    public static final DiversionConfig INSTANCE = new DiversionConfig();

    private Map<Configs, Object> configMap = new HashMap<>();

    private DiversionConfig() {
    }

    /**
     * 保存配置
     * 
     * @param config
     * @param value
     *            必须是确定类型
     */
    public void addConfig(Configs config, Object value) {
        configMap.put(config, value != null ? value : config.defValue());
    }

    public Object getConfig(Configs config) {
        Object result = configMap.get(config);
        return result == null ? config.defValue() : result;
    }

    public Object getConfig(String configName) {
        Configs config = Configs.formSign(configName);
        if (config != null) {
            Object result = configMap.get(config);
            return result == null ? config.defValue() : result;
        }
        return null;
    }

}
