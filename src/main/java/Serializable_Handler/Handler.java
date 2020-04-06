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

import MethodMessage.MethodInfoses;
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
        if (byteString.size() != 0) {
            byte[] bytes = byteString.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                object = objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return object;
    }
    /*
        import java.lang.Integer;
        import java.lang.Double;
        import java.lang.Float;
        import java.lang.Long;
        import java.lang.Boolean;
        import java.lang.Byte;
        import java.lang.Character;
        import java.lang.Short;
        import java.lang.String;
     */
    public Object getreturnType(String typename,Object value){
        switch (typename){
            case "java.lang.Integer":
                return (int)value;
            case "import java.lang.Double":
                return (double)value;
            case "java.lang.Float":
                return (float)value;
            case "java.lang.Long":
                return (long)value;
            case "java.lang.Boolean":
                return (boolean)value;
            case "java.lang.Byte":
                return (byte)value;
            case "java.lang.Character":
                return (char)value;
            case "java.lang.Short":
                return (short)value;
            case "java.lang.String":
                return (String)value;
            default:return null;
        }
    }
}
