package proto.handler.gate;

import gate.Gates;
import io.netty.channel.ChannelHandlerContext;
import proto.MainProto;
import proto.RProtocol;

/**
 * @author sunfengmao
 * @Date 2018/6/21
 */
public class GRConnectHandler extends RProtocol {

    public GRConnectHandler(ChannelHandlerContext ctx, int userId, Object msg) {
        super(ctx, userId, msg);
    }

    @Override
    protected void process() {
        MainProto.GRConnect grConnect = (MainProto.GRConnect) msg;
        Gates.getInstance().insert(grConnect.getGateId(), ctx);
        logger.info("gate和game连接成功，收到gate发来的协议");

        MainProto.RGConnectRes rGConnectRes = MainProto.RGConnectRes.newBuilder()
                .setResult(MainProto.RGConnectRes.Result.SUCCESS).build();
        MainProto.Send send = MainProto.Send.newBuilder()
                .setType(MainProto.Send.ProtoType.RGConnectRes)
                .setRGConnectRes(rGConnectRes).build();
        ctx.writeAndFlush(send);
    }

}
