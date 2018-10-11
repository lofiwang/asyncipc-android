package com.lofiwang.ipcsrv.ipccall;

import android.os.Message;
import android.os.Messenger;

import com.lofiwang.ipcsrv.base.MsgService;

/**
 * Created by lofiwang on 2018/10/11.
 */

public class MsgCallService extends MsgService {
    private static final String TAG = "MsgService";
    private static boolean DBG = false;

    public MsgCallService(String name) {
        super(name);
    }

    @Override
    protected void onHandleMessage(Messenger clientMessenger, Message msgFromClient) {

    }

    @Override
    protected void onCreated() {

    }

    @Override
    protected void onDestroyed() {

    }
}
