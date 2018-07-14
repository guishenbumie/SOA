package log;

import org.slf4j.LoggerFactory;

/**
 * @author sunfengmao
 * @Date 2018/5/27
 * jdk自带的log太难用了，使用slf4j管理日志，底层用的logback
 */
public class Logger {

    public final org.slf4j.Logger slf4j;

    private Logger(Class<?> classname){
        slf4j = LoggerFactory.getLogger(classname);
    }

    private Logger(String name){
        slf4j = LoggerFactory.getLogger(name);
    }

    public static Logger getLogger(Class<?> classname){
        return new Logger(classname);
    }

    public static Logger getLogger(String name){
        return new Logger(name);
    }

    public void trace(String msg, Object... objects){
        if(!slf4j.isTraceEnabled()){
            return;
        }
        slf4j.trace(msg, objects);
    }

    public void debug(String msg, Object... objects){
        if(!slf4j.isDebugEnabled()){
            return;
        }
        slf4j.debug(msg, objects);
    }

    public void info(String msg, Object... objects){
        if(!slf4j.isInfoEnabled()){
            return;
        }
        slf4j.info(msg, objects);
    }

    public void warn(String msg, Object... objects){
        if(!slf4j.isWarnEnabled()){
            return;
        }
        slf4j.warn(msg, objects);
    }

    public void error(String msg, Object... objects){
        if(!slf4j.isErrorEnabled()){
            return;
        }
        slf4j.error(msg, objects);
    }

    public static void main() throws Exception{
        Logger logger = Logger.getLogger(Logger.class);
        int i = 10;
        logger.debug("我是测试{}哈哈", i);
    }

}
