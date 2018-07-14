package db;

import game.ConfigManager;
import log.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author sunfengmao
 * @Date 2018/6/8
 */
public class JedisManager {

    private static Object lock = new Object();
    private static Logger logger = Logger.getLogger(JedisManager.class);

    //TODO redis的容灾机制，主从服务器或者分布式的分片存储，思考一下
    private JedisPool pool;

    private static JedisManager instance;

    private JedisManager(JedisPoolConfig poolConfig, String host, int port){
        pool = new JedisPool(poolConfig, host, port);
    }

    public static JedisManager getInstance(){
        return instance;
    }

    /**
     * 初始化jedis并连接redis
     */
    public static void start(){
        synchronized (lock) {
            JedisConfig jedisConfig = JedisConfig.getInstance();
            if(null == jedisConfig){
                throw new NullPointerException("jedis的配置没有加载");
            }
            if(null == instance){
                instance = new JedisManager(jedisConfig.getPoolConfig(),
                        jedisConfig.getHost(), jedisConfig.getPort());
            }

            //检验一下是否连接redis成功
            //不过jedis怎么没有现成的接口检验是否连接成功呢，不是很优雅呀
            if(ConfigManager.getInstance().isPropConf("sys", "redis.check")){
                Jedis jedis = getJedis();
                jedis.set("test", "1");
                jedis.expire("test", 60);//60秒后该键自动删除
                if(!"1".equals(jedis.get("test"))){
                    throw new RuntimeException("connect redis fail!");
                }
                jedis.close();
            }

            logger.info("加载jedis，连接redis的host={}，port={}；Jedis的maxTotal={}，maxIdle={}，maxWait={}",
                    jedisConfig.getHost(), jedisConfig.getPort(),
                    jedisConfig.getPoolConfig().getMaxTotal(),
                    jedisConfig.getPoolConfig().getMaxIdle(),
                    jedisConfig.getPoolConfig().getMaxWaitMillis());

        }
    }

    /**
     * 关闭和redis的连接
     */
    public static void stop(){
        logger.info("开始关闭与redis的连接……");
        synchronized (lock) {
            if(null != instance){
                instance.pool.close();
//                instance.jedis.shutdown();//TODO 这个方法只是关闭连接还是将redis也关掉
            }
        }
        logger.info("关闭与redis的连接完成");
    }

    /**
     * 获得一个jedis的控制
     * @return
     */
    public static Jedis getJedis(){
        return instance.pool.getResource();
    }

}
