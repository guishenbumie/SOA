package game.time;

import game.world.Onlines;
import log.Logger;
import proto.MainProto;
import util.TimeUtil;

import java.util.TimerTask;

/**
 * @author sunfengmao
 * @Date 2018/6/19
 * 每隔一段时间给所有连接的客户端同步当前时间，也算是一种心跳
 */
public class GameTimeTask extends TimerTask {

    Logger logger = Logger.getLogger(GameTimeTask.class);

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        logger.debug("定时给各客户端同步时间{}", TimeUtil.getTimeStr(now));
        MainProto.SGameTime sGameTime = MainProto.SGameTime.newBuilder()
                .setServerTime(now).build();
        Onlines.getInstance().getAllRoles().stream().forEach(r -> {
            MainProto.Send send = MainProto.Send.newBuilder()
                    .setUserId(r.getUserId())
                    .setType(MainProto.Send.ProtoType.SGameTime)
                    .setSGameTime(sGameTime).build();
            r.getCtx().writeAndFlush(send);
        });
    }

}
