# SimpleNettyRPC
    一个基于netty和我之前的写的后台框架中的ioc、di、aop以及后添加的rpc方法扫描模块
      https://github.com/mcl973/-web-Dao-Service-Controller-  这个就是之前的简单的后台框架
    
    首先创建了一个protobuf：
        syntax = "proto3";

        package MethodMessage;
        option java_package = "MethodMessage";
        option java_outer_classname = "MethodInfos";

        message MyMessage{
             enum MyMessageType {
                  MethodInfoType = 0;
                  responseType = 1;
             }

             MyMessageType mymessagetype = 1;
             //只能传递下面中的一个
             oneof messagetype{
                  MethodInfo  methodinfo = 2;
                  Response response = 3;
             }
        }
        message MethodInfo {
             string classname = 1;
             string methodname = 2;
             string methodReturnType = 3;
             //起到类似于列表的作用
             repeated ParagramesInfo paragrameinfo = 4;
        }
        message ParagramesInfo{
             string paragrameName = 1;
             string paragrameType = 2;
             string paragramevalue = 3;
        }

        message Response{
             string value = 1;
        }
    使用protoc生成java代码：
        protoc --proto_path=E:\java\Netty_RPC\src\main\java\protobuf --java_out=E:\java\Netty_RPC\src\main\java\ E:\java\Ne
tty_RPC\src\main\java\protobuf\protobuf_Request
#
     ioc、aop、di的部分我就不讲了，着重的讲一下关于rpc函数的扫描：
     首先使用自定义注解@Compolent("ExampleImpl")，像这样来标注一个类，记得要传递类名
     其次使用自定义注解@MethodRPC("test")，来修饰要被远程调用的函数，记得要传递方法名
     下面来看一下rpc的扫描部分：在ScannerAndInstance.HandleRPCMappEvent.HandleRPCMapping.java这里
          public class HandleRPCMapping extends AbstractHandleRPCmapping {
              @Override
              void instanceRoutemapping() {
                  Map<String, MethodInfos.MethodInfo> method_Object = new HashMap<>();

                  for(Map.Entry<String ,Object> map:iocmap.entrySet()){
                      Object value = map.getValue();
                      Class<?> aClass = value.getClass();
                      Method[] declaredMethods = aClass.getDeclaredMethods();
                      for (Method declaredMethod : declaredMethods) {
                          if (declaredMethod.isAnnotationPresent(MethodRPC.class)) {
                              //将这个函数的信息封装成protobuf类型的格式
                              JieXiMethodRPC jieXiMethodRPC = new JieXiMethodRPC();
                              //先获取方法上的名字
                              String s = jieXiMethodRPC.JiexiAnnotation(declaredMethod);
                              MethodInfos.ParagramesInfo.Builder builder = MethodInfos.ParagramesInfo.newBuilder();
                              Parameter[] parameters = declaredMethod.getParameters();
                              MethodInfos.ParagramesInfo build1 = null;
                              if(parameters.length>0) {
                                  //获取参数
                                  for (Parameter parameter : parameters) {
                                      builder.setParagrameName(parameter.getName());
                                      builder.setParagrameType(parameter.getType().toString());
                                  }
                                  build1 = builder.build();
                                  //构建methidonfo
                              }
                              MethodInfos.MethodInfo build = null;
                              if(build1!=null) {
                                  build = MethodInfos.MethodInfo.newBuilder()
                                          //到时可以使用这个快速的调取iocmap中具体的对象实例
                                          .setClassname(map.getKey())
                                          .setMethodname(s)
                                          .setMethodReturnType(declaredMethod.getReturnType().toString())
                                          .addParagrameinfo(build1).build();
                              }else{
                                  build = MethodInfos.MethodInfo.newBuilder()
                                          //到时可以使用这个快速的调取iocmap中具体的对象实例
                                          .setClassname(map.getKey())
                                          .setMethodname(s)
                                          .setMethodReturnType(declaredMethod.getReturnType().toString()).build();
                              }
                              //装入map中
                              method_Object.put(declaredMethod.getName(), build);
                          }
                      }
                  }
                  for(Map.Entry<String , MethodInfos.MethodInfo> map:method_Object.entrySet()){
                      methodInfoMap.put(map.getKey(),map.getValue());
                  }
              }
          }
          通过上面的方法将函数的函数名、返回值、参数信息等等都装载到protobuf类中也就是MethodInfo中。
          在将其放入最后的methodInfoMap中，以供其他的函数调用。
          
          
          1.在netty的server端，当client刚注册时就将本地的所有的rpc方法发送给client。
          2.client再根据自己需要的来调用具体的函数。
          3.在处理中使用了enum创建的单例的线程池来处理具体的函数调用事件。
          4.事件处理完成后直接调用对应的channel将数据返回给client。
          下面是server的handler的处理程序：
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
                          //其实这里可以适应方法的hashcode来作为key，具体的method作为value，返回给client的可以是这个
                          //调用的时候直接返回hashcode、和具体的参数的值。
                          //这样会简便一点
                          //这个就作为后续的改进吧
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
          
          
          客户端程序：
            
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
    
    
    效果：
            ![https://github.com/mcl973/SimpleNettyRPC/blob/master/%E5%AE%A2%E6%88%B7%E7%AB%AFshow.png]
            
            ![https://github.com/mcl973/SimpleNettyRPC/blob/master/%E6%9C%8D%E5%8A%A1%E7%AB%AFshw.png]
            
        
