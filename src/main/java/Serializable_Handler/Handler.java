/**
 * Copyright (C), 2015-2020, XXX有限公司
 * FileName: Handler
 * Author:   Administrator
 * Date:     2020/04/06 18:16
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 */
package Serializable_Handler;

import MethodMessage.MethodInfos;
import MethodMessage.MethodInfoses;
import StartRun.test;
import com.google.protobuf.ByteString;

import java.io.*;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2020/04/06
 * @since 1.0.0
 */
public class Handler {
    public MethodInfoses.ParagramesInfoes getSerializableParagrame(Object object){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        ByteString byteString = ByteString.copyFrom(bytes);
        return MethodInfoses.ParagramesInfoes.newBuilder().setParagramevalue(byteString).build();
    }

    public MethodInfoses.Responses getSerializableResponse(Object object){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        ByteString byteString = ByteString.copyFrom(bytes);
        return MethodInfoses.Responses.newBuilder().setResponsevalue(byteString).build();
    }

    public Object getObject(ByteString byteString){
        Object object = null;
        byte[] bytes = byteString.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            object = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }
}
