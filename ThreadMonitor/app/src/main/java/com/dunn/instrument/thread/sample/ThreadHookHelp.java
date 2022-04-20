package com.dunn.instrument.thread.sample;

import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dalvik.system.DexFile;
import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * @ClassName: ThreadHelp
 * @Author: ZhuYiDian
 * @CreateDate: 2022/3/24 3:07
 * @Description:
 */
public class ThreadHookHelp {
    private static final String TAG = "ThreadHookHelp";
    private static int mThreadCount = 0;

    /**
     * 监听线程创建
     * 使用时机：App开始的时候就去监听
     * 原理：Hook Thread的构造函数
     */
    public static void monitorThreadCreate() {
        try {
            DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Log.i(TAG, "monitorThreadCreate: ----------------线程创建监控----start-----------------------");
                    mThreadCount++;
                    //当前thread
                    Thread thread = (Thread) param.thisObject;
                    Log.i(TAG, "monitorThreadCreate: thread name=" + thread.getName() + ", stack=" + Log.getStackTraceString(new Throwable()));

                    Class<?> clazz = thread.getClass();
                    if (clazz != Thread.class) {
                        Log.d(TAG, "monitorThreadCreate: found class extend Thread=" + clazz);
                        DexposedBridge.findAndHookMethod(clazz, "run", new ThreadMethodHook());
                    }
                    Log.d(TAG, "monitorThreadCreate: Thread=" + thread.getName() + ", class=" + thread.getClass() + ", is created..., mThreadCount="+mThreadCount);
                    Log.i(TAG, "monitorThreadCreate: ----------------线程创建监控----end-----------------------\n");
                }
            });
            DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "monitorThreadCreate: e=" + e);
        }
    }

    public static void monitorDexLoad(){
        try {
            DexposedBridge.findAndHookMethod(DexFile.class, "loadDex", String.class, String.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    String dex = (String) param.args[0];
                    String odex = (String) param.args[1];
                    Log.i(TAG, "monitorDexLoad: load dex, input:" + dex + ", output:" + odex);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "monitorDexLoad: e=" + e);
        }
    }
}
