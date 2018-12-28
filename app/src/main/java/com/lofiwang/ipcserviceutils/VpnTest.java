package com.lofiwang.ipcserviceutils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by chunsheng.wang on 2018/5/18.
 * lazy load
 */

public class VpnTest {
    private static final String TAG = "VpnTest";
    private final String mServerName = "47.88.156.102";
    private final int mServerPort = 8889;
    private final SocketAddress serverAddress = new InetSocketAddress(mServerName, mServerPort);
    private DatagramChannel datagramChannel;
    private static VpnTest sInstance = null;
    private TimerHandler timerHandler = new TimerHandler(new TimerHandler.TimerCallback() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onTimer() {
            mWorkHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendData("current time:" + SystemClock.elapsedRealtime());
                }
            });
        }

        @Override
        public void onStopped() {

        }
    });
    private Handler mWorkHandler = null;

    private VpnTest() {
        HandlerThread ht = new HandlerThread(TAG + "VpnTest");
        ht.start();
        mWorkHandler = new Handler(ht.getLooper()) {
            @Override
            public void handleMessage(Message msg) {

            }
        };

        timerHandler.setPeriod(5000);
        timerHandler.startDelayed(0);
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                sendData("current time:" + SystemClock.elapsedRealtime());
            }
        });
    }

    public static final VpnTest getInstance() {
        if (sInstance == null) {
            synchronized (VpnTest.class) {
                if (sInstance == null) {
                    sInstance = new VpnTest();
                }
            }
        }
        return sInstance;
    }

    void connectServer() {
        Log.d(TAG,"connectServer");
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.connect(serverAddress);
            datagramChannel.configureBlocking(false);
            Log.d(TAG,"connectServer ok");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"connectServer io exception");
            if (datagramChannel != null) {
                if (!datagramChannel.isConnected()) {
                    try {
                        datagramChannel.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    void sendData(String data) {
        Log.d(TAG,"sendData");
        if (datagramChannel != null && datagramChannel.isConnected()) {
            Log.d(TAG,"write data");
            if (!TextUtils.isEmpty(data)) {
                try {
                    datagramChannel.write(ByteBuffer.wrap(data.getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            connectServer();
        }
    }
}