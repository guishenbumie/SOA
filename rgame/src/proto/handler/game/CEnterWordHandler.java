package proto.handler.game;

import game.ConfigManager;
import game.SRoleBaseConfig;
import game.world.Onlines;
import io.netty.channel.ChannelHandlerContext;
import proto.MainProto;
import proto.RProtocol;

import java.util.Arrays;

/**
 * @author sunfengmao
 * @Date 2018/6/6
 */
public class CEnterWordHandler extends RProtocol {

    public CEnterWordHandler(ChannelHandlerContext ctx, int userId, Object msg) {
        super(ctx, userId, msg);
    }

    @Override
    protected void process() {
        MainProto.CEnterWord cEnterWord = (MainProto.CEnterWord) msg;
        long roleId = cEnterWord.getRoleId();
        Onlines.getInstance().insert(userId, roleId, ctx);
        logger.debug("角色{}请求进入世界", roleId);

        SRoleBaseConfig conf = ConfigManager.getInstance().getConfById(
                SRoleBaseConfig.class, MainProto.SEnterWord.SchoolType.FASHI.getNumber());
        if(null == conf){
            logger.error("SRoleBaseConfig表获取id={}的配置出错!", MainProto.SEnterWord.SchoolType.FASHI.getNumber());
            return;
        }

        MainProto.SEnterWord sEnterWord = MainProto.SEnterWord.newBuilder()
                .setRoleId(roleId)
                .setRoleName(conf.getName())
                .setSchool(MainProto.SEnterWord.SchoolType.FASHI)
                .addAllFriendIds(Arrays.asList(12139, 12140, 12141))
                .addXiakes(MainProto.SEnterWord.XiakeInfo.newBuilder()
                        .setXiakeId(1)
                        .setXiakeName("天下第一怪"))
                .addXiakes(MainProto.SEnterWord.XiakeInfo.newBuilder()
                        .setXiakeId(2)
                        .setXiakeName("天下第二怪"))
                .build();
        MainProto.Send send = MainProto.Send.newBuilder()
                .setUserId(userId)
                .setType(MainProto.Send.ProtoType.SEnterWord)
                .setSEnterWord(sEnterWord).build();
        ctx.writeAndFlush(send);
    }

}
