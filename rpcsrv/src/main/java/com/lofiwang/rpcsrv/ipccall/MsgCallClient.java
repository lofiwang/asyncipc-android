package com.lofiwang.rpcsrv.ipccall;

import android.content.Context;
import android.os.Message;

import com.lofiwang.rpcsrv.base.MsgClient;

import java.util.WeakHashMap;

/**
 * Created by chunsheng.wang on 2018/5/24.
 */

public abstract class MsgCallClient extends MsgClient {
    private static final String TAG = "MsgCallClient";
    private static final String MSG_CALL_TOKEN_STR = "msg_call_token_str";

    private WeakHashMap<String, CallListener> mCalls = new WeakHashMap<>();

    public MsgCallClient(Context context, String name) {
        super(context, name);
    }

    @Override
    protected void onHandleMessage(Message msgFromServer) {
        String token = msgFromServer.getData().getString(MSG_CALL_TOKEN_STR);
        CallListener listener = mCalls.get(token);
        if (listener != null) {
            listener.onCalled(msgFromServer);
            mCalls.remove(token);
        }
    }

    protected abstract void onConnected();

    protected abstract void onDisconnected();

    public void sendMsgCall(final Message msgToServer, final CallListener listener) {
        getWorkHandler().post(new Runnable() {
            @Override
            public void run() {
                String token = listener.obtainTokenString();
                msgToServer.getData().putString(MSG_CALL_TOKEN_STR, token);
                mCalls.put(token, listener);
                sentToServer(msgToServer);
            }
        });

    }
}
