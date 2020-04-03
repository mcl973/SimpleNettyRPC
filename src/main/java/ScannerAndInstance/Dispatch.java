/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: Dispatch
 * Author:   Administrator
 * Date:     2020/02/19 12:09
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package ScannerAndInstance;

import ScannerAndInstance.HandleAutowriteEvent.HandleAutowrite;
import ScannerAndInstance.HandleRPCMappEvent.HandleRPCMapping;
import ScannerAndInstance.HanleAopEvent.HandleAopBean;
import ScannerAndInstance.Instance.InstanceBean;
import ScannerAndInstance.Scanner.Scanner;


/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/02/19
 * @since 1.0.0
 */
public class Dispatch {

    /*
        初始化--》 数据库连接、扫描、实例化、aop操作、自动注入、路径映射
     */
    public void init() {
        //扫描
        new Scanner();
        //实例化类
        new InstanceBean();
        //处理aop
        new HandleAopBean();
        //处理自动注入
        new HandleAutowrite();
        //处理路径和方法的映射
        new HandleRPCMapping();
    }

    public Dispatch(){
        init();
    }
}
