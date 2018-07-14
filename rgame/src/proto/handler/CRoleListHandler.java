package proto.handler;

import io.netty.channel.ChannelHandlerContext;
import proto.MainProto;
import proto.RProtocol;

import java.util.Arrays;

/**
 * @author sunfengmao
 * @Date 2018/5/31
 */
public class CRoleListHandler extends RProtocol {

    public CRoleListHandler(ChannelHandlerContext ctx, Object msg) {
        super(ctx, msg);
    }

    @Override
    protected void process() {
        MainProto.CRoleList cRoleList = (MainProto.CRoleList) msg;
        logger.debug("用户请求自己的角色列表");

        MainProto.SRoleList sRoleList = MainProto.SRoleList.newBuilder()
                .addAllRoleIds(Arrays.asList(12137, 12138)).build();
        MainProto.Send send = MainProto.Send.newBuilder()
                .setType(MainProto.Send.ProtoType.SRoleList)
                .setSRoleList(sRoleList).build();
        ctx.writeAndFlush(send);
    }

}
