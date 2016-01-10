package com.jiuan.it.ipc.common.timertask;


import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTask60 extends TimerTask {

    Timer timer;
    Handler han;
    int count = 60;


    public TimerTask60(Timer timer, Handler han) {
        this.timer = timer;
        this.han = han;
    }

    @Override
    public void run() {
        Message msg = new Message();
        if (count-- > 0) {
            msg.what = 0;
            msg.obj = count;
            han.sendMessage(msg);
        } else {
            msg.what = 1;
            msg.obj = count;
            han.sendMessage(msg);
            timer.cancel();
            this.cancel();
        }
    }
}
