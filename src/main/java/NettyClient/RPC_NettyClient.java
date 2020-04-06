/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: RPC_NettyClient
 * Author:   Administrator
 * Date:     2020/04/03 16:25
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package NettyClient;

import MethodMessage.MethodInfoses;
import Serializable_Handler.Handler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
public class RPC_NettyClient {
    static ConcurrentHashMap<MethodInfoses.MethodInfoes,Object> set = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    public RPC_NettyClient(){
        EventLoopGroup boss = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(boss).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                //protobuf解码
                pipeline.addLast(new ProtobufDecoder(MethodInfoses.MyMessages.getDefaultInstance()));
                //protobuf编码
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

                pipeline.addLast(new SimpleChannelInboundHandler<MethodInfoses.MyMessages>() {

                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MethodInfoses.MyMessages myMessages) throws Exception {
                        new clientExecuteThread(myMessages).start();
                        System.out.println("/////////");
                    }
                });
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8899).sync();
            Channel channel = channelFuture.channel();
            //简单的测试一下，看看可不可以有返回值。
            test test = new test(channel);
            test.start();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
        }
    }
    class test extends Thread{
        Channel channel;
        Handler handler = new Handler();
        public test(Channel channel){
            this.channel = channel;
        }
        @Override
        public void run() {
            boolean kk = true;

            while(kk) {
                for(Map.Entry<MethodInfoses.MethodInfoes,Object> nexts:set.entrySet()){
                        MethodInfoses.MethodInfoes next = nexts.getKey();
                        if(next.getParagrameinfoCount() == 0) {
                            if (next != null) {
                                Object[] object = new Object[next.getParagrameinfoCount()];
//                                object[0] = "mcl";
//                                object[1] = 10;
                                MethodInfoses.MethodInfoes excute = excute(next, object);
                                MethodInfoses.MyMessages build = MethodInfoses.MyMessages.newBuilder().setMethodinfo(excute).build();
                                channel.writeAndFlush(build);
                                kk = false;
                            }
                        }
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        //执行包装具体的mymessage
        public MethodInfoses.MethodInfoes excute( MethodInfoses.MethodInfoes next,Object[] args){
            MethodInfoses.MethodInfoes.Builder builder = MethodInfoses.MethodInfoes.newBuilder()
                    .setClassname(next.getClassname())
                    .setMethodhashcode(next.getMethodhashcode());
            //填充参数值
            for (Object arg : args) {
                builder.addParagrameinfo(handler.getSerializableParagrame(arg));
            }
            return builder.build();
        }
    }
    //处理 接收到的数据
    //包括接收服务端推送的数据
    //和解析函数调用返回的结果
    class clientExecuteThread extends Thread{
        MethodInfoses.MyMessages myMessages;
        public clientExecuteThread(MethodInfoses.MyMessages myMessages){
            this.myMessages = myMessages;
        }
        @Override
        public void run() {
            synchronized (lock) {
                System.out.println("//////////////");
                Handler handler = new Handler();
                MethodInfoses.MethodInfoes methodinfo = myMessages.getMethodinfo();
                assert methodinfo != null;
                set.put(methodinfo,new Object());
                MethodInfoses.Responses response = myMessages.getResponse();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(methodinfo.getClassname());
                stringBuilder.append("\n");
                stringBuilder.append(methodinfo.getMethodhashcode());
                stringBuilder.append("\n");
                List<MethodInfoses.ParagramesInfoes> paragrameinfoList = methodinfo.getParagrameinfoList();
//            首先通过
                for (MethodInfoses.ParagramesInfoes paragramesInfo : paragrameinfoList) {
                    stringBuilder.append(paragramesInfo.getPtn().getParagrameName());
                    stringBuilder.append("\n");
                    stringBuilder.append(paragramesInfo.getPtn().getParagrameType());
                    stringBuilder.append("\n");
                }
                //获取函数执行返回的值
                Object object = handler.getObject(response.getResponsevalue());
                if (object != null) {
                    Class<?> aClass = object.getClass();
                    String typeName = aClass.getTypeName();
                    Object o = handler.getreturnType(typeName, object);
                    if (o == null) {//不是八大基本类型和String类型
                        Field[] declaredFields = aClass.getDeclaredFields();
                        for (Field declaredField : declaredFields) {
                            //通过反射获取值
                            declaredField.setAccessible(true);
                            try {
                                stringBuilder.append(declaredField.getName() + " " + declaredField.get(object));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                            stringBuilder.append("\n");
                        }
                    } else {//直接输出结果
                        System.out.println(object);
                    }
                }
                System.out.println(stringBuilder.toString());
            }
        }
    }
    public static void main(String[] args) {
//        Set<MethodInfos.MethodInfo> set = new HashSet<>();
        new RPC_NettyClient();
    }
}
