package gate;

import io.netty.channel.ChannelHandlerContext;
import proto.MainProto;

/**
 * @author sunfengmao
 * @Date 2018/6/20
 */
public class GateBean {

    private ChannelHandlerContext ctx;
    private MainProto.Send send;

    public GateBean(ChannelHandlerContext ctx, MainProto.Send send) {
        this.ctx = ctx;
        this.send = send;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public MainProto.Send getSend() {
        return send;
    }

    public void setSend(MainProto.Send send) {
        this.send = send;
    }
}
