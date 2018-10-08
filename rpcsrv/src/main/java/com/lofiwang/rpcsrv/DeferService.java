package com.lofiwang.rpcsrv;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by chunsheng.wang on 2018/7/11.
 */

public abstract class DeferService extends Service {
    private static final String TAG = "DeferService";
    private static boolean DBG = false;
    private static final int MSG_WHAT_ASYNC_HANDLE_MESSAGE = 0;
    private static final int MSG_WHAT_DEFER_STOP_SELF = 1;
    private static long TIME_DEFER_STOP_SELF = 15 * 60 * 1000;//15min
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private String mName;
    private boolean mRedelivery;
    private int mStartId = 0;

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_ASYNC_HANDLE_MESSAGE:
                    mStartId = msg.arg1;
                    try {
                        onHandleIntent((Intent) msg.obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (hasMessages(MSG_WHAT_DEFER_STOP_SELF)) {
                        removeMessages(MSG_WHAT_DEFER_STOP_SELF);
                    }
                    sendEmptyMessageDelayed(MSG_WHAT_DEFER_STOP_SELF, TIME_DEFER_STOP_SELF);
                    break;
                case MSG_WHAT_DEFER_STOP_SELF:
                    if (DBG) {
                        Log.d(TAG, "stopSelf - stop id:" + mStartId);
                    }
                    stopSelf(mStartId);
                    break;
                default:
                    break;
            }
        }
    }


    public DeferService(String name) {
        super();
        mName = name;
    }

    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        // TODO: It would be nice to have an option to hold a partial wakelock
        // during processing, and to have a static startService(Context, Intent)
        // method that would launch the service & hand off a wakelock.

        super.onCreate();
        HandlerThread thread = new HandlerThread("DeferService[" + mName + "]");
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
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
    public void onStart(Intent intent, int startId) {
        Message msg = mServiceHandler.obtainMessage(MSG_WHAT_ASYNC_HANDLE_MESSAGE);
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DBG) {
            Log.d(TAG, "onStartCommand - start id:" + startId);
        }
        onStart(intent, startId);
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DBG) {
            Log.d(TAG, "onDestroy");
        }
        mServiceLooper.quit();
        try {
            onDestroyed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public final DeferService setTimeDeferStopSelf(long time) {
        TIME_DEFER_STOP_SELF = time;
        return this;
    }

    protected final DeferService setDebug(boolean debug) {
        DBG = debug;
        return this;
    }

    protected abstract void onHandleIntent(Intent intent);

    protected abstract void onCreated();

    protected abstract void onDestroyed();
}
