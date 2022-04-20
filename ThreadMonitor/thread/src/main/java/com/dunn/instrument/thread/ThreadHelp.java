package com.dunn.instrument.thread;

import android.os.Build;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: ThreadHelp
 * @Author: ZhuYiDian
 * @CreateDate: 2022/3/24 3:07
 * @Description:
 */
public class ThreadHelp {
    private static final String TAG = "ThreadHelp";
    private static Map<Integer, DeadLockThread> deadLocks = new HashMap<>();

    /**
     * 初始化
     * 使用时机：App开始的时候就去初始化
     */
    public static void monitorThreadInit(){
        int result = NativeThreadMonitor.nativeInit(Build.VERSION.SDK_INT);
        Log.i(TAG,"monitorThreadInit: nativeInit -> "+result);
        result = NativeThreadMonitor.monitorThread();
        Log.i(TAG,"monitorThreadInit: monitorThread -> "+result);
    }

    /**
     * 开始监控所有线程
     * 使用时机：找合适的时机去监控线程
     * 原理：
     *  1,获取所有的线程(Native 闪退的代码有)
     *  2,对 BOLCKED 的线程获取锁信息
     */
    public static void monitorAllThread(){
        Log.i(TAG, "monitorAllThread: ----------------线程死锁监控----start-----------------------");
        // 1. 获取所有的线程，Native 闪退的代码有
        Set<Thread> allThreads = NativeThreadMonitor.getAllThread();
        // 2. 对 BOLCKED 的线程获取锁信息
        for (Thread thread : allThreads) {
            if (thread.getState() == Thread.State.BLOCKED) {
                Log.i(TAG, "monitorAllThread: thread -> " + thread.getName());
                // 当前线程在竞争锁，拿到 native thread 地址
                long threadNativeAddress = (long) ReflectUtils.getFiledObject(thread, "nativePeer");
                if (threadNativeAddress == 0) {
                    continue;
                }
                // 获取到锁信息, 当前竞争锁，但是锁被其他线程占用了
                int blockThreadId = NativeThreadMonitor.getContentThreadId(threadNativeAddress);
                int currentThreadId = NativeThreadMonitor.getCurrentThreadId(threadNativeAddress, Build.VERSION.SDK_INT);
                Log.i(TAG, "monitorAllThread: blockThreadId -> " + blockThreadId + " , currentThreadId -> " + currentThreadId);
                deadLocks.put(currentThreadId, new DeadLockThread(currentThreadId, blockThreadId, thread));
            }
        }
        // 3. 分析和输出所有线程的死锁信息
        // 将所有的情况进行分组
        ArrayList<HashMap<Integer, Thread>> deadLockThreadGroup = deadLockThreadGroup();
        // 所有的死锁信息输出
        for (HashMap<Integer, Thread> group : deadLockThreadGroup) {
            for (Integer curId : group.keySet()) {
                DeadLockThread deadLockThread = deadLocks.get(curId);
                if (deadLockThread == null) {
                    continue;
                }
                Thread waitThread = group.get(deadLockThread.blockThreadId);
                if (waitThread == null) {
                    continue;
                }
                Thread deadThread = group.get(deadLockThread.curThreadId);

                Log.i(TAG,"monitorAllThread: thread_name = "+deadThread.getName());
                Log.i(TAG,"monitorAllThread: wait_name = "+waitThread.getName());

                StackTraceElement[] stackTraceElements = deadThread.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTraceElements) {
                    Log.i(TAG, stackTraceElement.toString());
                }
            }
        }
        Log.i(TAG, "monitorAllThread: ----------------线程死锁监控----end-----------------------\n");
    }

    private static ArrayList<HashMap<Integer, Thread>> deadLockThreadGroup() {
        HashSet<Integer> traversalThreadIds = new HashSet<>();
        ArrayList<HashMap<Integer, Thread>> lockThreadGroups = new ArrayList<>();
        for (Integer currentThreadId : deadLocks.keySet()) {
            if (traversalThreadIds.contains(currentThreadId)) {
                continue;
            }

            HashMap<Integer, Thread> deadLockGroup = findDeadLockLink(currentThreadId, new HashMap<Integer, Thread>());
            traversalThreadIds.addAll(deadLockGroup.keySet());
            lockThreadGroups.add(deadLockGroup);
        }
        return lockThreadGroups;
    }

    private static HashMap<Integer, Thread> findDeadLockLink(Integer currentThreadId, HashMap<Integer, Thread> group) {
        DeadLockThread deadLockThread = deadLocks.get(currentThreadId);
        if (currentThreadId == 0 || deadLockThread == null) {
            return new HashMap<>();
        }
        if (group.containsKey(currentThreadId)) {
            return group;
        }
        group.put(currentThreadId, deadLockThread.thread);
        return findDeadLockLink(deadLockThread.blockThreadId, group);
    }
}
