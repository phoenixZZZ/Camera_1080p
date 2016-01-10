package com.jiuan.it.ipc.ui.view;

import android.app.Activity;

import java.util.Stack;

public class ActivityCache {
    private static Stack<Activity> activityStack;
    private static ActivityCache instance;
    private ActivityCache() {
    }
    public static ActivityCache getScreenManager() {
        if (instance == null) {
            instance = new ActivityCache();
        }
        return instance;
    }
    //退出栈顶Activity
    public void popActivity(Activity activity) {
        if (activity != null) {
            //在从自定义集合中取出当前Activity时，也进行了Activity的关闭操作
            activity.finish();
            activityStack.remove(activity);
            activity = null;
        }
    }
    //获得当前栈顶Activity
    public Activity currentActivity() {
        Activity activity = null;
        if(!activityStack.empty())
            activity= activityStack.lastElement();
        return activity;
    }
    //将当前Activity推入栈中
    public void pushActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    //退出栈中所有Activity
    public void popAllActivity() {

        Activity currentActivity = null;

        while (true) {
            currentActivity = currentActivity();
            if (currentActivity == null) {
                break;
            }

            popActivity(currentActivity);
        }
    }

    //退出栈中所有Activity
    public void popAllActivityExceptOne(Class cls) {

        Activity currentActivity = null;

        while (true) {
            currentActivity = currentActivity();
            if (currentActivity == null) {
                break;
            }
            if (currentActivity.getClass().equals(cls)) {
                break;
            }

            popActivity(currentActivity);
        }

    }
}