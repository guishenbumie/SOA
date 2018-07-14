package client;

import gate.Executor;
import gate.Onlines;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import log.Logger;
import proto.MainProto;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 */
public class GateClientHandler extends SimpleChannelInboundHandler<MainProto.Send> {

    private static final Logger logger = Logger.getLogger(GateClientHandler.class);

    public GateClientHandler(){}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MainProto.Send send) throws Exception {
        if(send.getType() == MainProto.Send.ProtoType.CGConnect){
            MainProto.GCConnectRes.Result flag = Onlines.getInstance().addUser(send.getUserId(), ctx);
            MainProto.GCConnectRes gcConnectRes = MainProto.GCConnectRes.newBuilder()
                    .setResult(flag).build();
            MainProto.Send result = MainProto.Send.newBuilder()
                    .setType(MainProto.Send.ProtoType.GCConnectRes)
                    .setGCConnectRes(gcConnectRes).build();
            ctx.writeAndFlush(result);
        }else{
            Executor.getInstance().send2Game(send);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("异常" + cause.getMessage());
        //释放资源
        ctx.close();
    }

}
