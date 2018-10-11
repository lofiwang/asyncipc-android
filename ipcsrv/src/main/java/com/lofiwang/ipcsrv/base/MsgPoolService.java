package com.lofiwang.ipcsrv.base;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chunsheng.wang on 2018/7/11.
 */

public abstract class MsgPoolService extends Service {
    private static final String TAG = "MsgService";
    private static boolean DBG = false;
    private String mName;
    private volatile Looper mServiceLooper;
    private Messenger mMessenger;

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 3;
    private static final long KEEP_ALIVE_TIME = 30;
    private ExecutorService mExecutorService;

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msgFromClient) {
            try {
                Message msg = Message.obtain(msgFromClient);
                Task task = new Task(msg.replyTo, msg) {
                    @Override
                    public void run() {
                        super.run();
                        onHandleMessage(this.mgr, this.msg);
                    }
                };
                mExecutorService.submit(task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MsgPoolService(String name) {
        super();
        this.mName = name;
    }

    @Override
    public void onCreate() {
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.
        super.onCreate();
        HandlerThread thread = new HandlerThread(TAG + "[" + mName + "]");
        thread.start();
        mServiceLooper = thread.getLooper();
        ServiceHandler serviceHandler = new ServiceHandler(mServiceLooper);
        mMessenger = new Messenger(serviceHandler);

        mExecutorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
                new ThreadPoolExecutor.DiscardPolicy()
        );
        if (DBG) {
            Log.d(TAG, "onCreate");
        }
        try {
            onCreated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (DBG) {
            Log.d(TAG, "onDestroy");
        }
        mServiceLooper.quit();
        mExecutorService.shutdownNow();
        try {
            onDestroyed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mMessenger == null) {
            return null;
        }
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    protected final MsgPoolService setDebug(boolean debug) {
        DBG = debug;
        return this;
    }

    public boolean sendToClient(Messenger clientMessenger, Message msgToClient) {
        boolean sent;
        if (clientMessenger == null || msgToClient == null) {
            Log.e(TAG, "messenger or msg is null.");
            sent = false;
        } else {
            try {
                clientMessenger.send(msgToClient);
                sent = true;
            } catch (Exception e) {
                e.printStackTrace();
                sent = false;
            }
        }
        return sent;
    }

    public class Task implements Runnable {
        Messenger mgr;
        Message msg;

        Task(Messenger clientMessenger, Message msgFromClient) {
            mgr = clientMessenger;
            msg = msgFromClient;
        }

        @Override
        public void run() {
        }
    }

    protected abstract void onHandleMessage(Messenger clientMessenger, Message msgFromClient);

    protected abstract void onCreated();

    protected abstract void onDestroyed();
}
