package game;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import db.JedisConfig;
import db.JedisManager;
import log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sunfengmao
 * @Date 2018/6/7
 */
public class ConfigManager {

    private static final int GATE_PORT = 8900;
    public static int rgameserver_port = 8920;

    public static final XStream xStream = new XStream(new DomDriver());

    private final Map<String, Properties> properties = new ConcurrentHashMap<>();//系统配置，基本都是起服时使用
    private final Map<String, Object> xmlbeans = new ConcurrentHashMap<>();//xml的配置
    private final Map<String, String> simbeans = new ConcurrentHashMap<>();//一些简单的全局变量，支持MXBean更改
    private final Map<Integer, IModule> modules = new ConcurrentHashMap<>();//所有模块，key=模块的类型，value=模块对象实例

    //配置的信息 begin
    public final static String PROPERTIES_PATH = "conf/properties";
    public final static String XML_PATH = "conf/xml";
    public final static String RELOAD_FILE = XML_PATH + "/game.ReloadConfig.xml";
    //配置的信息 end

    private Logger logger = Logger.getLogger(ConfigManager.class);

    private static ConfigManager instance = new ConfigManager();

    private ConfigManager(){}

    public static ConfigManager getInstance(){
        return instance;
    }

    public void init() {
        //加载起服传进来的参数
        String[] args = RGameServer.args;
        if(null != args && args.length > 0){
            rgameserver_port = Integer.valueOf(args[0]);
        }

        //加载properties配置文件
        loadProperties();

        //加载xml配置文件
        loadXml();

        //加载简单全局变量
        simbeans.put("server.open.date", getPropConfValue("simple", "server.open.date"));

        //协议初始化
        RControlor.getInstance().loadProtocols();

        //加载jedis配置
        JedisConfig.initJedisConfig();
        //连接redis
        JedisManager.start();

        //线程管理开启
        int procedurePoolSize = getPropConfInt("sys", "procedurePoolSize");
        int scheduledSize = getPropConfInt("sys", "scheduledSize");
        RExecutor.start(procedurePoolSize, scheduledSize);

        //加载服务器中各种逻辑模块
        loadModules();
    }

    private void loadProperties(){
        File[] propFiles = new File(PROPERTIES_PATH).listFiles();
        if(null == propFiles || propFiles.length <= 0){
            throw new NullPointerException(PROPERTIES_PATH + " can't load files!");
        }
        Arrays.stream(propFiles)
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
    }

    private void loadXml(){
        File[] xmlFiles = new File(XML_PATH).listFiles();
        if(null == xmlFiles || xmlFiles.length <= 0){
            throw new NullPointerException(XML_PATH + " can't load files!");
        }
        //后面注掉的两行，是为了将XStream框架设置为安全模式，不再报红色警告
//        XStream.setupDefaultSecurity(xStream);
//        xStream.allowTypes(new Class[]{game.SRoleBaseConfig.class});
        Arrays.stream(xmlFiles)
                .filter(p->p.getName().endsWith(".xml"))
                .forEach(f->{
                    Object o = null;
                    try (FileInputStream fis = new FileInputStream(f)) {
                        o = xStream.fromXML(fis);
                    } catch (Exception e) {
                        logger.error("{} load error, {}", f.getAbsolutePath(), e);
                        return;
                    }
                    if(null != o && o instanceof TreeMap){
                        String name = f.getName().substring(0, f.getName().indexOf(".xml"));
                        xmlbeans.put(name, o);
                        logger.info("register bean={}", name);
                    }
                });
    }

    private void loadModules(){
        //TODO 会不会有模块间依赖的问题呢
        Arrays.stream(EModuleType.values()).forEach(e->{
            Object o = null;
            try {
                o = Class.forName(e.getClzName()).newInstance();
            } catch (Exception ex) {
                logger.error("加载模块{}失败，{}", e.getClzName(), ex.getMessage());
                return;
            }
            if(!(o instanceof IModule)){
                logger.error("模块{}没有实现IModule接口！", e.getClzName());
                return;
            }
            IModule module = (IModule) o;
            try {
                module.init();
                modules.put(e.getType(), module);
            } catch (Exception ex){
                logger.error("模块{}执行初始化失败！{}", e.getClzName(), ex.getMessage());
            }
        });
    }

    public void exit() {
        //关闭线程
        RExecutor.stop();

        //关闭jedis
        JedisManager.stop();

        //各模块的官服操作
        Arrays.stream(EModuleType.values()).forEach(e->{
            Object o = null;
            try {
                o = Class.forName(e.getClzName()).newInstance();
            } catch (Exception ex) {
                logger.error("关闭模块{}失败，{}", e.getClzName(), ex.getMessage());
                return;
            }
            if(!(o instanceof IModule)){
                logger.error("模块{}没有实现IModule接口！", e.getClzName());
                return;
            }
            IModule module = (IModule) o;
            try {
                module.exit();
            } catch (Exception ex){
                logger.error("模块{}执行关闭失败！{}", e.getClzName(), ex.getMessage());
            }
        });
    }

