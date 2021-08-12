package com.braintreepayments.api;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public class UiDelayTimer {

    private final Handler handler;
    private final long minDelay;

    private long startTime;

    UiDelayTimer(long minDelay) {
        this.minDelay = minDelay;
        this.handler = new Handler(Looper.getMainLooper());
    }

    void start() {
        startTime = SystemClock.elapsedRealtime();
    }

    void postWhenReady(Runnable r) {
        long elapsedTime = SystemClock.elapsedRealtime() - startTime;
        if (elapsedTime >= minDelay) {
            handler.post(r);
        } else {
            // prevent delay from being negative
            long delay = Math.max(0, minDelay - elapsedTime);
            handler.postDelayed(r, delay);
        }
    }
}
