/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: test
 * Author:   Administrator
 * Date:     2020/04/06 17:44
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package StartRun;

import java.io.Serializable;

/**
 * 〈一句话功能简述〉<br> 
 * 〈测试用〉
 *
 * @author Administrator
 * @create 2020/04/06
 * @since 1.0.0
 */
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
