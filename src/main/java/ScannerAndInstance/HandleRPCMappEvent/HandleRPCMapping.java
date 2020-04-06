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
import MethodMessage.MethodInfoses;

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
        Map<String, MethodInfoses.MethodInfoes> method_Object = new HashMap<>();

        for(Map.Entry<String ,Object> map:iocmap.entrySet()){
            Object value = map.getValue();
            Class<?> aClass = value.getClass();
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.isAnnotationPresent(MethodRPC.class)) {
                    getmethodinfo(map.getKey(),declaredMethod,method_Object);
                }
            }
        }
        for(Map.Entry<String , MethodInfoses.MethodInfoes> map:method_Object.entrySet()){
            methodInfoMap.put(map.getKey(),map.getValue());
        }
    }
    public void getmethodinfo(String classname, Method declaredMethod, Map<String, MethodInfoses.MethodInfoes> method_Object){
        Parameter[] parameters = declaredMethod.getParameters();
        /*
            1.获取classname
            2.获取method的hashcode
            3.获取参数名和类型
         */
        //获取classname和method的hashcode
        MethodInfoses.MethodInfoes.Builder builder = MethodInfoses.MethodInfoes.newBuilder()
                .setClassname(classname)
                .setMethodhashcode(declaredMethod.hashCode());
        //提恩建每一个参数的信息：参数名、参数类型
        for (Parameter parameter : parameters) {
            MethodInfoses.paragrameTypeAndName paragrameTypeAndName = MethodInfoses.paragrameTypeAndName
                    .newBuilder().setParagrameName(parameter.getName())
                    .setParagrameType(parameter.getType().toString()).build();
            MethodInfoses.ParagramesInfoes paragramesInfoes = MethodInfoses.ParagramesInfoes.newBuilder()
                    .setPtn(paragrameTypeAndName).build();
            builder.addParagrameinfo(paragramesInfoes);
        }
        //        //装入map中
        method_Object.put(declaredMethod.getName(), builder.build());
    }
}
