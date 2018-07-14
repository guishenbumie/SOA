package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import log.Logger;

/**
 * @author sunfengmao
 * @Date 2018/6/26
 */
public class HeartHandler extends ChannelInboundHandlerAdapter {

    Logger logger = Logger.getLogger(HeartHandler.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                logger.debug("与client的通道已经一段时间内没有读操作!");

            }else if(event.state() == IdleState.WRITER_IDLE){
                logger.debug("与client的通道已经一段时间没有写操作！");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
