package game;

import gate.GateBean;
import gate.GateMain;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import proto.MainProto;

import java.util.concurrent.BlockingQueue;

/**
 * @author sunfengmao
 * @Date 2018/6/20
 */
public class ConnectGameTask implements Runnable {

    public static final String RGAMESERVER_HOST = "127.0.0.1";
    private static final int RGAMESERVER_PORT = 8920;

    public ConnectGameTask(){}

    @Override
    public void run() {
        while (GateMain.isOpen) {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                                pipeline.addLast(new ProtobufDecoder(MainProto.Send.getDefaultInstance()));
                                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                                pipeline.addLast(new ProtobufEncoder());
                                pipeline.addLast(new GateGameHandler());
                            }
                        });

                //发起异步连接
                ChannelFuture future = bootstrap.connect(RGAMESERVER_HOST, RGAMESERVER_PORT).sync();

                //等待客户端链路关闭
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //释放nio线程组
                group.shutdownGracefully();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
