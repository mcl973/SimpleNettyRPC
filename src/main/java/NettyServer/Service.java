/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: Service
 * Author:   Administrator
 * Date:     2020/04/03 15:01
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package NettyServer;

import com.sun.corba.se.internal.CosNaming.BootstrapServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
public class Service {
    private final String host = "127.0.0.1";
    private final int port = 8899;
    private  EventLoopGroup boss;
    private  EventLoopGroup worker;

    public ServerBootstrap createBoot(){
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,worker).channel(NioServerSocketChannel.class).childHandler(new ServiceChannelInbound());
        return serverBootstrap;
    }
    public void start(){
        ServerBootstrap boot = createBoot();
        try {
            ChannelFuture channelFuture = boot.bind(host, port).sync();
            Channel channel = channelFuture.channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
