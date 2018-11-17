package com.lofiwang.ipcsrv.thread;

import android.os.Handler;
import android.os.HandlerThread;

public class ExecutorThread extends HandlerThread {
    private static final String TAG = "ExecutorThread";

    private static ExecutorThread sInstance;
    private static Handler sHandler;

    private ExecutorThread() {
        this("ExecutorThread", android.os.Process.THREAD_PRIORITY_DEFAULT);
    }

    private ExecutorThread(String name, int priority) {
        super(name, priority);

    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new ExecutorThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static ExecutorThread get() {
        synchronized (ExecutorThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (ExecutorThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
}
