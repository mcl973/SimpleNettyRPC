/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: HandleRouteMapping
 * Author:   Administrator
 * Date:     2020/02/19 16:02
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package ScannerAndInstance.HandleRPCMappEvent;

import Annotation_Collection.MethodRpc.MethodRPC;
import MethodMessage.MethodInfos;
import ScannerAndInstance.Instance.JiexiAnnotation.JieXiMethodRPC;
import ScannerAndInstance.Instance.JiexiAnnotation.JieXiRouteMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/02/19
 * @since 1.0.0
 */
public class HandleRPCMapping extends AbstractHandleRPCmapping {
    @Override
    void instanceRoutemapping() {
        //其实应该分的再细一点
        //如Dao、DaoSql一个map，Service一个map，Controller和路径映射一个map
        //最后将这些都放入一个大的map里，或是不放置。
        //但是这样的话就不能够在注入的时候区分哪些东西在哪里
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
