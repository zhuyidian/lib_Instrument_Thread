package com.dunn.instrument.thread.sample;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dunn.instrument.thread.DeadLockThread;
import com.dunn.instrument.thread.NativeThreadMonitor;
import com.dunn.instrument.thread.ReflectUtils;
import com.dunn.instrument.thread.ThreadHelp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity implements View.OnClickListener {
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ThreadHelp.monitorThreadInit();
//        int result = NativeThreadMonitor.nativeInit(Build.VERSION.SDK_INT);
//        Log.e("TAG","nativeInit -> "+result);
//        result = NativeThreadMonitor.monitorThread();
//        Log.e("TAG","monitorThread -> "+result);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setOnClickListener(this);

        // 1. ANR 监控轮询
        // 2. 获取到所有的线程
        // 3. 通过反射获取到 native thread 对象地址

        // 写一个简单的死锁
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();
                synchronized (lock1) {
                    sleep_(2);
                    synchronized (lock2) {
                        Log.e("TAG", "threa1");
                        // 发一个聊天消息，隐藏进度条等等
                    }
                }
            }
        };

        Thread thread2 = new Thread() {
            @Override
            public void run() {
                super.run();
                synchronized (lock2) {
                    sleep_(2);
                    synchronized (lock1) {
                        Log.e("TAG", "threa2");
                    }
                }
            }
        };

        thread1.setName("test-thread1");
        thread1.start();
        thread2.setName("test-thread2");
        thread2.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 做大量的计算，做大量的内存开辟，其他一些不应该的操作
            }
        },"thread3").start();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        ThreadHelp.monitorAllThread();

//        // 输出所有的死锁信息，也不是腾讯内部的方案，也有可能是
//        // 1. 获取所有的线程，Native 闪退的代码有
//        Set<Thread> allThreads = NativeThreadMonitor.getAllThread();
//        // 2. 对 BOLCKED 的线程获取锁信息
//        for (Thread thread : allThreads) {
//            if (thread.getState() == Thread.State.BLOCKED) {
//                Log.e("TAG", "thread -> " + thread.getName());
//                // 当前线程在竞争锁，拿到 native thread 地址
//                long threadNativeAddress = (long) ReflectUtils.getFiledObject(thread, "nativePeer");
//                if (threadNativeAddress == 0) {
//                    continue;
//                }
//                // 获取到锁信息, 当前竞争锁，但是锁被其他线程占用了
//                int blockThreadId = NativeThreadMonitor.getContentThreadId(threadNativeAddress);
//                int currentThreadId = NativeThreadMonitor.getCurrentThreadId(threadNativeAddress, Build.VERSION.SDK_INT);
//                Log.e("TAG", "blockThreadId -> " + blockThreadId + " , currentThreadId -> " + currentThreadId);
//                deadLocks.put(currentThreadId, new DeadLockThread(currentThreadId, blockThreadId, thread));
//            }
//        }
//        // 3. 分析和输出所有线程的死锁信息
//        // 讲所有的情况进行分组
//        ArrayList<HashMap<Integer, Thread>> deadLockThreadGroup = deadLockThreadGroup();
//        // 所有的死锁信息输出
//        for (HashMap<Integer, Thread> group : deadLockThreadGroup) {
//            for (Integer curId : group.keySet()) {
//                DeadLockThread deadLockThread = deadLocks.get(curId);
//                if (deadLockThread == null) {
//                    continue;
//                }
//                Thread waitThread = group.get(deadLockThread.blockThreadId);
//                if (waitThread == null) {
//                    continue;
//                }
//                Thread deadThread = group.get(deadLockThread.curThreadId);
//
//                Log.e("TAG","thread_name = "+deadThread.getName());
//                Log.e("TAG","wait_name = "+waitThread.getName());
//
//                StackTraceElement[] stackTraceElements = deadThread.getStackTrace();
//                for (StackTraceElement stackTraceElement : stackTraceElements) {
//                    Log.e("TAG", stackTraceElement.toString());
//                }
//            }
//        }
    }

    private void sleep_(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, DeadLockThread> deadLocks = new HashMap<>();

    private ArrayList<HashMap<Integer, Thread>> deadLockThreadGroup() {
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

    private HashMap<Integer, Thread> findDeadLockLink(Integer currentThreadId, HashMap<Integer, Thread> group) {
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
