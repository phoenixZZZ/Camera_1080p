package com.jiuan.it.ipc.common.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Administrator on 2015/6/25.
 */
public class BlockDialog extends Dialog {

    int dialogResult;
    Handler mHandler ;

    public BlockDialog(Activity context)
    {
        super(context);
        setOwnerActivity(context);
        onCreate();
    }
    public int getDialogResult()
    {
        return dialogResult;
    }
    public void setDialogResult(int dialogResult)
    {
        this.dialogResult = dialogResult;
    }

    public void onCreate() {

    }

    public void endDialog(int result)
    {
        dismiss();
        setDialogResult(result);
        Message m = mHandler.obtainMessage();
        mHandler.sendMessage(m);
    }

    public int showDialog()
    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                // process incoming messages here
                //super.handleMessage(msg);
                throw new RuntimeException();
            }
        };
        super.show();
        try {
            Looper.getMainLooper().loop();
        }
        catch(RuntimeException e2)
        {
        }
        return dialogResult;
    }
}
