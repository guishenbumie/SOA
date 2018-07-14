package gate;

import io.netty.channel.ChannelHandlerContext;
import log.Logger;
import proto.MainProto;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author sunfengmao
 * @Date 2018/6/21
 */
public class Executor {

    private static Object lock = new Object();
    private static Logger logger = Logger.getLogger(Executor.class);

    private static Executor instance;

    private final ExecutorService service;

    private volatile boolean isStop = false;

    private GameProtoTask gameProtoTask;
    private ClientProtoTask clientProtoTask;

    private Executor(int poolSize){
        service = Executors.newFixedThreadPool(poolSize);
        gameProtoTask = new GameProtoTask();
        clientProtoTask = new ClientProtoTask();
    }

    public static Executor getInstance(){
        return instance;
    }

    public static void start(int poolSize){
        synchronized (lock) {
            if(null == instance){
                instance = new Executor(poolSize);
            }
            instance.service.submit(instance.gameProtoTask);
            instance.service.submit(instance.clientProtoTask);
        }
    }

    public static void stop(){
        synchronized (lock) {
            if(null != instance){
                instance.isStop = true;
                instance.service.shutdown();
            }
        }
    }

    public Future<?> submit(Runnable task){
        return service.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T t){
        return service.submit(task, t);
    }

    public <T> Future<T> submit(Callable<T> callable){
        return service.submit(callable);
    }

    public void execute(Runnable task){
        service.execute(task);
    }

    public void send2Game(MainProto.Send send){
        if(!gameProtoTask.queue.offer(new GateBean(Onlines.getInstance().getToGameCtx(), send))){
            logger.error("gate将给game发送的协议放到队列中失败！");
        }
    }

    public void send2Client(MainProto.Send send){
        ChannelHandlerContext ctx = Onlines.getInstance().getUserCtx(send.getUserId());
        if(null == ctx){
            logger.error("没有userid和对应网络连接的映射！{}", send.getType().name());
            //TODO 获取不到网络连接的情况会不会出现，出现了是否做一个踢下线的操作呢
            return;
        }
        if(!clientProtoTask.queue.offer(new GateBean(ctx, send))){
            logger.error("gate将给client发送的协议放到队列中失败！");
        }
    }

    private static class GameProtoTask implements Runnable{

        private BlockingQueue<GateBean> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (!instance.isStop) {
                GateBean gateBean = queue.poll();
                if(null != gateBean){
                    try {
                        gateBean.getCtx().writeAndFlush(gateBean.getSend());
                    } catch (Exception e) {
                        logger.error("gate给game发送协议出错：{}", e);
                    }
                    logger.debug("gate给game发协议{}", gateBean.getSend().getType().name());
                }
            }
        }

    }

    private static class ClientProtoTask implements Runnable{

        private BlockingQueue<GateBean> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (!instance.isStop) {
                GateBean gateBean = queue.poll();
                if(null != gateBean){
                    try {
                        gateBean.getCtx().writeAndFlush(gateBean.getSend());
                    } catch (Exception e) {
                        logger.error("gate给client发送协议出错：{}", e);
                    }
                    logger.debug("gate给client发送协议{}", gateBean.getSend().getType().name());
                }
            }
        }
    }

}
