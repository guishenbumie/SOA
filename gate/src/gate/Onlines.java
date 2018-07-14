package gate;

import io.netty.channel.ChannelHandlerContext;
import log.Logger;
import proto.MainProto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author sunfengmao
 * @Date 2018/6/25
 * 网络连接的情况
 */
public class Onlines {

    private Map<Integer, ChannelHandlerContext> userId2Client = new ConcurrentHashMap<>();//key=用户id，value=与对应的客户端的网络连接
    private ChannelHandlerContext toGame;

    private Logger logger = Logger.getLogger(Onlines.class);

    private static Lock lock = new ReentrantLock();

    private static Onlines instance;

    private Onlines(){}

    public static Onlines getInstance(){
        return instance;
    }

    /**
     * 获得和game的网络连接
     * @return
     */
    public ChannelHandlerContext getToGameCtx(){
        return toGame;
    }

    /**
     * 获得与执行user的网络连接
     * @param userId
     * @return
     */
    public ChannelHandlerContext getUserCtx(int userId){
        return userId2Client.get(userId);
    }

    /**
     * 添加和gama的网络连接情况
     * @param ctx
     */
    public static void connectGame(ChannelHandlerContext ctx){
        lock.lock();
        if(null == instance){
            instance = new Onlines();
        }
        instance.toGame = ctx;
        GateMain.isOpen = true;
        lock.unlock();
        instance.logger.info("添加与game的网络连接");
    }

    /**
     * 添加一个用户和他对应的网络连接情况
     * @param userId
     * @param ctx
     * @return
     */
    public MainProto.GCConnectRes.Result addUser(int userId, ChannelHandlerContext ctx){
        try {
            lock.lockInterruptibly();
            if(userId2Client.containsKey(userId)){
                //TODO 如果已经有该用户的网络连接了，是否应该做踢下线处理，让用户重新连呢
                logger.error("用户{}的网络连接已经存在", userId);
                userId2Client.remove(userId);
                return MainProto.GCConnectRes.Result.REPEAT;
            }
            userId2Client.put(userId, ctx);
            logger.info("添加用户{}的网络连接", userId);
            return MainProto.GCConnectRes.Result.SUCCESS;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return MainProto.GCConnectRes.Result.UNKNOWN;
    }

}
