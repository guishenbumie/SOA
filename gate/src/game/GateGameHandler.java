package game;

import gate.Executor;
import gate.GateMain;
import gate.Onlines;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import log.Logger;
import proto.MainProto;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 */
public class GateGameHandler extends SimpleChannelInboundHandler<MainProto.Send> {

    private static final Logger logger = Logger.getLogger(GateGameHandler.class);

    public GateGameHandler(){}

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("gate连接上game，给game发协议");
        MainProto.GRConnect grConnect = MainProto.GRConnect.newBuilder()
                .setGateId(GateMain.gateNum.incrementAndGet()).build();
        MainProto.Send send = MainProto.Send.newBuilder()
                .setType(MainProto.Send.ProtoType.GRConnect)
                .setGRConnect(grConnect).build();
        ctx.writeAndFlush(send);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MainProto.Send send) throws Exception {
        if(send.getType() == MainProto.Send.ProtoType.RGConnectRes){//收到game的建立连接成功协议
            MainProto.RGConnectRes rgConnectRes = send.getRGConnectRes();
            if(rgConnectRes.getResult() != MainProto.RGConnectRes.Result.SUCCESS){
                logger.error("与game连接出错");
                ctx.close();
            }
            Onlines.connectGame(ctx);
            logger.info("gate与game建立连接成功");
        }else{//其他协议都是gate转发给client
            Executor.getInstance().send2Client(send);
//            logger.error("收到game的协议错误！");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("异常" + cause.getMessage());
        //释放资源
        ctx.close();
    }

}
