package com.dunn.instrument.thread;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static Object getFiledObject(Object obj, String filedName) {
        try{
            Field field = getDeclaredField(obj, filedName);
            return field.get(obj);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Field getDeclaredField(Object obj, String filedName) {
        Class<?> clazz = obj.getClass();
        while (clazz != Object.class){
            try{
                Field field = clazz.getDeclaredField(filedName);
                field.setAccessible(true);
                return field;
            }catch (Exception e){
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
