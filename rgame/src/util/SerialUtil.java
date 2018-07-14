package util;

import java.io.*;

/**
 * @author sunfengmao
 * @Date 2018/6/13
 * 序列化编解码的工具类
 */
public class SerialUtil {

    /**
     * 将指定对象编码成二进制数组
     * @param o
     * @return
     */
    public static byte[] encode(Object o){
        if(null == o){
            throw new NullPointerException("Can't encode, object = null!");
        }
        byte[] data = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
            data = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 将指定二进制数组解码
     * @param data
     * @return
     */
    public static Object decode(byte[] data){
        if(null == data){
            throw new NullPointerException("Can't decode, data = null!");
        }
        Object o = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            try {
                o = ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return o;
    }

}
