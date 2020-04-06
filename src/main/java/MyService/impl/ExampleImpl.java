/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: ExampleImpl
 * Author:   Administrator
 * Date:     2020/04/03 17:39
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package MyService.impl;

import Annotation_Collection.Aop.Before;
import Annotation_Collection.MethodRpc.MethodRPC;
import Annotation_Collection.NormalBean.Compolent;
import MyService.Interface.Example;
import StartRun.test;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
@Compolent("ExampleImpl")
public class ExampleImpl implements Example {

    @MethodRPC("test")
    @Before("MyService.AopMethods.Before.BeforAop3")
    @Override
    public test test(String name,int age) {
        System.out.println("this is test");
        System.out.println(name+"   "+age);
        test t = new test("mcl",26);
        return t;
    }
    @MethodRPC("getresult")
    @Override
    public long getresult() {
        return 1000L;
    }
}
