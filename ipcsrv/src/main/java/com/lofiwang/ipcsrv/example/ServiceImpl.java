package com.lofiwang.ipcsrv.example;

import android.os.Message;
import android.os.Messenger;

import com.lofiwang.ipcsrv.base.MsgPoolService;
import com.lofiwang.ipcsrv.thread.ExecutorHandler;

import java.lang.ref.WeakReference;


public class ServiceImpl extends MsgPoolService {
    private static final String TAG = "NewsService";
    WeakReference<ExecutorHandler> weakReference = new WeakReference<>(new ExecutorHandler());
    private static ExecutorHandler executorHandler = new ExecutorHandler() {
        @Override
        protected void onHandleMessage(Message msg) {
            super.onHandleMessage(msg);
        }
    };


    public ServiceImpl() {
        super(TAG);
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
