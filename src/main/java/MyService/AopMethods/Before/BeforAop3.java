<<<<<<< HEAD
/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: BeforAop3
 * Author:   Administrator
 * Date:     2020/04/03 19:22
 * Description: 安全操作
 * History:
 * <author>          <time>          <version>          <desc>
 */
package MyService.AopMethods.Before;

import MyService.AopMethods.BaseInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 〈一句话功能简述〉<br> 
 * 〈安全操作〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
=======
>>>>>>> origin/master
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
