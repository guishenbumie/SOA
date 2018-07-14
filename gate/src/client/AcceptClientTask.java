package client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import proto.MainProto;

import java.util.concurrent.TimeUnit;

/**
 * @author sunfengmao
 * @Date 2018/6/20
 */
public class AcceptClientTask implements Runnable {

    private static final int GATE_PORT = 8900;

    public AcceptClientTask(){}

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//使用TCP连接
                    .option(ChannelOption.SO_BACKLOG, 1024)//BACKLOG用于构造服务器套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求队列的最大长度
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();

                            //在解码前，利用包头中包含的数组长度解决半包/粘包的问题
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            //配置Protobuf解码处理器，收到消息会自动解码
                            pipeline.addLast(new ProtobufDecoder(MainProto.Send.getDefaultInstance()));
                            //在序列化的字节数组前加上序列化后的字节长度
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            //配置Protobuf解码器，发送消息会先经过编码
                            pipeline.addLast(new ProtobufEncoder());

                            //自己的消息处理器，接受消息后自己的处理
                            pipeline.addLast(new GateClientHandler());

                            //IdleStateHandler与HeartBeatHandler结合使用
                            //IdleStateHandler为netty提供的监听handler，监听对应事件的空闲时间
                            pipeline.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new HeartHandler());
                        }
                    });

            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(GATE_PORT).sync();

            //等待服务器监听端口关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
