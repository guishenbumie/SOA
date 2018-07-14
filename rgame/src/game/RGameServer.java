package game;

import log.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author sunfengmao
 * @Date 2018/5/26
 */
public class RGameServer {

    private static final Logger logger = Logger.getLogger(RGameServer.class);

    public static volatile boolean isOpen = false;

    public static String[] args;

    public static void main(String[] args) throws Exception {
        logger.info("RGame Server starting……");

        RGameServer.args = args;

        //注册MXBean
        RControlor controlor = RControlor.getInstance();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeanServer.registerMBean(controlor, new ObjectName("RBean:name=controlor"));

        //初始化
        ConfigManager.getInstance().init();

        //开启网络
        try {
            controlor.open();

            logger.info("RGame Server shutdown");

            //关服操作
            ConfigManager.getInstance().exit();

            logger.info("shutdown over");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(1);
        }

    }

}
