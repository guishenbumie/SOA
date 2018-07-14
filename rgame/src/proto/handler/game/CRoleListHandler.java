package proto.handler.game;

import io.netty.channel.ChannelHandlerContext;
import proto.MainProto;
import proto.RProtocol;

import java.util.Arrays;

/**
 * @author sunfengmao
 * @Date 2018/5/31
 */
public class CRoleListHandler extends RProtocol {

    public CRoleListHandler(ChannelHandlerContext ctx, int userId, Object msg) {
        super(ctx, userId, msg);
    }

    @Override
    protected void process() {
        MainProto.CRoleList cRoleList = (MainProto.CRoleList) msg;
        logger.debug("用户{}请求自己的角色列表", userId);

        MainProto.SRoleList sRoleList = MainProto.SRoleList.newBuilder()
                .addAllRoleIds(Arrays.asList(Long.valueOf(12137), Long.valueOf(12138))).build();
        MainProto.Send send = MainProto.Send.newBuilder()
                .setUserId(userId)
                .setType(MainProto.Send.ProtoType.SRoleList)
                .setSRoleList(sRoleList).build();
        ctx.writeAndFlush(send);
    }

}
