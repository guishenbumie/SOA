package game;

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
import log.Logger;
import proto.MainProto;
import proto.ProtoBean;
import proto.RProtocol;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author sunfengmao
 * @Date 2018/6/13
 */
public class RControlor implements RControlorMXBean {

    private static final Logger logger = Logger.getLogger(RControlor.class);

    private static RControlor instance = new RControlor();

    private RControlor(){}

    public static RControlor getInstance(){
        return instance;
    }

    private ChannelFuture future;//TODO 这块要不要加同步相关的东西

    private static final Map<MainProto.Send.ProtoType, ProtoBean> protocols = new ConcurrentHashMap<>();
    private static final Class[] protoParams = {ChannelHandlerContext.class, int.class, Object.class};

    /**
     * 加载协议相关的东西，这里暂时先这么做
     */
    public void loadProtocols(){
        final String prefix = "proto.handler.";
        final String suffix = "Handler";
        int protoSize = MainProto.Send.ProtoType.values().length;
        if(protoSize <= 3){
            logger.error("init protocol error!");
            return;
        }
        //protoBuf的枚举必须从0开始，而且最后有个默认添加的-1，也为了和协议的type号对应，真正的协议从2开始
        MainProto.Send.ProtoType[] protoType =
                Arrays.copyOfRange(MainProto.Send.ProtoType.values(), 1, protoSize-1);

        final String gatePrefix = prefix + "gate.";
        Arrays.stream(protoType)
                .filter(f -> f.name().startsWith("GR"))
                .forEach(p -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(gatePrefix).append(p.name()).append(suffix);
                    protocols.put(p, new ProtoBean(p, p.name(), sb.toString()));
                    logger.info("register handler={} of protocol={}", sb.toString(), p.name());
                });

        final String gamePrefix = prefix + "game.";
        Arrays.stream(protoType)
                .filter(f -> f.name().startsWith("C"))
                .forEach(p -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(gamePrefix).append(p.name()).append(suffix);
                    protocols.put(p, new ProtoBean(p, p.name(), sb.toString()));
                    logger.info("register handler={} of protocol={}", sb.toString(), p.name());
                });
    }

    public void open(){
        //配置服务器的Nio线程服
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
                            pipeline.addLast(new RGameServerHandler());

//                            //心跳机制
//                            pipeline.addLast(new IdleStateHandler(100, 0, 0, TimeUnit.SECONDS));
//                            //自己的心跳处理
//                            pipeline.addLast(new HeartBeatServerHandler());
                        }
                    });

            //绑定端口，同步等待成功
            future = bootstrap.bind(ConfigManager.rgameserver_port).sync();

            //等待服务器监听端口关闭
            RGameServer.isOpen = true;
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() {
        if(RGameServer.isOpen){
            future.channel().close();
        }
    }

    private class RGameServerHandler extends SimpleChannelInboundHandler<MainProto.Send> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, MainProto.Send send) throws Exception {
            //收到协议，根据协议号找到对应的协议和它的处理类的类名，再通过反射获得对应处理类的一个对象来处理相关的协议
            //协议和对应的处理类，初步思路是用自动生成代码的工具实现
            ProtoBean protoBean = protocols.get(send.getType());
            if(null == protoBean){
                logger.error("收到协议异常，type={}，protocol={}", send.getType(), send);
                return;
            }
            logger.debug("RGameServer收到协议{}", protoBean.getProtoName());
            Class clz = null;
            try {
                clz = Class.forName(protoBean.getHandlerName());
            } catch (ClassNotFoundException e) {
                logger.error("没有找到协议{}的处理类！", protoBean.getProtoName());
                return;
            }
            Constructor con = null;
            try {
                con = clz.getConstructor(protoParams);
            } catch (Exception e){
                logger.error("获取协议{}的构造方法出错！{}", protoBean.getProtoName(), e);
                return;
            }
            Method method = null;
            try {
                method = send.getClass().getMethod("get"+protoBean.getProtoName());
            } catch (Exception e){
                logger.error("通过send协议的指定方法{}获取对应的协议出错！", "get"+protoBean.getProtoName());
                return;
            }
            Object o = null;
            try {
                o = con.newInstance(ctx, send.getUserId(), method.invoke(send));
            } catch (Exception e) {
                logger.error("调用send协议的指定方法{}获取协议出错！", "get"+protoBean.getProtoName());
                return;
            }
            if(!(o instanceof RProtocol)){
                logger.error("处理类{}没有继承RProtocol类！", protoBean.getHandlerName());
                return;
            }
            RProtocol protocol = (RProtocol) o;
            RExecutor.getInstance().execute(protocol);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }

    @Override
    public String reload() {
        //先加载各种配置
        ReloadResult result = ConfigManager.getInstance().reload();
        if(!result.isSuccess()){
            return result.getMsg();
        }
        result.appendMsg("Reload success!\n");
        return result.getMsg();
    }

    @Override
    public String reload(String key, String value) {
        if(null == key || null == value){
            return "Reload fail!";
        }
        String old = ConfigManager.getInstance().getSimbeans().putIfAbsent(key, value);
        if(null == old){
            return "Reload fail!";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Reload success!");
        sb.append("key=").append(key).append(",old=").append(old).append(",new=").append(value);
        return sb.toString();
    }

}
