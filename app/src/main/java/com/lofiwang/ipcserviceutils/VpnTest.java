package com.lofiwang.ipcserviceutils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.CharArrayReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
    private ByteBuffer byteBuffer = null;
    private TimerHandler timerHandler = new TimerHandler(new TimerHandler.TimerCallback() {
        @Override
        public void onStarted() {

        }

        @Override
        public void onTimer() {
            mSendHandler.post(new Runnable() {
                @Override
                public void run() {
                    sendData("current time:" + SystemClock.elapsedRealtime());
                }
            });
            mReceiveHandler.post(new Runnable() {
                @Override
                public void run() {
                    receiveData();

                }
            });
        }

        @Override
        public void onStopped() {

        }
    });
    private Handler mSendHandler = null;
    private Handler mReceiveHandler = null;

    private VpnTest() {
        HandlerThread ht = new HandlerThread(TAG + "VpnTest send");
        ht.start();
        HandlerThread ht1 = new HandlerThread(TAG + "VpnTest receive");
        ht1.start();

        mSendHandler = new Handler(ht.getLooper());
        mReceiveHandler = new Handler(ht1.getLooper());

        timerHandler.setPeriod(5000);
        timerHandler.startDelayed(0);
        mSendHandler.post(new Runnable() {
            @Override
            public void run() {
                sendData("current time:" + SystemClock.elapsedRealtime());

            }
        });
        mReceiveHandler.post(new Runnable() {
            @Override
            public void run() {
                receiveData();

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
        Log.d(TAG, "connectServer");
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.connect(serverAddress);
            datagramChannel.configureBlocking(false);
            byteBuffer = ByteBuffer.allocate(1024);
            Log.d(TAG, "connectServer ok");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "connectServer io exception");
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
        Log.d(TAG, "sendData");
        if (datagramChannel != null && datagramChannel.isConnected()) {
            Log.d(TAG, "write data");
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

    void receiveData() {
        Log.d(TAG, "receiveData");
        if (datagramChannel != null && datagramChannel.isConnected()) {
            Log.d(TAG, "read data");
            try {
                if (byteBuffer != null) {
                    int count = datagramChannel.read(byteBuffer);
                    byte[] bytes = byteBuffer.array();
                    String readStr = new String(bytes,0,count);
                    Log.d(TAG, "read data:" + readStr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            connectServer();
        }
    }
}