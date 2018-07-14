package game;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import log.Logger;
import proto.MainProto;
import proto.RProtocol;
import proto.handler.CEnterWordHandler;
import proto.handler.CRoleListHandler;

/**
 * @author sunfengmao
 * @Date 2018/5/26
 */
public class RGameServerHandler extends SimpleChannelInboundHandler<MainProto.Send> {

    private static final Logger logger = Logger.getLogger(RGameServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MainProto.Send send) throws Exception {
        //TODO 这里先这么写，之后肯定要优化
        if(send.getType() == MainProto.Send.ProtoType.CRoleList){
            RProtocol protocol = new CRoleListHandler(channelHandlerContext, send.getCRoleList());
            RExecutor.getInstance().execute(protocol);
        }else if(send.getType() == MainProto.Send.ProtoType.CEnterWord){
            RProtocol protocol = new CEnterWordHandler(channelHandlerContext, send.getCEnterWord());
            RExecutor.getInstance().execute(protocol);
        }else{
            logger.error("收到协议异常，type={}，protocol={}", send.getType(), send);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
////        String body = (String) msg;
////        System.out.println("SGame服接收到：" + body);
////        String result = "SGame服收到了你的消息." + StringUtil.lineSeparator();
////        ByteBuf buf = Unpooled.copiedBuffer(result.getBytes());
////        ctx.writeAndFlush(buf);//给客户端回消息
//
////        MainProto.CEnterWord cEnterWord = MainProto.CEnterWord.parseFrom(((String) msg).getBytes());
//
//        MainProto.CEnterWord cEnterWord = (MainProto.CEnterWord) msg;
//        int roleId = cEnterWord.getRoleId();
//        logger.debug("用户{}请求进入世界", roleId);
//
//        MainProto.SEnterWord sEnterWord = MainProto.SEnterWord.newBuilder()
//                .setRoleId(roleId)
//                .setSchool(MainProto.SEnterWord.SchoolType.FASHI)
//                .addAllFriendIds(Arrays.asList(12139, 12140, 12141))
//                .addXiakes(MainProto.SEnterWord.XiakeInfo.newBuilder()
//                        .setXiakeId(1)
//                        .setXiakeName("天下第一怪"))
//                .addXiakes(MainProto.SEnterWord.XiakeInfo.newBuilder()
//                        .setXiakeId(2)
//                        .setXiakeName("天下第二怪"))
//                .build();
//        ByteBuf buf1 = Unpooled.copiedBuffer(sEnterWord.toByteArray());
//        ctx.writeAndFlush(buf1);
//    }

}
