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

import MethodMessage.MethodInfos;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
public class RPC_NettyClient {
    static Set<MethodInfos.MethodInfo> set = new HashSet<>();
    public static void main(String[] args) {
//        Set<MethodInfos.MethodInfo> set = new HashSet<>();
        EventLoopGroup boss = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(boss).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                //protobuf解码
                pipeline.addLast(new ProtobufDecoder(MethodInfos.MyMessage.getDefaultInstance()));
                //protobuf编码
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());

                pipeline.addLast(new SimpleChannelInboundHandler<MethodInfos.MyMessage>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MethodInfos.MyMessage myMessage) throws Exception {
//                        Channel channel = channelHandlerContext.channel();
                        MethodInfos.MethodInfo methodinfo = myMessage.getMethodinfo();
                        set.add(methodinfo);
                        MethodInfos.Response response = myMessage.getResponse();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(methodinfo.getClassname());
                        stringBuilder.append(methodinfo.getMethodname());
                        List<MethodInfos.ParagramesInfo> paragrameinfoList = methodinfo.getParagrameinfoList();
                        for (MethodInfos.ParagramesInfo paragramesInfo : paragrameinfoList) {
                            stringBuilder.append(paragramesInfo.getParagrameName());
                            stringBuilder.append(paragramesInfo.getParagrameType());
                            stringBuilder.append(paragramesInfo.getParagramevalue());
                        }
                        stringBuilder.append(response.getValue());
                        System.out.println(stringBuilder.toString());
                    }
                });
            }
        });
        try {
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8899).sync();
            Channel channel = channelFuture.channel();
            //简单的测试一下，看看可不可以有返回值。
//            MethodInfos.MethodInfo methodInfo = MethodInfos.MethodInfo.newBuilder().setClassname("TestMethod")
//                    .setMethodname("test").setMethodReturnType("int").build();

            test test = new test(channel);
            test.start();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
        }
    }
    static class test extends Thread{
        Channel channel;
        public test(Channel channel){
            this.channel = channel;
        }
        @Override
        public void run() {
            boolean kk = true;
            while(kk) {
                Iterator<MethodInfos.MethodInfo> iterator = set.iterator();
                if (set.size()>0) {
                    while (iterator.hasNext()) {
                        MethodInfos.MethodInfo next = iterator.next();
                        if (next != null) {
                            MethodInfos.MyMessage message = MethodInfos.MyMessage.newBuilder().setMethodinfo(next).build();
                            channel.writeAndFlush(message);
                            kk = false;
                        }
                    }
                }else{
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}