package com.jiuan.it.ipc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class CustomTimerButton extends Button implements View.OnClickListener {

    private static final int DEFAULT_TIME_COUNT = 61;

    private static final int DEFAULT_SLEEP_TIME = 1000;

    private int timeCount = DEFAULT_TIME_COUNT;

    private int sleepTime = DEFAULT_SLEEP_TIME;

    private boolean stopThread;

    public interface TimerListener {

        void timerStart(View v, int startCount);

        void timerCount(View v, int currentCount);

        void timerEnd(View v);

    }

    private TimerListener timerListener;

    public CustomTimerButton(Context context) {
        super(context);
    }

    public CustomTimerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTimerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopThread = true;
    }

    @Override
    public void onClick(View v) {
        if (timerListener != null && timeCount > 0) {
            stopThread = false;
            timerListener.timerStart(v, timeCount);
            setClickable(false);
            new Thread(new TimerRunnable(v, timeCount)).start();
        }
    }

    /**
     * 设置计数监听器
     */
    public void setTimerListener(TimerListener timerListener) {
        setOnClickListener(this);
        this.timerListener = timerListener;
    }

    /**
     * 设置计数
     */
    public void setTimeCount(int timeCount) {
        this.timeCount = timeCount;
    }

    public int getTimeCount() {
        return timeCount;
    }

    /**
     * 设置休眠时间
     */
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void stopThread() {
        stopThread = true;
    }

    private class TimerRunnable implements Runnable {

        private View v;

        private int count;

        public TimerRunnable(View v, int count) {
            this.v = v;
            this.count = count;
        }

        @Override
        public void run() {
            while (count > 0 && !stopThread) {
                try {
                    count--;
                    if (timerListener != null) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                timerListener.timerCount(v, count);
                            }
                        });
                    }
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (timerListener != null) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        timerListener.timerEnd(v);
                        setClickable(true);
                    }
                });
            }
        }
    }

}
