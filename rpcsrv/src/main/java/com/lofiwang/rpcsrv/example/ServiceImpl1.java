package com.lofiwang.rpcsrv.example;

import android.os.Message;
import android.os.Messenger;

import com.lofiwang.rpcsrv.MsgService;


public class ServiceImpl1 extends MsgService {
    private static final String TAG = "NewsService";

    public ServiceImpl1() {
        super(TAG);
    }

    @Override
    protected void onCreated() {

    }

    @Override
    protected void onDestroyed() {

    }

    @Override
    protected void onHandleMessage(Messenger clientMessenger, Message msgFromClient) {

    }
}
