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

import MethodMessage.MethodInfos;
import ScannerAndInstance.AbstractBean;
import ThreadPool.ThreadExcutors;
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
public class ServiceHandler extends SimpleChannelInboundHandler<MethodInfos.MyMessage> {
    //获取具体可以被远程调用的方法
    private Map<String, MethodInfos.MethodInfo> map = AbstractBean.methodInfoMap;
    //获取ioc容器
    private Map<String,Object> iocmap = AbstractBean.iocmap;
    //获取单例的线程池
    private ExecutorService executorService = ThreadExcutors.INSTANCE.getExecutor();
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("////////////");
        //这里需要需要调用的方法传输回去
        for(Map.Entry<String, MethodInfos.MethodInfo> maps:map.entrySet()) {
            MethodInfos.MyMessage message = MethodInfos.MyMessage.newBuilder().setMethodinfo(maps.getValue()).build();
            Channel channel = ctx.channel();
            channel.writeAndFlush(message);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MethodInfos.MyMessage myMessage) throws Exception {
        //在这里处理具体的事务
        //使用线程池来解决netty 的channelread0的单线程问题
        MethodInfos.MethodInfo methodinfo = myMessage.getMethodinfo();
        Future<?> submit = executorService.submit(new tack(methodinfo,channelHandlerContext.channel()));
    }

    class tack implements Runnable{
        MethodInfos.MethodInfo methodInfo;
        Channel channel;
        public tack(MethodInfos.MethodInfo methodInfo,Channel channel){
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
                //对比方法名
                if(declaredMethod.getName().equals(methodInfo.getMethodname())) {
                    //对比返回值类型
                    String string = declaredMethod.getReturnType().toString();
                    if (string.equals(methodInfo.getMethodReturnType())) {
                        Parameter[] parameters = declaredMethod.getParameters();
                        //对比参数长度
                        if (methodInfo.getParagrameinfoCount() == parameters.length) {
                            int flag = 0;
                            for (int i = 0; i < parameters.length; i++) {
                                MethodInfos.ParagramesInfo paragrameinfo = methodInfo.getParagrameinfo(i);
                                //对比参数类型，和参数名字。
                                if (!parameters[i].getName().equals(paragrameinfo.getParagrameName()) ||
                                        !parameters[i].getType().toString().equals(paragrameinfo.getParagrameType())) {
                                    flag = 1;
                                }
                            }
                            if (flag == 0) {
                                method = declaredMethod;
                                break;
                            }
                        }
                    }
                }
            }
            if(method != null){
                //获取参数，获取传递过来的参数值
                int count = methodInfo.getParagrameinfoCount();
                Object[] objects = new Object[count];
                for(int i=0;i<count;i++){
                    objects[i] = methodInfo.getParagrameinfo(i);
                }
                try {
                    //执行方法
                    Object object = method.invoke(o,objects);
                    //将返回值传递给response,在封装成mymessage
                    MethodInfos.Response response = MethodInfos.Response.newBuilder().setValue(object.toString()).build();
                    MethodInfos.MyMessage message = MethodInfos.MyMessage.newBuilder()
                                                    .setResponse(response).build();
                    //将mymessage传递给远程。
                    channel.writeAndFlush(message);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
