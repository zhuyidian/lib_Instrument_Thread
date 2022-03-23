package com.dunn.instrument.thread;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NativeThreadMonitor {
    // Native 闪退的代码
    private static ThreadGroup systemThreadGroup;
    private static final String TAG = "NativeThreadMonitor";

    static {
        System.loadLibrary("native-lib");

        try {
            Class<?> threadGroupClass = Class.forName("java.lang.ThreadGroup");
            Field systemThreadGroupField = threadGroupClass.getDeclaredField("systemThreadGroup");
            systemThreadGroupField.setAccessible(true);
            systemThreadGroup = (ThreadGroup) systemThreadGroupField.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前所有线程
     */
    public static final Set<Thread> getAllThread(){
        return getAllStackTraces().keySet();
    }

    /**
     * 获取线程堆栈的map.
     *
     * @return 返回线程堆栈的map
     */
    private static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        if (systemThreadGroup == null) {
            return Thread.getAllStackTraces();
        } else {
            Map<Thread, StackTraceElement[]> map = new HashMap<>();

            // Find out how many live threads we have. Allocate a bit more
            // space than needed, in case new ones are just being created.
            int count = systemThreadGroup.activeCount();
            Thread[] threads = new Thread[count + count / 2];
            Log.d(TAG, "activeCount: " + count);

            // Enumerate the threads and collect the stacktraces.
            count = systemThreadGroup.enumerate(threads);
            for (int i = 0; i < count; i++) {
                try {
                    map.put(threads[i], threads[i].getStackTrace());
                } catch (Throwable e) {
                    Log.e(TAG, "fail threadName: " + threads[i].getName(), e);
                }
            }
            return map;
        }
    }

    public static native int stringFromJNI();

    public static native int getContentThreadId(long threadNativeAddress);

    public static native int nativeInit(int sdkVersion);

    public static native int getCurrentThreadId(long threadNativeAddress, int sdkVersion);

    public static native int monitorThread();
}
