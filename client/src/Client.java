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

/**
 * @author sunfengmao
 * @Date 2018/5/26
 */
public class Client {

    private static final String host = "127.0.0.1";
    private static int port = 8900;

    private static Logger logger = Logger.getLogger(Client.class);

    public static void main(String[] args) throws Exception{
        System.out.println("客户端开启，开始给服务器发消息");

        if(null == args && args.length > 0){
            port = Integer.valueOf(args[0]);
        }

        connect();
    }

    private static void connect(){
        //配置客户端Nio线程组
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
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            //发起异步连接
            ChannelFuture future = bootstrap.connect(host, port).sync();

            //等待客户端链路关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            logger.error("连接出现问题！{}", e);
        } finally {
            //释放nio线程组
            group.shutdownGracefully();
        }
    }

}
