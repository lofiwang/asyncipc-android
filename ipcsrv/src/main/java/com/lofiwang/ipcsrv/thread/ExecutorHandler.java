package com.lofiwang.ipcsrv.thread;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorHandler extends Handler {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 3;
    private static final long KEEP_ALIVE_TIME = 30;
    private static ExecutorService sExecutorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
            new ThreadPoolExecutor.DiscardPolicy()
    );

    public ExecutorHandler() {
        super(ExecutorThread.get().getLooper());
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            Message msgCopy = Message.obtain(msg);
            Task task = new Task(msgCopy) {
                @Override
                public void run() {
                    onHandleMessage(this.msg);
                }
            };
            if (sExecutorService.isShutdown()) {
                return;
            }
            sExecutorService.submit(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onHandleMessage(Message msg) {

    }

    //
    private static class Task implements Runnable {
        Message msg;

        Task(Message msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
        }
    }
}
