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

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setOnClickListener(this);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        ThreadHelp.monitorAllThread();
    }

    private void sleep_(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
