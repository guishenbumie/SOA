package db;

import game.ConfigManager;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author sunfengmao
 * @Date 2018/6/9
 */
public class JedisConfig {

    private static Object lock = new Object();
    private static JedisConfig instance;

    private final JedisPoolConfig poolConfig;
    private final String host;
    private final int port;

    private JedisConfig(int maxTotal, int maxIdle, long maxWait, String host, int port){
        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        this.host = host;
        this.port = port;
    }

    public static JedisConfig getInstance(){
        return instance;
    }

    /**
     * 加载jedis和redis的配置
     */
    public static void initJedisConfig(){
        synchronized (lock) {
            if(null == instance){
                int maxTotal = ConfigManager.getInstance().getPropConfInt("sys", "jedis.pool.maxTotal");
                int maxIdle = ConfigManager.getInstance().getPropConfInt("sys", "jedis.pool.maxIdle");
                long maxWait = ConfigManager.getInstance().getPropConfLong("sys", "jedis.pool.maxWait");
                String host = ConfigManager.getInstance().getPropConfValue("sys", "redis.host");
                int port = ConfigManager.getInstance().getPropConfInt("sys", "redis.port");
                instance = new JedisConfig(maxTotal, maxIdle, maxWait, host, port);
            }
        }
    }

    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
