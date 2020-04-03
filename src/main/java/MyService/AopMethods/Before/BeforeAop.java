/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: BeforeAop
 * Author:   Administrator
 * Date:     2020/02/21 14:55
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package MyService.AopMethods.Before;

import MyService.AopMethods.BaseInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/02/21
 * @since 1.0.0
 */
public class BeforeAop implements BaseInterface {
    @Override
    public Object Excute(Method method, Object object, Object[] objects) {
        Object object1 = null;
        try {
            before();
            object1 = method.invoke(object,objects);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return object1;
    }
    public void  before(){
        System.out.println("this is before");
    }
}
