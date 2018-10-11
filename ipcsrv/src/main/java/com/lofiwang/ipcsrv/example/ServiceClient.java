package com.lofiwang.ipcsrv.example;

import android.content.Context;
import android.os.Message;

import com.lofiwang.ipcsrv.base.MsgClient;


/**
 * Created by chunsheng.wang on 2018/5/24.
 */

public class ServiceClient extends MsgClient {
    private static final String TAG = "NewsMgr";

    public ServiceClient(Context context) {
        super(context, TAG);
    }

    @Override
    protected void onConnected() {

    }

    @Override
    protected void onDisconnected() {

    }

    @Override
    protected void onHandleMessage(Message msgFromServer) {

    }
}
