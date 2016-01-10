package com.jiuan.it.ipc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.service.ConstantlyService;
import com.jiuan.it.ipc.service.TimeService;
import com.jiuan.it.ipc.socket.SocketSend;
import com.jiuan.it.ipc.tools.DigitalTrans;
import com.jiuan.it.ipc.ui.view.ActivityCache;
import java.text.SimpleDateFormat;
import glnk.client.GlnkChannel;
import glnk.client.GlnkClient;

public abstract class BaseActivity extends Activity {

    private Toast tipError;

    public static String url = "10.42.0.1";//10.42.0.1;

    public static GlnkClient gClient;  //GooLink连接

    public static SocketSend send;  //Socket发送请求

    public static GlnkChannel liveChannel;

    private ProgressDialog progress;

    public SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private LocalBroadcastManager  mLocalBroadcastManager;

    private BaseReceive baseReceive;

    public boolean backPressed = false; //是否触发返回键

    public int  isService = 0;

    public boolean isShowDialog = false;

    public int sleep = 2000;

    private String message;

    private int millisInFuture = 30*1000;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCache.getScreenManager().pushActivity(this);
        tipError = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        registerReceiver();
        backPressed = false;
        isService = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 共通机能托管
        ZnkActivityUtil.setContext(this);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(baseReceive);
    }

    /**
     * 监听Back键按下事件
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;//自动调用finish()
        ActivityCache.getScreenManager().popActivity(this);
    }

    protected final Toast tipErrorShow(String value) {
        tipError.setText(value);
        tipError.show();
        return tipError;
    }

    protected final Toast tipErrorShow(int resId) {
        tipError.setText(resId);
        tipError.show();
        return tipError;
    }


    /**
     * 显示未知进度条对话框
     */
    protected final void showProgressDialog(String title, String message,int millisInFuture) {
        progress = ProgressDialog.show(this, title, message, true);
        this.message =message;
        this.millisInFuture =millisInFuture;
        if(!TextUtils.equals(message,"正在下载")
                ||!TextUtils.equals(message,"视频合成")){
            timer.start();
        }

    }

    /**
     * 消除未知进度条对话框
     */
    protected final void dismissProgressDialog() {
        if(progress!=null&&progress.isShowing()){
            progress.dismiss();
        }
    }

    CountDownTimer timer = new CountDownTimer(millisInFuture, 1000) { //10秒

        @Override
        public void onTick(long millisUntilFinished) {
            if(progress!=null&&!progress.isShowing()){
                cancel();
            }
        }
        @Override
        public void onFinish() {
            dismissProgressDialog();
            if(TextUtils.equals(message,"设备正在进行连接")){
                Log.e("打印关闭","onFinish");
                showFailDialog("网络提示", "设备已断开，请选择其他设备");
                stopConstantlyService();
            }
        }
    };

    class BaseReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Config.START_CONNECT)) {
                if(isService!=1){
                    startConnect();
                    isService = 1;
                }
            }else if(intent.getAction().equals(Config.STOP_CONNECT)){
                if(isService!=2){
                    stopConnect();
                    isService = 2;
                }
            }else if(intent.getAction().equals(Config.START_SEARCH)){
                search(intent.getBooleanExtra("flag",true));
            }else if(intent.getAction().equals(Config.ACTION_DEVICE_FAILURE)) {
                if(isShowDialog){
                    Log.e("打印关闭",intent.getStringExtra("TYPE"));
                    showFailDialog("提示", "设备已断开，请选择其他设备");
                    stopConstantlyService();
                    isShowDialog = false;
                }

            }
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.START_CONNECT);
        filter.addAction(Config.STOP_CONNECT);
        filter.addAction(Config.START_SEARCH);
        filter.addAction(Config.ACTION_DEVICE_FAILURE);
        baseReceive = new BaseReceive();
        mLocalBroadcastManager.registerReceiver(baseReceive, filter);
    }

    public  void startConstantlyService(boolean name) {
        Intent intent = new Intent(this, ConstantlyService.class);
        intent.putExtra("flag",name);
        startService(intent);
    }
    public  void stopConstantlyService() {
        Intent intent = new Intent(this, ConstantlyService.class);
        stopService(intent);
    }

    public void startConnect() {}
    public void stopConnect() {}
    public void search(boolean flag) {}
    public void start(){}
    public void stop(){}

    public  void startService() {
        Intent intent = new Intent(this, TimeService.class);
        startService(intent);
    }
    public  void stopService() {
        Intent intent = new Intent(this, TimeService.class);
        stopService(intent);
    }

    public  void sendData(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("TYPE", "BaseActivity");
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public  void sendData(String action, LocalBroadcastManager  localBroadcastManager) {
        Intent intent = new Intent();
        intent.setAction(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    public  void showFailDialog(String title, String message) {
        Dialog dialog = new AlertDialog.Builder(ZnkActivityUtil.getContext()).setTitle(title)
                .setMessage(message).setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent fail = new Intent();
                                if (Config.DEVICE_CODE == null&&
                                        Config.getLoginInfo(BaseActivity.this)==null) {
                                    fail.setClass(ZnkActivityUtil.getContext(), LoginActivity.class);
                                } else {
                                    fail.setClass(ZnkActivityUtil.getContext(), DeviceActivity.class);
                                }
                                fail.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(fail);
                                dialog.dismiss();
                            }
                        }

                ) . setCancelable(false)
                  . create();

                dialog.show();
        }


    public void showTokenFailure(String msg){
        if (TextUtils.equals(msg, "访问令牌错误")
                && System.currentTimeMillis() < Config.getGlobal(ZnkActivityUtil.getContext()).getToken().getAccessExpire()) {

            Dialog dialog = new AlertDialog.Builder(ZnkActivityUtil.getContext()).setTitle("提示")
                    .setMessage("您的账号在其他设备上登录了，请您重新登录").setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Config.cleanLogin(BaseActivity.this);
                                    Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    ZnkActivityUtil.finishActivity();
                                    dialog.dismiss();
                                }
                            }).create();
            // 显示
            dialog.show();

        }
    }

    /**GooLink 发送*/
    public int setGooLinkSend(byte[] data){
        if(data!=null&&liveChannel!=null){
            int result = liveChannel.sendManuData(data);
            return result;
        }
        return -1;
    }
}
