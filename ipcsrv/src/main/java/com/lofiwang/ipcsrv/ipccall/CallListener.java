package com.lofiwang.ipcsrv.ipccall;

import android.os.Message;
import android.os.SystemClock;

abstract public class CallListener {
    private long timestamp = SystemClock.elapsedRealtimeNanos();

    public String obtainTokenString() {
        return this.timestamp + this.toString();
    }

    abstract void onCalled(Message msg);
}
