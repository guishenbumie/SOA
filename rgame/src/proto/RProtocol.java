package proto;

import io.netty.channel.ChannelHandlerContext;
import log.Logger;

/**
 * @author sunfengmao
 * @Date 2018/5/31
 * 所有协议的父类
 */
public abstract class RProtocol implements Runnable {

    protected ChannelHandlerContext ctx;
    protected int userId;
    protected Object msg;
    protected Logger logger;

    public RProtocol(ChannelHandlerContext ctx, int userId, Object msg) {
        this.ctx = ctx;
        this.userId = userId;
        this.msg = msg;
        logger = Logger.getLogger(this.getClass());
    }

    @Override
    public final void run() {
        logger.debug("{}开始处理协议{}", this.getClass().getName(), msg.getClass().getName());
        process();
    }

    protected abstract void process();

}
