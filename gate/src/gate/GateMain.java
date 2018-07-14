package gate;

import client.AcceptClientTask;
import game.ConnectGameTask;
import log.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 */
public class GateMain {

    private static Logger logger = Logger.getLogger(GateMain.class);

    public static volatile boolean isOpen = true;
    //TODO gate应该支持增加动态增加连接，另外这块的并发明显不对，先占个位置，以后再细写
    public static AtomicInteger gateNum = new AtomicInteger(0);

    public final static String PROPERTIES_PATH = "conf/properties";
    private static final Map<String, Properties> properties = new ConcurrentHashMap<>();//系统配置，基本都是起服时使用

    public static void main(String[] args) throws Exception {
        //加载MXBean
        Controlor controlor = new Controlor();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(controlor, new ObjectName("Gate:name=controlor"));

        //加载配置
        File[] files = new File(PROPERTIES_PATH).listFiles();
        if(null == files || files.length <= 0){
            throw new NullPointerException(PROPERTIES_PATH + " can't load files!");
        }
        Arrays.stream(files)
                .filter(p->p.getName().endsWith(".properties"))
                .forEach(f->{
                    Properties prop = new Properties();
                    try (FileInputStream fis = new FileInputStream(f)) {
                        prop.load(fis);
                    } catch (FileNotFoundException e){
                        logger.error("{} not found, {}", f.getAbsolutePath(), e);
                        return;
                    } catch (IOException e){
                        logger.error("{} load error, {}", f.getAbsolutePath(), e);
                        return;
                    }
                    String name = f.getName().substring(0, f.getName().indexOf(".properties"));
                    properties.put(name, prop);
                    logger.info("register properties={}", name);
                });


        Executor.start(getPropConfInt("sys", "pool.size"));
        Executor.getInstance().submit(new ConnectGameTask());//连接游戏服务器
        Executor.getInstance().submit(new AcceptClientTask());//接受客户端的连接

    }

    public static Properties getPropConf(String name){
        return properties.get(name);
    }

    /**
     * 获得指定的properties文件的指定key的value
     * @param name
     * @param key
     * @return
     */
    public static String getPropConfValue(String name, String key){
        Properties properties = getPropConf(name);
        if(null == properties){
            throw new NullPointerException("没有指定的配置properties文件name=" + name);
        }
        String value = properties.getProperty(key);
        if(null == value){
            throw new NullPointerException("配置文件" + name + ".properties没有指定的key=" + key);
        }
        return value;
    }

    /**
     * 获得指定的properties文件的指定key的value值的int值
     * @param name
     * @param key
     * @return
     */
    public static int getPropConfInt(String name, String key){
        String value = getPropConfValue(name, key);
        return Integer.valueOf(value);
    }

}
