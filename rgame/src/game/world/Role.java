package game.world;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author sunfengmao
 * @Date 2018/6/26
 * 在线用户的信息
 */
public class Role {

    private int userId;
    private long roleId;
    private ChannelHandlerContext ctx;//与该用户所在gate的网络连接

    public Role(int userId, long roleId, ChannelHandlerContext ctx) {
        this.userId = userId;
        this.roleId = roleId;
        this.ctx = ctx;
    }

    public int getUserId() {
        return userId;
    }

    public long getRoleId() {
        return roleId;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
}
