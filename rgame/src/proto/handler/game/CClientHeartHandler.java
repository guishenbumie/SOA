package proto.handler.game;

import io.netty.channel.ChannelHandlerContext;
import proto.MainProto;
import proto.RProtocol;

/**
 * @author sunfengmao
 * @Date 2018/6/30
 */
public class CClientHeartHandler extends RProtocol {

    public CClientHeartHandler(ChannelHandlerContext ctx, int userId, Object msg) {
        super(ctx, userId, msg);
    }

    @Override
    protected void process() {
        MainProto.CClientHeart cClientHeart = (MainProto.CClientHeart) msg;
    }

}
