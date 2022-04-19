package com.dunn.instrument.threadhook;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;

/**
 * @ClassName: ThreadMethodHook
 * @Author: ZhuYiDian
 * @CreateDate: 2022/4/19 16:21
 * @Description:
 */
public class ThreadMethodHook extends XC_MethodHook {
    private static final String TAG = "ThreadMethodHook";

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i(TAG, "beforeHookedMethod: thread:" + t + ", started..");
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Thread t = (Thread) param.thisObject;
        Log.i(TAG, "afterHookedMethod: thread:" + t + ", exit..");
    }
}
