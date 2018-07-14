package game;

import log.Logger;
import proto.RProtocol;
import java.util.concurrent.*;

/**
 * @author sunfengmao
 * @Date 2018/5/31
 * 这里只对RGameServer的线程进行了最简单的管理，还有很多优化空间
 */
public class RExecutor {

    private static Object lock = new Object();
    private static Logger logger = Logger.getLogger(RExecutor.class);

    private static RExecutor instance;

    private final ExecutorService protocols;
    private final ExecutorService procedures;
    private final ScheduledExecutorService scheduleds;

    private RExecutor(int procedurePoolSize, int scheduledSize){
        protocols = Executors.newCachedThreadPool();
        procedures = Executors.newFixedThreadPool(procedurePoolSize);
        scheduleds = Executors.newScheduledThreadPool(scheduledSize);
    }

    public static RExecutor getInstance(){
        return instance;
    }

    /**
     * 开启线程管理器
     * @param procedurePoolSize
     * @param scheduledSize
     */
    public static void start(int procedurePoolSize, int scheduledSize){
        logger.info("线程管理器开始初始化，指定protocol线程池{}个，指定scheduled线程池{}个", procedurePoolSize, scheduledSize);
        synchronized (lock) {
            if(null == instance){
                instance = new RExecutor(procedurePoolSize, scheduledSize);
            }
        }
        logger.info("线程管理器初始化完成");
    }

    /**
     * 关闭线程管理器
     */
    public static void stop(){
        logger.info("线程管理器开始关闭……");
        synchronized (lock) {
            if(null != instance){
                instance.protocols.shutdown();
                instance.procedures.shutdown();
                instance.scheduleds.shutdown();
            }
        }
        logger.info("线程管理器关闭完成");
    }

    public Future<?> submit(RProtocol protocol){
        return protocols.submit(protocol);
    }

    public <T> Future<T> submit(RProtocol protocol, T t){
        return protocols.submit(protocol, t);
    }

    public <T> Future<T> submit(Callable<T> callable){
        return protocols.submit(callable);
    }

    public void execute(RProtocol protocol){
        protocols.execute(protocol);//TODO 考虑如果线程执行时阻塞怎么办，xdb用的超时处理
    }

    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit){
        if(delay < 0){
            delay = 0;
        }
        return scheduleds.schedule(task, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit){
        if(delay < 0){
            delay = 0;
        }
        return scheduleds.schedule(callable, delay, unit);
    }

    /**
     * 一次任务开始执行时，就开始计算间隔时间的定时
     * @param task
     * @param delay
     * @param period
     * @param unit
     * @return
     */
    public ScheduledFuture<?> scheduledAtFixedRate(Runnable task, long delay, long period, TimeUnit unit){
        return scheduleds.scheduleAtFixedRate(task, delay, period, unit);
    }

    /**
     * 一次任务开始执行，直到这个任务执行结束才开始计算间隔时间的定时
     * @param task
     * @param delay
     * @param period
     * @param unit
     * @return
     */
    public ScheduledFuture<?> scheduledWithFixedDelay(Runnable task, long delay, long period, TimeUnit unit){
        return scheduleds.scheduleWithFixedDelay(task, delay, period, unit);
    }

}
