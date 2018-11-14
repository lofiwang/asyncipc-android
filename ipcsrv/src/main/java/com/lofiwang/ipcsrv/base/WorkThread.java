package com.lofiwang.ipcsrv.base;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by chunsheng.wang on 2018/11/13.
 */

public class WorkThread extends HandlerThread {
    private static WorkThread sInstance;
    private static Handler sHandler;

    private WorkThread() {
        super("work_thread", android.os.Process.THREAD_PRIORITY_FOREGROUND);
    }

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new WorkThread();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static WorkThread get() {
        synchronized (WorkThread.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (WorkThread.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
}
