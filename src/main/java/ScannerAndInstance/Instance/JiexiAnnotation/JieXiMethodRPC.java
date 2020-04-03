/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: JieXiMethodRPC
 * Author:   Administrator
 * Date:     2020/04/03 14:26
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package ScannerAndInstance.Instance.JiexiAnnotation;

import Annotation_Collection.MethodRpc.MethodRPC;
import ScannerAndInstance.Instance.JieXiAnnotationInterface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
public class JieXiMethodRPC implements JieXiAnnotationInterface {
    @Override
    public String JiexiAnnotation(Class clazz) {
        return null;
    }

    @Override
    public String JiexiAnnotation(Method method) {
        MethodRPC annotation = method.getAnnotation(MethodRPC.class);
        if(annotation!=null){
            String value = annotation.value();
            if(value == null || "".equals(value)){
                return null;
            }else return value;
        }
        return null;
    }

    @Override
    public String JiexiAnnotation(Field field) {
        return null;
    }

    @Override
    public String JiexiAnnotation(Parameter parameter) {
        return null;
    }
}
