package com.jiuan.it.ipc.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IPCameraApplication;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.tools.DensityUtils;
import com.jiuan.it.ipc.ui.view.ActivityCache;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.jiuan.it.ipc.ui.widget.CustomToolbar.OnClickCustomToolbarListener;

import java.text.SimpleDateFormat;

/**
 * 摄像头基类
 */
public abstract class RealTimeActivity extends BaseActivity   {

    private ProgressDialog progress;

    public WindowManager wm=null;

    public WindowManager.LayoutParams wmParams=null;

    public ImageView leftbtn=null;

    public ImageView rightbtn=null;

    private int mAlpha = 0;// ImageView的alpha值

    private boolean isHide;

    public PopupWindow popupWindow;

    public LinearLayout player;

    public LinearLayout bar;

    public SeekBar mSeekBar;

    public final  int  disparity = 180;

    public TextView hint;

    public Button btn_left,btn_right;

    public View viewLand,viewPort,view;

    public static IPCameraApplication application;

    private int popupWidth; //显示宽度

    private boolean  port = true; //显示宽度

    public SimpleDateFormat fmt=new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public  final  int  maxImagesNumber= 6 ;

    public String[] quality ,videoWidth,videoHeight;

    public String deviceName;

    public String deviceId;

    /** 视频质量设置 */
    public final int GooLink_VIDEO_QUALITY_HIGH = 0xA200;////设置视频质量高

    public final int GooLink_VIDEO_QUALITY_MIDDLE = 0xA201;//设置视频质量中

    public final int GooLink_VIDEO_QUALITY_LOW = 0xA202;//设置视频质量低

    public final int GooLink_VIDEO_QUALITY_OK = 0xA210;//返回值

    /** LED*/
    public final int GooLink_LED_ON = 0xA300; //LED开启

    public final int GooLink_LED_OFF = 0xA301;//LED关闭

    /** 报警*/
    public final int GooLink_ALARM_ON = 0xA400;//报警开启

    public final int GooLink_ALARM_OFF = 0xA401;//报警关闭

    private boolean isResume = false;

    private PowerManager powerManager = null;

    private PowerManager.WakeLock wakeLock = null;

