# SimpleNettyRPC
      详细介绍：
            https://blog.csdn.net/qq_30761967/article/details/105361477
      更新：可以调用任意方法，在protobuf方法中添加了byte属性，使得函数中才能够可以表示object类型的变量。
      用之前的函数名+参数类型的匹配可以确定一个函数，替代为适应hashcode来匹配唯一的函数。
            修改的protobuf如下：
            protobuf_Requests：
                  syntax = "proto3";
                  package MethodMessage;
                  option java_package = "MethodMessage";
                  option java_outer_classname = "MethodInfoses";

                  message MyMessages{
                       enum MyMessageType {
                            MethodInfoType = 0;
                            responseType = 1;
                       }
                       MyMessageType mymessagetype = 1;

                       oneof messagetype{
                            MethodInfoes  methodinfo = 2;
                            Responses response = 3;
                       }
                  }
                  message MethodInfoes {
                       string classname = 1;
                       int32 methodhashcode = 2;
                       repeated ParagramesInfoes paragrameinfo = 3;
                  }
                  message ParagramesInfoes{
                       paragrameTypeAndName ptn = 1;
                       bytes paragramevalue = 2;
                  }
                  message paragrameTypeAndName{
                      string paragrameName = 1;
                      string paragrameType = 2;
                  }
                  message Responses{
                       bytes responsevalue = 1;
                  }
            在客户端简单的测试了下功能，可以实现正常的调用，并且可以返回数据。
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
                                    //遍历远程函数调用的列表，这里可以是set而不是map
                                  for(Map.Entry<MethodInfoses.MethodInfoes,Object> nexts:set.entrySet()){
                                          MethodInfoses.MethodInfoes next = nexts.getKey();
                                          if(next.getParagrameinfoCount() == 0) {
                                              if (next != null) {
                                                  Object[] object = new Object[next.getParagrameinfoCount()];
                                                  //这里根据具体的函数来填写相应的参数，这个需要人工参与，应为参数不可能
                                                  //由程序帮你填写。
                                                  //我这里调用的是一个无参的函数。
                  //                                object[0] = "mcl";
                  //                                object[1] = 10;
                                                  //得到序列化后的 MethodInfoes   
                                                  MethodInfoses.MethodInfoes excute = excute(next, object); 
                                                  //将其封装为MyMessages格式
                                                  MethodInfoses.MyMessages build = MethodInfoses.MyMessages.newBuilder().setMethodinfo(excute).build();
                                                  //发送
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
       client短的对于object类型的返回值的处理：
       //获取函数执行返回的值
                Object object = handler.getObject(response.getResponsevalue());
                if (object != null) {
                    Class<?> aClass = object.getClass();
                    String typeName = aClass.getTypeName();
                    //判断是不是八大基本类型+String
                    Object o = handler.getreturnType(typeName, object);
                    if (o == null) {//不是八大基本类型和String类型
                        //一般情况下都是pojo类型的，如果是需要调用object里的参数的话，可以使用method的invoke来调用。
                        //获取所有的属性
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
         对于服务器端的处理object的做法是：
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
                    //由于是参数类型，在执行method.invoke的时候传进去的都是obejct[]所以在这里根本不需要判断器类型，由java本身去做判断
                    //public Object invoke(Object obj, Object... args)，所以这里直接将obejct赋值给obejct[i]
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
                          
      下一步的设想，应该将这个项目结构拆分的更加细化，如封装、序列化反序列化、io、处理事件（发送事件和接收事件）、ioc、di、aop、rpcmethodmapped。
      进一步的思想是，提供函数列表名和对应的hashcode，由用户自定义的去传递参数。
            还需要有一个专门的注册中心，目前的注册中心是有服务器端来扮演的，如client端一注册，服务器端就像其发送自己的可以被远程调用的函数封装。
            目前只有一个服务器，所以网关不需要，所以负载均衡自然也是不需要的。反向代理更不用说。
      
      
     
      
      1.在netty的server端，当client刚注册时就将本地的所有的rpc方法发送给client。
      2.client再根据自己需要的来调用具体的函数。
      3.在处理中使用了enum创建的单例的线程池来处理具体的函数调用事件。
      4.事件处理完成后直接调用对应的channel将数据返回给client。
      5.使用jdk的动态代理实现了rpc调用的线程安全性，代码如下：
            @Compolent("ExampleImpl")
            public class ExampleImpl implements Example {

                @MethodRPC("test")
                @Before("MyService.AopMethods.Before.BeforAop3")
                @Override
                public int test() {
                    System.out.println("this is test");
                    return 10;
                }
            }
            public class BeforAop3 implements BaseInterface {
                @Override
                public Object Excute(Method method, Object object, Object[] objects) {
                    return synchronizedMethod(method,object,objects);
                }
                public synchronized Object synchronizedMethod(Method method, Object object, Object[] objects){
                    try {
                        return method.invoke(object,objects);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                }
            }
            运行时期通过原本的匹配方式，会执行到BeforAop3这个函数上，可以看见只是在骑上封装了一个线程安全的函数，使用synchronized修饰。
      
      其中线程池是有一个枚举类型写成的单例获得的：
      public enum ThreadExcutors {
            INSTANCE(5,5,5,5);
            private ExecutorService executorService;
            ThreadExcutors(int coresize,int maxsize,int arrayblockingqueuesize,int waitTime){
                executorService = new ThreadPoolExecutor(coresize,maxsize,waitTime, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<Runnable>(arrayblockingqueuesize),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
            }
            public ExecutorService getExecutor(){
                return executorService;
            }
        }
        所以他是安全的。
      
     一个基于netty和我之前的写的后台框架中的ioc、di、aop以及后添加的rpc方法扫描模块
      https://github.com/mcl973/-web-Dao-Service-Controller-  这个就是之前的简单的后台框架
    
  
    使用protoc生成java代码：
        protoc --proto_path=E:\java\Netty_RPC\src\main\java\protobuf --java_out=E:\java\Netty_RPC\src\main\java\ E:\java\Netty_RPC\src\main\java\protobuf\protobuf_Requests
     handler的代码：（其实是序列化和反序列化的过程）
      public class Handler {
          public MethodInfoses.ParagramesInfoes getSerializableParagrame(Object object){
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
              ObjectOutputStream objectOutputStream = null;
              try {
                  objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                  objectOutputStream.writeObject(object);
              } catch (IOException e) {
                  e.printStackTrace();
              }
              byte[] bytes = byteArrayOutputStream.toByteArray();
              ByteString byteString = ByteString.copyFrom(bytes);
              return MethodInfoses.ParagramesInfoes.newBuilder().setParagramevalue(byteString).build();
          }

          public MethodInfoses.Responses getSerializableResponse(Object object){
              ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();//以字节数组的输出流作为ObjectOutputStream的参数
              ObjectOutputStream objectOutputStream = null;
              try {
                  objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                  objectOutputStream.writeObject(object);//将数据写入ByteArrayOutputStream
              } catch (IOException e) {
                  e.printStackTrace();
              }
              byte[] bytes = byteArrayOutputStream.toByteArray();//转化为字节数组
              ByteString byteString = ByteString.copyFrom(bytes);
              return MethodInfoses.Responses.newBuilder().setResponsevalue(byteString).build();
          }

          public Object getObject(ByteString byteString){//反序列化
              Object object = null;
              if (byteString.size() != 0) {
                  byte[] bytes = byteString.toByteArray();
                  ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);//读取数据
                  try {
                      ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);//作为参数传入
                      object = objectInputStream.readObject();//获取object
                  } catch (IOException | ClassNotFoundException e) {
                      e.printStackTrace();
                  }
              }
              return object;
          }
          /*
              import java.lang.Integer;
              import java.lang.Double;
              import java.lang.Float;
              import java.lang.Long;
              import java.lang.Boolean;
              import java.lang.Byte;
              import java.lang.Character;
              import java.lang.Short;
              import java.lang.String;
           */
          public Object getreturnType(String typename,Object value){//判断是否是基本类型和String
              switch (typename){
                  case "java.lang.Integer":
                      return (int)value;
                  case "import java.lang.Double":
                      return (double)value;
                  case "java.lang.Float":
                      return (float)value;
                  case "java.lang.Long":
                      return (long)value;
                  case "java.lang.Boolean":
                      return (boolean)value;
                  case "java.lang.Byte":
                      return (byte)value;
                  case "java.lang.Character":
                      return (char)value;
                  case "java.lang.Short":
                      return (short)value;
                  case "java.lang.String":
                      return (String)value;
                  default:return null;
              }
          }
      } 
     贴上客户端的代码：
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
                       //一下代码，可以改成列表形式提供给用户
                       /*以下代码可以封装成一个函数，函数的参数中包含一个函数的hashcode和参数列表，函数中通过hashcode拿取具体的函数
                         然后按照再将其封装成MethodInfoe形式。
                          如：
                          public void getresult(int hashcode,Object[] obejct,Channel channel){
                              MethodInfoses.MethodInfoes mehthod = map.get(hashcode);
                              MethodInfoses.MethodInfoes excute = excute(mehthod, object);
                              MethodInfoses.MyMessages build = MethodInfoses.MyMessages.newBuilder().setMethodinfo(excute).build();
                              channel.writeAndFlush(build);
                          }
                       */
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
            
     服务端的handler的代码：
     public class ServiceHandler extends SimpleChannelInboundHandler< MethodInfoses.MyMessages> {
          //获取具体可以被远程调用的方法
          private Map<String, MethodInfoses.MethodInfoes> map = AbstractBean.methodInfoMap;
          //获取ioc容器
          private Map<String,Object> iocmap = AbstractBean.iocmap;
          //获取单例的线程池
          private ExecutorService executorService = ThreadExcutors.INSTANCE.getExecutor();
          @Override
          public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
              executorService.execute(new sendRPCMethod(ctx.channel()));
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
          class sendRPCMethod extends Thread{
              Channel channel;
              public sendRPCMethod(Channel channel){
                  this.channel = channel;
              }

              @Override
              public void run() {
                  //这里需要需要调用的方法传输回去
                  for(Map.Entry<String, MethodInfoses.MethodInfoes> maps:map.entrySet()) {
                      MethodInfoses.MyMessages message = MethodInfoses.MyMessages.newBuilder().setMethodinfo(maps.getValue()).build();
                      channel.writeAndFlush(message);
                      try {
                          //这个算是一个bug，如果不加这一段的睡眠会是睡眠的时间不够那么就会导致client端出现错误
                          //可能的额情况是第二个函数拥有第一个函数的参数
                          //在client端使用锁也解决不了问题，最终只能归结为串扰，即认为两次数据发生了冲突了，因为在底层都是比特流
                          //在客户端解析出现错误，如果保留一段时间在发送就不会出现这样的问题。
                          //很奇怪的现象，如果有读者知道的可以修改下，忽视通知在下：邮箱：979377991@qq.com
                          Thread.sleep(100);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                  }
              }
          }
      }
      
      效果：客户端。
            /////////
            //////////////
            /////////   第一个函数的信息
            ExampleImpl
            1314143599
            arg0
            class java.lang.String
            arg1
            int

            //////////////  第二个函数的信息
            ExampleImpl
            765298446

            /////////
            //////////////  调用第二个函数的结果返回
            1000

            0
            
    第二个函数：
          @MethodRPC("getresult")  //被标识，在ioc阶段会将其独立的创建出来，放入单独的rpcMap中，以供后续程序的使用
          @Override
          public long getresult() {
              return 1000L;
          }
    
     执行第一个函数显示的是：
      /////////
      /////////
      //////////////
      ExampleImpl
      1314143599
      arg0
      class java.lang.String
      arg1
      int

      //////////////
      ExampleImpl
      765298446

      /////////
      //////////////返回的数据

      0
      age 26    
      name mcl
      
      第一个函数的代码：
          @MethodRPC("test")
          @Before("MyService.AopMethods.Before.BeforAop3")   //这个方法需要被增强
          @Override
          public test test(String name,int age) {
              System.out.println("this is test");
              System.out.println(name+"   "+age);
              test t = new test("mcl",26);
              return t;
          }
          //增强的方法，其实就是加了层synchronized，保证其其安全性
          public class BeforAop3 implements BaseInterface {
                @Override
                public Object Excute(Method method, Object object, Object[] objects) {
                    return synchronizedMethod(method,object,objects);
                }
                public synchronized Object synchronizedMethod(Method method, Object object, Object[] objects){
                    try {
                        return method.invoke(object,objects);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                }
            }
            //返回数据的类
            public class test implements Serializable {
                int age;
                String name;
                public test(String name,int age){
                    this.name = name;
                    this.age = age;
                }
                public int getAge() {
                    return age;
                }

                public void setAge(int age) {
                    this.age = age;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }
            }
            

   
      
      
      
      
