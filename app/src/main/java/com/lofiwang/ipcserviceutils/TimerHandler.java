package com.lofiwang.ipcserviceutils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by chunsheng.wang on 2018/11/14.
 */

public class TimerHandler {
    private static final int DEFAULT_PERIOD = 3 * 1000;
    private static final int MSG_WHAT_START = 1;
    private static final int MSG_WHAT_TIMER = 2;
    private static final int MSG_WHAT_STOP = 3;

    private static final int TIMER_STATE_STARTED = 1;
    private static final int TIMER_STATE_TIMER = 2;
    private static final int TIMER_STATE_STOPPED = 3;

    private final Handler handler;
    private long period = DEFAULT_PERIOD;
    private int timerState = TIMER_STATE_STOPPED;
    private final TimerCallback timerCallback;

    public TimerHandler(TimerCallback callback) {
        if (callback == null) {
            throw new NullPointerException("TimerHandler(TimerCallback callback) callback is null.");
        }
        this.timerCallback = callback;
        handler = new Handler(WorkThread.get().getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_WHAT_START:
                        if (timerState == TIMER_STATE_STARTED || timerState == MSG_WHAT_TIMER) {
                            return;
                        }
                        timerState = TIMER_STATE_STARTED;
                        try {
                            timerCallback.onStarted();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.sendEmptyMessageDelayed(MSG_WHAT_TIMER, period);
                        break;
                    case MSG_WHAT_TIMER:
                        if (timerState == TIMER_STATE_STOPPED) {
                            return;
                        }
                        timerState = TIMER_STATE_TIMER;
                        try {
                            timerCallback.onTimer();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.sendEmptyMessageDelayed(MSG_WHAT_TIMER, period);
                        break;
                    case MSG_WHAT_STOP:
                        if (timerState == TIMER_STATE_STOPPED) {
                            return;
                        }
                        timerState = TIMER_STATE_STOPPED;
                        if (this.hasMessages(MSG_WHAT_TIMER)) {
                            this.removeMessages(MSG_WHAT_TIMER);
                        }
                        if (this.hasMessages(MSG_WHAT_START)) {
                            this.removeMessages(MSG_WHAT_START);
                        }
                        if (this.hasMessages(MSG_WHAT_STOP)) {
                            this.removeMessages(MSG_WHAT_STOP);
                        }
                        try {
                            timerCallback.onStopped();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };
    }

    public void setPeriod(long period) {
        if (period < DEFAULT_PERIOD) {
            Log.w("TimerHandler", "timer period must be more than 10s.");
            period = DEFAULT_PERIOD;
        }
        this.period = period;
        if (handler.hasMessages(MSG_WHAT_TIMER)) {
            handler.removeMessages(MSG_WHAT_TIMER);
            handler.sendEmptyMessageDelayed(MSG_WHAT_TIMER, period);
        }
    }

    public long getPeriod() {
        return period;
    }

    public void startDelayed(long delayMillis) {
        if (handler.hasMessages(MSG_WHAT_START)) {
            handler.removeMessages(MSG_WHAT_START);
        }
        handler.sendEmptyMessageDelayed(MSG_WHAT_START, delayMillis);
    }

    public void stopDelayed(long delayMillis) {
        if (handler.hasMessages(MSG_WHAT_STOP)) {
            handler.removeMessages(MSG_WHAT_STOP);
        }
        handler.sendEmptyMessageDelayed(MSG_WHAT_STOP, delayMillis);
    }

    public interface TimerCallback {
        void onStarted();

        void onTimer();

        void onStopped();
    }
}
