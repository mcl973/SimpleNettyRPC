/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: ServiceHandler
 * Author:   Administrator
 * Date:     2020/04/03 14:12
 * Description: service的handler
 * History:
 * <author>          <time>          <version>          <desc>
 */
package NettyServer;

import MethodMessage.MethodInfoses;
import ScannerAndInstance.AbstractBean;
import Serializable_Handler.Handler;
import ThreadPool.ThreadExcutors;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 〈一句话功能简述〉<br>
 * 〈service的handler〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
public class ServiceHandler extends SimpleChannelInboundHandler< MethodInfoses.MyMessages> {
    //获取具体可以被远程调用的方法
    private Map<String, MethodInfoses.MethodInfoes> map = AbstractBean.methodInfoMap;
    //获取ioc容器
    private Map<String,Object> iocmap = AbstractBean.iocmap;
    //获取单例的线程池
    private ExecutorService executorService = ThreadExcutors.INSTANCE.getExecutor();
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //这里需要需要调用的方法传输回去
        for(Map.Entry<String, MethodInfoses.MethodInfoes> maps:map.entrySet()) {
            MethodInfoses.MyMessages message = MethodInfoses.MyMessages.newBuilder().setMethodinfo(maps.getValue()).build();
            Channel channel = ctx.channel();
            channel.writeAndFlush(message);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MethodInfoses.MyMessages myMessage) throws Exception {
        //在这里处理具体的事务
        //使用线程池来解决netty 的channelread0的单线程问题
        MethodInfoses.MethodInfoes methodinfo = myMessage.getMethodinfo();
        executorService.execute(new tack(methodinfo,channelHandlerContext.channel()));
    }

    class tack implements Runnable{
        MethodInfoses.MethodInfoes methodInfo;
        Channel channel;
        public tack(MethodInfoses.MethodInfoes methodInfo,Channel channel){
            this.methodInfo = methodInfo;
            this.channel = channel;
        }

        @Override
        public void  run() {
            //查找具体的函数和具体的对象实例
            //查找对象实例
            String classname = methodInfo.getClassname();
            Object o = iocmap.get(classname);
            Class<?> aClass = o.getClass();
            Method[] declaredMethods = aClass.getDeclaredMethods();
            Method method = null;
            //对比对象中的所有的方法，取出返回类型、方法名、参数类型，参数名字、全都与
            //methodinfo中一样的方法。
            for (Method declaredMethod : declaredMethods) {
                //只需要对比hashcode的值就可以了
                if(declaredMethod.hashCode() == methodInfo.getMethodhashcode()){
                    //如果hashcode相同空，那么就表示是同一个，虽然说这样有可能会有冲突的出现，但是目前
                    //就这样了。后续再去修改
                    method = declaredMethod;
                    break;
                }
            }
            Handler handler = new Handler();
            if(method != null){
                //获取参数，获取传递过来的参数值
                int count = methodInfo.getParagrameinfoCount();
                Object[] objects = new Object[count];
                for(int i=0;i<count;i++){
                    //这里需要做一个操作，就是将byte【】类型的转成object的。
                    MethodInfoses.ParagramesInfoes paragrameinfo = methodInfo.getParagrameinfo(i);
                    ByteString byteString = paragrameinfo.getParagramevalue();
                    Object object = handler.getObject(byteString);
                    objects[i] =object;
                }
                try {
                    //执行方法
                    Object object = method.invoke(o,objects);
                    //首先将结果序列化
                    MethodInfoses.Responses serializableResponse = handler.getSerializableResponse(object);
                    //将返回值传递给response,在封装成mymessage
                    MethodInfoses.MyMessages message = MethodInfoses.MyMessages.newBuilder()
                            .setResponse(serializableResponse).build();
                    //将mymessage传递给远程。
                    channel.writeAndFlush(message);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