    /**
     * 加载热加载的配置表
     * @return
     */
    public ReloadResult reload() {
        //加载热加载表的配置
        File file = new File(RELOAD_FILE);
        if(!file.exists()){
            return new ReloadResult(false, "no reload xml!\n");
        }
        Object object = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            object = xStream.fromXML(fis);
        } catch (Exception e) {
            logger.error("{} load error, {}", file.getAbsolutePath(), e);
            return new ReloadResult(false, "load reload xml error!\n");
        }
        if(null == object || object instanceof TreeMap){
            logger.error("bean of reload xml is error!");
            return new ReloadResult(false, "bean of reload xml is error!\n");
        }
        String name = file.getName().substring(0, file.getName().indexOf(".xml"));
        xmlbeans.put(name, object);
        ReloadResult reloadResult = new ReloadResult(true, "load reload xml success.\n");

        //重新加载properties和xml
        TreeMap<Integer, ReloadConfig> confs = (TreeMap<Integer, ReloadConfig>) object;
        confs.values().forEach(c -> {
            if(c.getType() == EReloadType.RELOAD_PROPERTIES.getType()){
                File rf = new File(PROPERTIES_PATH + "/" + c.getName());
                if(!rf.exists()){
                    reloadResult.fail();
                    reloadResult.appendMsg("no properties " + rf.getName() + "\n");
                    logger.error("no properties {}!", rf.getName());
                    return;
                }
                try (FileInputStream rfis = new FileInputStream(rf)) {
                    Properties rp = new Properties();
                    String rname = rf.getName().substring(0, rf.getName().indexOf(".properties"));
                    properties.put(rname, rp);
                    reloadResult.appendMsg("reload properties " + rname + " success.\n");
                } catch (Exception e) {
                    reloadResult.fail();
                    reloadResult.appendMsg("reload properties " + name + " fail!\n");
                    logger.error("reload properties {} fail! {}", rf.getName(), e);
                    return;
                }
            }else if(c.getType() == EReloadType.RELOAD_XML.getType()){
                File rf = new File(XML_PATH + "/" + c.getName());
                if(!rf.exists()){
                    reloadResult.fail();
                    reloadResult.appendMsg("no xml " + rf.getName() + "\n");
                    logger.error("no xml {}!", rf.getName());
                    return;
                }
                try (FileInputStream rfis = new FileInputStream(rf)) {
                    Object ro = xStream.fromXML(rfis);
                    String rname = rf.getName().substring(0, rf.getName().indexOf(".xml"));
                    xmlbeans.put(rname, ro);
                    reloadResult.appendMsg("reload xml " + rname + " success.\n");
                } catch (Exception e) {
                    reloadResult.fail();
                    reloadResult.appendMsg("reload xml " + name + " fail!\n");
                    logger.error("reload xml {} fail! {}", rf.getName(), e);
                }
            }
        });

        //再执行模块的热加载
        confs.values().stream()
                .filter(c -> c.getType() == EReloadType.RELOAD_MODULE.getType() && modules.containsKey(c.getModuleType()))
                .map(m -> modules.get(m.getModuleType()))
                .forEach(p -> {
                    try {
                        p.reload();
                    } catch (Exception e) {
                        reloadResult.fail();
                        reloadResult.appendMsg("reload module " + p.getETpye().getClzName() + " fail!\n");
                        logger.error("reload module {} fail! {}", p.getETpye().getClzName(), e);
                    }
                });
        return reloadResult;
    }

    public Properties getPropConf(String name){
        return properties.get(name);
    }

    /**
     * 获得指定的properties文件的指定key的value
     * @param name
     * @param key
     * @return
     */
    public String getPropConfValue(String name, String key){
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
    public int getPropConfInt(String name, String key){
        String value = getPropConfValue(name, key);
        return Integer.valueOf(value);
    }

    /**
     * 获得指定的properties文件的指定key的value值的long值
     * @param name
     * @param key
     * @return
     */
    public long getPropConfLong(String name, String key){
        String value = getPropConfValue(name, key);
        return Long.valueOf(value);
    }

    /**
     * 获得指定的properties文件的指定key的value值=true or false
     * @param name
     * @param key
     * @return
     */
    public boolean isPropConf(String name, String key){
        String value = getPropConfValue(name, key).trim();
        return value.equals("true");
    }

    /**
     * 获得相关的xml配置文件的数据
     * @param t
     * @param <T>
     * @return
     */
    public <T> TreeMap<Integer, T> getConf(Class<T> t){
        Object o = xmlbeans.get(t.getName());
        if(null == o){
            return null;
        }
        try{
            @SuppressWarnings("unchecked")
            TreeMap<Integer, T> map = (TreeMap<Integer, T>) o;
            return map;
        }catch(ClassCastException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得配置表中指定id的配置
     * @param t
     * @param confId
     * @param <T>
     * @return
     */
    public <T> T getConfById(Class<T> t, int confId){
        Map<Integer, T> confs = getConf(t);
        if(null == confs || confs.isEmpty()){
            return null;
        }
        return confs.get(confId);
    }

    public Map<String, String> getSimbeans() {
        return simbeans;
    }

    public String getSimbean(String key){
        return simbeans.get(key);
    }

}
