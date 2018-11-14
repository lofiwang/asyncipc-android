package com.lofiwang.ipcsrv.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chunsheng.wang on 2018/11/12.
 */

abstract public class ThreadHandler {
    protected String TAG = this.getClass().getSimpleName();
    protected boolean DBG = false;
    private static volatile Looper sServiceLooper;
    private volatile ServiceHandler mServiceHandler;

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

    public ThreadHandler(String name) {
        if (sServiceLooper == null) {
            HandlerThread thread = new HandlerThread("ThreadHandler[" + name + "]");
            thread.start();
            sServiceLooper = thread.getLooper();
        }
        mServiceHandler = new ServiceHandler(sServiceLooper);
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                onCreated();
            }
        });
    }

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Message msgCopy = Message.obtain(msg);
                Task task = new Task(msgCopy) {
                    @Override
                    public void run() {
                        super.run();
                        onHandleMessage(this.msg);
                    }
                };
                sExecutorService.submit(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Task implements Runnable {
        Message msg;

        Task(Message msgFromClient) {
            msg = msgFromClient;
        }

        @Override
        public void run() {
        }
    }

    protected Handler getServiceHandler() {
        return mServiceHandler;
    }

    protected abstract void onHandleMessage(Message msg);

    protected abstract void onCreated();

}
