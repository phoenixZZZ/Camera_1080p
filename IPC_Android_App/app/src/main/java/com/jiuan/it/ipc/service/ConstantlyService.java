package com.jiuan.it.ipc.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.jiuan.it.ipc.Config;

public  class ConstantlyService extends Service {

    private LocalBroadcastManager mLocalBroadcastManager;

    private final int TIME = 10 * 1000; //10秒

    private  Intent intent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocalBroadcastManager= LocalBroadcastManager.getInstance(this);
        //定时器发送广播
        handler.postDelayed(runnable, TIME);//执行一次runnable.
        this.intent = intent;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendData() {
        Intent intent = new Intent();
        intent.setAction(Config.START_SEARCH);
        intent.putExtra("flag", intent.getBooleanExtra("flag",true));
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    Handler handler=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            sendData();
            handler.postDelayed(runnable, TIME);//执行一次runnable.
        }
    };
}
