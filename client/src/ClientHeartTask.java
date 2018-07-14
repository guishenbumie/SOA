import io.netty.channel.ChannelHandlerContext;

/**
 * @author sunfengmao
 * @Date 2018/6/26
 * 给gate发送心跳
 */
public class ClientHeartTask implements Runnable {

    private ChannelHandlerContext ctx;

    public ClientHeartTask(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run() {

    }

}
