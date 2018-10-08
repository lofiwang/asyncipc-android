package com.lofiwang.rpcsrv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;

/**
 * Created by chunsheng.wang on 2018/5/24.
 */

public abstract class MsgClient {
    private static final String TAG = "MsgClient";
    private Context mContext;
    private String mName;
    private String mPkgName = null;
    private String mClassName = null;
    private Messenger mService;
    private boolean bindAction = false;//true bind,false unbind
    private boolean isConn = false;
    private static final int MSG_WHAT_REBIND = 1;
    private Handler mWorkHandler;

    public Handler getWorkHandler() {
        return mWorkHandler;
    }

    private final Handler mUiHdl = new UiHdl();

    private class UiHdl extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_WHAT_REBIND:
                    bindMsgService(mPkgName, mClassName);
                    break;
            }
        }
    }

    private Messenger mMessenger;
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConn = true;
            mService = new Messenger(service);
            try {
                onConnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!bindAction) {
                unbindMsgServiceImpl();
                return;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConn = false;
            try {
                onDisconnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mService = null;
            if (bindAction) {
                mUiHdl.sendEmptyMessageDelayed(MSG_WHAT_REBIND, 10000);
            }
        }
    };

    public MsgClient(Context context, String name) {
        mContext = context;
        mName = name;
        HandlerThread workThread = new HandlerThread(TAG + "[" + mName + "]");
        workThread.start();
        mWorkHandler = new Handler(workThread.getLooper()) {
            @Override
            public void handleMessage(Message msgFromServer) {
                try {
                    onHandleMessage(msgFromServer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mMessenger = new Messenger(mWorkHandler);
    }

    public void bindMsgService(String pkgName, String className) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(className)) {
            throw new IllegalArgumentException("pkg & class is null");
        }
        bindAction = true;
        mPkgName = pkgName;
        mClassName = className;
        Intent intent = new Intent();
        intent.setClassName(mPkgName, mClassName);
        intent.putExtra("client", mName);
        boolean bindResult = mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
        if (mUiHdl.hasMessages(MSG_WHAT_REBIND)) {
            mUiHdl.removeMessages(MSG_WHAT_REBIND);
        }
        if (!bindResult) {
            mUiHdl.sendEmptyMessageDelayed(MSG_WHAT_REBIND, 15000);
        }
    }

    public void unbindMsgService() {
        if (!bindAction) {
            return;
        }
        bindAction = false;
        if (mUiHdl.hasMessages(MSG_WHAT_REBIND)) {
            mUiHdl.removeMessages(MSG_WHAT_REBIND);
        }
        if (isConn) {
            unbindMsgServiceImpl();
        }
    }

    public void unbindMsgServiceImpl() {
        mContext.unbindService(mConn);
        isConn = false;
        try {
            onDisconnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sentToServer(Message msgToServer) {
        try {
            msgToServer.replyTo = mMessenger;
            if (isConn && bindAction) {
                mService.send(msgToServer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void onHandleMessage(Message msgFromServer);

    protected abstract void onConnected();

    protected abstract void onDisconnected();
}