    private CustomToolbar toolbar =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持屏幕常亮
        setContentView(R.layout.activity_port_base);
        application = (IPCameraApplication)getApplication();
        application.setContext(this);
        quality = getResources().getStringArray(R.array.quality);
        videoWidth = getResources().getStringArray( R.array.videoWidth);
        videoHeight = getResources().getStringArray(R.array.videoHeight);
        deviceId = getIntent().getStringExtra("UID");
        deviceName = getIntent().getStringExtra("Name");
        powerManager = (PowerManager)this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        findViews();
        setAllowControl(false,0);
    }

    @Override

    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        Configuration();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initFloatView();
        if(!backPressed&&isResume){
            stopService();
        }
        isResume = true;
        if(isService==2){
           isShowDialog = true;
            Log.e("RealTimeActivity","关闭");
           sendData(Config.ACTION_DEVICE_FAILURE);
        }
        wakeLock.acquire();//屏幕常亮唤醒状态-开启
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在程序退出(Activity销毁）时销毁悬浮窗口
        wm.removeView(leftbtn);
        wm.removeView(rightbtn);

        if(!backPressed){
            startService();
        }
        wakeLock.release();//屏幕常亮唤醒状态-关闭

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
         //stop();
         Config.XROT = 0.0f;
         dismissProgressDialog();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
             stop();
            return true;
        }

        return false;
    }
    /**
     * 发送操作信息
     */
    public void sendHandle( Handler handler,int what,Object object) {
        Message panorama = new Message();
        panorama.what = what;
        panorama.obj = object;
        handler.sendMessage(panorama);
    }


    public void findViews(){
        player = (LinearLayout) findViewById(R.id.player);
        hint = (TextView) findViewById(R.id.hint);
        bar = (LinearLayout) findViewById(R.id.bar);
        mSeekBar  = (SeekBar) findViewById(R.id.seekBar);
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_right = (Button) findViewById(R.id.btn_right);
        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        toolbar.setOnClickCuteToolbarListener(new OnClickCustomToolbarListener() {
            @Override
            public void onClickLeft() {
                   stop();
            }

            @Override
            public void onClickRight() {
                Config.XROT -=90f;
            }
        });
        mSeekBar.setMax(360);
        /** 拖动按钮 */
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
        mSeekBar.setEnabled(false);
        viewPort = findViewById(R.id.bar_01);
        viewLand = findViewById(R.id.bar_02);
        view = viewPort;
        port = true;
    }

    public void Configuration(){
        int mCurrentOrientation = getResources().getConfiguration().orientation;
        if ( mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT ) {
            findViewById(R.id.bar_01).setVisibility(View.VISIBLE);
            findViewById(R.id.bar_02).setVisibility(View.GONE);
            view = viewPort;
            port = true;

        } else if ( mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
            findViewById(R.id.bar_01).setVisibility(View.GONE);
            findViewById(R.id.bar_02).setVisibility(View.VISIBLE);
            view = viewLand;
            port = false;

        }
    }

    public  void loadConnect() {

    }


    /**
     * 视频质量界面
     * @param xoff
     * @param yoff
     * @param btn
     */
    protected void showPopupWindow(int xoff, int yoff ,View btn,String value) {
        final String[] objects = new String[2];
        int j=0;
        for (int i = 0; i < quality.length; i++) {
            if(!value.equals(quality[i])){
                objects[j] = quality[i];
                j++;
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,objects);
        ListView listview = new ListView(this);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position, objects[position]);
            }
        });
        popupWindow = new PopupWindow(this);
        popupWindow.setHeight(DensityUtils.dp2px(this, 120));
        if(port){
            popupWidth = DensityUtils.dp2px(this, 100);//(view.getWidth()*40)/100;
        }else{
            popupWidth = (view.findViewById(R.id.layout_content).getWidth()*40)/100;
        }
        popupWindow.setWidth(popupWidth);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.argb(50, 52, 53, 55)));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(listview);
        popupWindow.showAsDropDown(btn, xoff, yoff+Config.number);
        if (thread != null && !thread.isAlive()) {
            thread.start();
        }
    }

    Thread thread = new Thread() {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
    };
    /**
     * 初始化悬浮按钮
     */
    private void initFloatView(){
        //获取WindowManager
        wm=(WindowManager)getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //设置LayoutParams(全局变量）相关参数
        wmParams = new WindowManager.LayoutParams();

        wmParams.type= WindowManager.LayoutParams.TYPE_PHONE;   //设置window type
        wmParams.format=PixelFormat.RGBA_8888;   //设置图片格式，效果为背景透明
        //设置Window flag
        wmParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //以屏幕左上角为原点，设置x、y初始值
        wmParams.x=0;
        wmParams.y=0;
        //设置悬浮窗口长宽数据
        wmParams.width=DensityUtils.dp2px(this, 80);
        wmParams.height=DensityUtils.dp2px(this, 80);

        //创建悬浮按钮
        createLeftFloatView();
        createRightFloatView();
    }

    /**
     * 创建左边悬浮按钮
     */
    private void createLeftFloatView(){
        leftbtn=new ImageView(this);
        leftbtn.setImageResource(R.drawable.btn_left_land);
        leftbtn.setAlpha(0);
        leftbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                setLeftbtn();
            }
        });
        //调整悬浮窗口
        wmParams.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
        //显示myFloatView图像
        wm.addView(leftbtn, wmParams);
    }
    /**
     * 创建右边悬浮按钮
     */
    private void createRightFloatView(){
        rightbtn=new ImageView(this);
        rightbtn.setImageResource(R.drawable.btn_right_land);
        rightbtn.setAlpha(0);
        rightbtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                setRightbtn();
            }
        });
        //调整悬浮窗口
        wmParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        //显示myFloatView图像
        wm.addView(rightbtn, wmParams);
    }
    /**
     * 图片渐变显示处理
     */
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg) {
            if(msg.what==1 && mAlpha<255){
                //System.out.println("---"+mAlpha);
                mAlpha += 50;
                if(mAlpha>255)
                    mAlpha=255;
                leftbtn.setAlpha(mAlpha);
                leftbtn.invalidate();
                rightbtn.setAlpha(mAlpha);
                rightbtn.invalidate();
                if(!isHide && mAlpha<255)
                    mHandler.sendEmptyMessageDelayed(1, 100);
            }else if(msg.what==0 && mAlpha>0){
                //System.out.println("---"+mAlpha);
                mAlpha -= 10;
                if(mAlpha<0)
                    mAlpha=0;
                leftbtn.setAlpha(mAlpha);
                leftbtn.invalidate();
                rightbtn.setAlpha(mAlpha);
                rightbtn.invalidate();
                if(isHide && mAlpha>0)
                    mHandler.sendEmptyMessageDelayed(0, 100);
            }
        }
    };

    private void showFloatView(){
        isHide = false;
        mHandler.sendEmptyMessage(1);
    }

    private void hideFloatView(){
        new Thread(){
            public void run() {
                try {
                    Thread.sleep(1500);
                    isHide = true;
                    mHandler.sendEmptyMessage(0);
                } catch (Exception e) {

                }
            }
        }.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int mCurrentOrientation = getResources().getConfiguration().orientation;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                //System.out.println("========ACTION_DOWN");
                if ( mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
                   showFloatView();
                }
                break;
            case MotionEvent.ACTION_UP:
                //System.out.println("========ACTION_UP");
                if ( mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE ) {
                    hideFloatView();
                }
                break;
        }
        return true;
    }

    /**
     * 左边按钮执行事件
     */
    public  void setLeftbtn(){

    }
    /**
     * 左边按钮执行事件
     */
    public  void setRightbtn(){

    }
    /**
     * 视频质量选项
     */
    public  void selectItem(int position,String value){

    }

    /**
     * 锁定屏幕
     * @param activity
     */
    public  void disableRotation(Activity activity){
        switch (getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();//api<8要将getRotation换成getOrientation
                    if(rotation == android.view.Surface.ROTATION_90|| rotation == android.view.Surface.ROTATION_180){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }
                break;

            case Configuration.ORIENTATION_LANDSCAPE:
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    int rotation = getWindowManager().getDefaultDisplay().getRotation();//同上
                    if(rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    }
                }
                break;
        }
    }

    /**
     * 解锁屏幕
     * @param activity
     */
    public void enableRotation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    /**
     * 解锁屏幕
     * @param id
     * @param resid 背景图
     */
    public void setOnClickState(int id,int resid){
        viewPort.findViewById(id).setBackgroundResource(resid);
        viewLand.findViewById(id).setBackgroundResource(resid);
    }


    /** 权限控制按钮*/
    public  void setAllowControl(boolean flag,int mark) {
        btn_left.setEnabled(flag);
        btn_right.setEnabled(flag);
        viewPort.findViewById(R.id.btn_select).setEnabled(flag);
        viewLand.findViewById(R.id.btn_select).setEnabled(flag);
        if(mark==1){
            viewPort.findViewById(R.id.btn_microphone).setEnabled(flag);
            viewLand.findViewById(R.id.btn_microphone).setEnabled(flag);
        }else {
            viewPort.findViewById(R.id.btn_microphone).setEnabled(false);
            viewLand.findViewById(R.id.btn_microphone).setEnabled(false);
        }
    }
    private class OnSeekBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {

        // 触发操作，拖动
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

        }

        // 表示进度条刚开始拖动，开始拖动时候触发的操作
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        // 停止拖动时候
        public void onStopTrackingTouch(SeekBar seekBar) {
            int seekProgress = mSeekBar.getProgress();
            if(seekProgress<8){
                mSeekBar.setProgress(0);
                sendPTZ(360);
            } else if(seekProgress>=8 && seekProgress<23){
                mSeekBar.setProgress(15);
                sendPTZ(345);
            }else if(seekProgress>=23 && seekProgress<38){
                mSeekBar.setProgress(30);
                sendPTZ(330);
            }else if(seekProgress>=38 && seekProgress<53){
                mSeekBar.setProgress(45);
                sendPTZ(315);
            }else if(seekProgress>=53 && seekProgress<68){
                mSeekBar.setProgress(60);
                sendPTZ(300);
            }else if(seekProgress>=68 && seekProgress<83){
                mSeekBar.setProgress(75);
                sendPTZ(285);
            }else if(seekProgress>=83 && seekProgress<98){
                mSeekBar.setProgress(90);
                sendPTZ(270);
            }else if(seekProgress>=98 && seekProgress<113){
                mSeekBar.setProgress(105);
                sendPTZ(255);
            }else if(seekProgress>=113 && seekProgress<128){
                mSeekBar.setProgress(120);
                sendPTZ(240);
            }else if(seekProgress>=128 && seekProgress<143){
                mSeekBar.setProgress(135);
                sendPTZ(225);
            }else if(seekProgress>=143 && seekProgress<158){
                mSeekBar.setProgress(150);
                sendPTZ(210);
            }else if(seekProgress>=158 && seekProgress<173){
                mSeekBar.setProgress(165);
                sendPTZ(195);
            }else if(seekProgress>=173 && seekProgress<188){
                mSeekBar.setProgress(180);
                sendPTZ(0);
            }else if(seekProgress>=188 && seekProgress<203){
                mSeekBar.setProgress(195);
                sendPTZ(15);
            }else if(seekProgress>=203 && seekProgress<218){
                mSeekBar.setProgress(210);
                sendPTZ(30);
            }else if(seekProgress>=218 && seekProgress<233){
                mSeekBar.setProgress(225);
                sendPTZ(45);
            }else if(seekProgress>=233 && seekProgress<248){
                mSeekBar.setProgress(240);
                sendPTZ(60);
            }else if(seekProgress>=248 && seekProgress<263){
                mSeekBar.setProgress(255);
                sendPTZ(75);
            }else if(seekProgress>=263 && seekProgress<278){
                mSeekBar.setProgress(270);
                sendPTZ(90);
            }else if(seekProgress>=278 && seekProgress<293){
                mSeekBar.setProgress(285);
                sendPTZ(105);
            }else if(seekProgress>=293 && seekProgress<308){
                mSeekBar.setProgress(300);
                sendPTZ(120);
            }else if(seekProgress>=308 && seekProgress<323){
                mSeekBar.setProgress(315);
                sendPTZ(135);
            }else if(seekProgress>=323 && seekProgress<338){
                mSeekBar.setProgress(330);
                sendPTZ(150);
            }else if(seekProgress>=338 && seekProgress<353){
                mSeekBar.setProgress(345);
                sendPTZ(165);
            }else if(seekProgress>=353){
                mSeekBar.setProgress(360);
                sendPTZ(180);
            }

        }
    }

    /**发送新云台转动*/
    public void sendPTZ(int seekProgress){

    }
}

