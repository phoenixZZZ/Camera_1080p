package com.jiuan.it.ipc.common.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.ui.BaseActivity;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.ui.DeviceActivity;

import java.util.Map;

public class ZnkActivityUtil {

    private static BaseActivity activityContext;
    private static String className;
    private static Intent intent = new Intent();

    public static Intent getSingletonIntent() {
        return intent;
    }

    public static String getClassName() {
        return className;
    }

    public static Context getContext() {
        return activityContext;
    }

    public static void setContext(BaseActivity context) {
        activityContext = context;
        className = activityContext.getClass().getSimpleName();
    }

    public static void showSimpleDialog(String title, String message) {

        Dialog dialog = new AlertDialog.Builder(activityContext).setTitle(title)
                .setMessage(message).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
        // 显示
        dialog.show();
    }

    public static void showSimpleDialogAndFinish(String title, String message) {

        Dialog dialog = new AlertDialog.Builder(activityContext).setTitle(title)
                .setMessage(message).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finishActivity();
                            }
                        }).create();
        // 显示
        dialog.show();
    }



    public static void showSimpleDialogAndForward(String message, final Class<? extends BaseActivity> clazz) {

        showSimpleDialogAndForward("提示", message, clazz);
    }

    public static void showSimpleDialogAndForward(String title, String message, final Class<? extends BaseActivity> clazz) {

        Dialog dialog = new AlertDialog.Builder(activityContext).setTitle(title)
                .setMessage(message).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finishActivity();
                                startActivity(clazz);
                            }
                        }).create();
        // 显示
        dialog.show();
    }

    public static void startActivity(Class<? extends BaseActivity> cls) {
        intent.setClass(activityContext, cls);
        intent.putExtra("name", className);
        activityContext.startActivity(intent);
    }

    public static void finishActivity() {
        activityContext.finish();
    }

    public static void clearEditText(int id) {
        ((EditText) activityContext.findViewById(id)).setText("");
    }

    public static void setVisibility(int id, boolean showFlg,Map<EditText,ImageView> clearMap) {

        for (Map.Entry<EditText,ImageView> entry : clearMap.entrySet()) {
            if (entry.getKey().getId() == id) {
                if (showFlg) {
                    entry.getValue().setVisibility(View.VISIBLE);
                } else {
                    entry.getValue().setVisibility(View.INVISIBLE);
                }
            } else {
                entry.getValue().setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void setVisibility(int id, boolean showFlg) {
        if (activityContext.findViewById(id) instanceof ImageView) {
            if (showFlg) {
                activityContext.findViewById(id).setVisibility(View.VISIBLE);
            } else {
                activityContext.findViewById(id).setVisibility(View.GONE);
            }
        }
    }

    public static void showSimpleDialog(String message) {
        showSimpleDialog("提示", message);
    }

    public static boolean isNetworkConnected() {
        return Client.isNetworkConnect(activityContext);
//        ConnectivityManager mConnectivityManager = (ConnectivityManager) activityContext
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//        if (mNetworkInfo != null) {
//            return mNetworkInfo.isAvailable();
//        }
//        return false;
    }

    public static String getString() {
        return activityContext.getString(R.string.resend_hint);
    }



    private static Dialog dialog;



    public static void overBlockDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static int dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5);
    }

    public static double px2Dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return px / scale + 0.5;
    }

    public static String getDeviceID() {
        TelephonyManager telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
