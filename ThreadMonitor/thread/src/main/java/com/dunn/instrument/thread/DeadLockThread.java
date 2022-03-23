package com.dunn.instrument.thread;

public class DeadLockThread {
    public int curThreadId;
    public int blockThreadId;
    public Thread thread;

    public DeadLockThread(int curThreadId, int blockThreadId, Thread thread) {
        this.curThreadId = curThreadId;
        this.blockThreadId = blockThreadId;
        this.thread = thread;
    }
}
