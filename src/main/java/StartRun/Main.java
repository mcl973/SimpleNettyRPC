/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: Main
 * Author:   Administrator
 * Date:     2020/04/03 16:36
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package StartRun;

import NettyServer.Service;
import ScannerAndInstance.Dispatch;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/03
 * @since 1.0.0
 */
public class Main {
    public static void main(String[] args) {
        new Dispatch();
        Service service = new Service();
        service.start();
    }
}
