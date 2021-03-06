/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: HandleAopBean
 * Author:   Administrator
 * Date:     2020/02/19 10:19
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package ScannerAndInstance.HanleAopEvent;
import Annotation_Collection.MethodRpc.MethodRPC;
import Annotation_Collection.RouteMap.RouteMapping;
import AutoJdk.JAutoAop;
import AutoJdk.MyInvokeHandler;
import MethodMessage.MethodInfoses;
import ScannerAndInstance.Instance.GetJieXi;
import ScannerAndInstance.Instance.JieXiAnnotationInterface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
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
public class HandleAopBean extends AbstractHandleAopBean {
    /*
        这里主要处理的就是ioc容器中函数元素上带有aop注解的方法的获取和代理。
        在这里有一个需要考虑的是如果需要被代理的类里包含自动注入，那么在代理之前就需要
        将值注入进去。
     */
    @Override
    void instanceaop() {
        //存放rpc函数信息的容器
        Map<String, MethodInfoses.MethodInfoes> map2 = new HashMap<>();
        Object value = null;
        for(Map.Entry<String,Object> map : iocmap.entrySet()){
            value = map.getValue();
            Class<?> aClass = value.getClass();
            //查看这个类的方法里是否包含了注解，如果包含了，那么就返回其值
            String result = isHasAopAnnotation(value.getClass().getMethods());
            ArrayList<Method> list = new ArrayList<>();
            //判断与没有需要被rpc的函数
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if(declaredMethod.isAnnotationPresent(MethodRPC.class)){
                    list.add(declaredMethod);
                }
            }

            //这一步很重要，如果没有这一步，候命是没有办法实现自动注入的，
            //所以需要在创建动态代理之前先将数据注入进去
            if (result!=null) {
                //判断其field元素是否需要自动注入
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    String s = isHasAutowrite(declaredField);
                    if (s != null) {
                        //给其权限
                        declaredField.setAccessible(true);
                        try {
                            //注入值
                            declaredField.set(value, iocmap.get(s));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //得到值，实例化,使用jdk的动态代理，原始的object有的只，在实现动态代理后还会继续保留其值
                //注入必须要在代理前，代理后的类不会有代理前的属性
                MyInvokeHandler myInvokeHandler = new MyInvokeHandler(value);
                Object newInstance = JAutoAop.createNewInstance(value, myInvokeHandler);
                Class<?> aClass1 = newInstance.getClass();
                map.setValue(newInstance);
                for (Method method : list) {
                    try {
                        Method declaredMethod = aClass1.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        getmethodinfo(map.getKey(),declaredMethod,map2);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //对于需要被aop的rpc函数，需要先一步的添加
        for(Map.Entry<String, MethodInfoses.MethodInfoes> maps:map2.entrySet()){
            methodInfoMap.put(maps.getKey(),maps.getValue());
        }
        map2 = null;
    }

    //是否包含了需要被动态代理的注解
    public String isHasAopAnnotation(Method[] methods){
        String result = null;
        for (Method method : methods) {
            //如果是RouteMapping跳过
            if (method.isAnnotationPresent(RouteMapping.class))
                continue;
            JieXiAnnotationInterface jieXi = GetJieXi.getJieXi(method);
            if (jieXi!=null) {
                result = jieXi.JiexiAnnotation(method);
                if (result != null)
                    return result;
            }
        }
        return result;
    }

    //是否是包含了自动注入的注解
    public String  isHasAutowrite(Field field){
        JieXiAnnotationInterface jieXi = GetJieXi.getJieXi(field);
        if (jieXi!=null)
            return jieXi.JiexiAnnotation(field);
        return null;
    }
    public void getmethodinfo(String classname, Method declaredMethod, Map<String, MethodInfoses.MethodInfoes> method_Object){
        Parameter[] parameters = declaredMethod.getParameters();
        /*
            1.获取classname
            2.获取method的hashcode
            3.获取参数名和类型
         */
        MethodInfoses.MethodInfoes.Builder builder = MethodInfoses.MethodInfoes.newBuilder()
                .setClassname(classname)
                .setMethodhashcode(declaredMethod.hashCode());
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
