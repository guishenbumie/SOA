package game.world;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author sunfengmao
 * @Date 2018/6/21
 * 在线的用户以及他的网络连接相关信息
 */
public class Onlines {

    private Lock lock = new ReentrantLock();

    private Map<Integer, Role> roles = new HashMap<>();

    private static class Inner {
        private static Onlines instance = new Onlines();
    }

    private Onlines(){}

    public static Onlines getInstance() {
        return Inner.instance;
    }

    /**
     * 添加新的在线用户
     * @param userId
     * @param roleId
     * @param ctx
     */
    public void insert(int userId, long roleId, ChannelHandlerContext ctx){
        try {
            lock.lockInterruptibly();

            roles.put(userId, new Role(userId, roleId, ctx));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获得当前所有在线玩家
     * @return
     */
    public List<Role> getAllRoles(){
        return roles.values().stream().collect(Collectors.toList());
    }

}
