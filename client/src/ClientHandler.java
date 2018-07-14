import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import proto.MainProto;

/**
 * @author sunfengmao
 * @Date 2018/5/26
 */
public class ClientHandler extends SimpleChannelInboundHandler<MainProto.Send> {

    private static final Logger logger = Logger.getLogger(ClientHandler.class);

    private static final int USERID = 111;//测试用的useid

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("用户{}尝试连接gate", USERID);
        MainProto.CGConnect cgConnect = MainProto.CGConnect.newBuilder().build();
        MainProto.Send send = MainProto.Send.newBuilder()
                .setUserId(USERID)
                .setType(MainProto.Send.ProtoType.CGConnect)
                .setCGConnect(cgConnect).build();
        ctx.writeAndFlush(send);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MainProto.Send result) throws Exception {
        if(result.getType() == MainProto.Send.ProtoType.GCConnectRes){
            MainProto.GCConnectRes.Result flag = result.getGCConnectRes().getResult();
            if(flag == MainProto.GCConnectRes.Result.SUCCESS){
                MainProto.CRoleList cRoleList = MainProto.CRoleList.newBuilder().build();
                logger.debug("用户请求自己的角色列表");
                MainProto.Send send = MainProto.Send.newBuilder()
                        .setUserId(USERID)
                        .setType(MainProto.Send.ProtoType.CRoleList)
                        .setCRoleList(cRoleList).build();
                ctx.writeAndFlush(send);
            }else if(flag == MainProto.GCConnectRes.Result.REPEAT){
                logger.debug("用户{}顶掉之前与gate的连接重新连接", USERID);
                MainProto.CGConnect cgConnect = MainProto.CGConnect.newBuilder().build();
                MainProto.Send send = MainProto.Send.newBuilder()
                        .setUserId(USERID)
                        .setType(MainProto.Send.ProtoType.CGConnect)
                        .setCGConnect(cgConnect).build();
                ctx.writeAndFlush(send);
            }else if(flag == MainProto.GCConnectRes.Result.UNKNOWN){
                logger.debug("连接gate的未知错误！");
                ctx.close();
            }
        }else if(result.getType() == MainProto.Send.ProtoType.SRoleList){
            MainProto.SRoleList sRoleList = result.getSRoleList();
            logger.debug("用户可选择角色id：{}", sRoleList.getRoleIdsList());

            MainProto.CEnterWord cEnterWord = MainProto.CEnterWord.newBuilder()
                    .setRoleId(12138).build();
            MainProto.Send send = MainProto.Send.newBuilder()
                    .setUserId(USERID)
                    .setType(MainProto.Send.ProtoType.CEnterWord)
                    .setCEnterWord(cEnterWord).build();
            ctx.writeAndFlush(send);
        }else if(result.getType() == MainProto.Send.ProtoType.SEnterWord){
            MainProto.SEnterWord sEnterWord = result.getSEnterWord();
            logger.debug("用户{}{}成功进入世界，当前职业为{}，好友列表{}，侠客列表{}",
                sEnterWord.getRoleId(),
                sEnterWord.getRoleName(),
                sEnterWord.getSchool(),
                sEnterWord.getFriendIdsList());

        }else if(result.getType() == MainProto.Send.ProtoType.SGameTime){
            MainProto.SGameTime sGameTime = result.getSGameTime();
            logger.debug("同步game的时间{}", TimeUtil.getTimeStr(sGameTime.getServerTime()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("异常" + cause.getMessage());
        //释放资源
        ctx.close();
    }

}
