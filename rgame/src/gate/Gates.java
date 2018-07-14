package gate;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sunfengmao
 * @Date 2018/6/26
 * 管理game和gate的网络连接
 */
public class Gates {

    private Map<Integer, ChannelHandlerContext> gateId2Ctx = new HashMap<>();

    private Lock lock = new ReentrantLock();

    private static class Inner {
        private static Gates instance = new Gates();
    }

    private Gates(){}

    public static Gates getInstance(){
        return Inner.instance;
    }

    /**
     * 添加一个新的gate的连接
     * @param gateId
     * @param ctx
     */
    public void insert(int gateId, ChannelHandlerContext ctx){
        try {
            lock.lockInterruptibly();

            gateId2Ctx.put(gateId, ctx);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
