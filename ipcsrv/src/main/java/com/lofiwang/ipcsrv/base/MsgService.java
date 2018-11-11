package com.cws.nps.common.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * Created by chunsheng.wang on 2018/7/11.
 */

public abstract class MsgService extends Service {
    private static final String TAG = "MsgService";
    private static boolean DBG = false;
    private String mName;
    private volatile Looper mServiceLooper;
    private Messenger mMessenger;
    private ServiceHandler mServiceHandler;

    public final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msgFromClient) {
            try {
                if (!this.getLooper().getThread().isAlive()) {
                    return;
                }
                onHandleMessage(msgFromClient.replyTo, msgFromClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public MsgService(String name) {
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
        mServiceHandler = new ServiceHandler(mServiceLooper);
        mMessenger = new Messenger(mServiceHandler);
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
        try {
            onDestroyed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mServiceLooper.quitSafely();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mMessenger == null) {
            return null;
        }
        return mMessenger.getBinder();
    }

    protected final MsgService setDebug(boolean debug) {
        DBG = debug;
        return this;
    }

    public ServiceHandler getServiceHandler() {
        return mServiceHandler;
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

    protected abstract void onHandleMessage(Messenger clientMessenger, Message msgFromClient);

    protected abstract void onCreated();

    protected abstract void onDestroyed();
}
