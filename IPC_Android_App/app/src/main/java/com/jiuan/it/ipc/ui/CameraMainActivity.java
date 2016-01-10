package com.jiuan.it.ipc.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IBackService;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.LastVer;
import com.jiuan.it.ipc.http.response.VerInfo;
import com.jiuan.it.ipc.model.ReceiveModel;
import com.jiuan.it.ipc.service.BackService;
import com.jiuan.it.ipc.socket.SocketSend;
import com.jiuan.it.ipc.tools.AppUtils;
import com.jiuan.it.ipc.tools.DigitalTrans;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import glnk.client.DataSourceListener2;
import glnk.client.GlnkChannel;
import glnk.client.GlnkClient;
import glnk.rt.MyRuntime;

public class CameraMainActivity extends BaseActivity implements View.OnClickListener ,
        CustomToolbar.OnClickCustomToolbarListener  {

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    /** 远端录像*/
    public final int GooLink_RECORDER_START = 0xA500; //设置录制开始

    public final int GooLink_RECORDER_STOP = 0xA501;//设置录制停止

    public final int GooLink_RECORDER_QUEST = 0xA502;//查询录像状态

    public final int GooLink_RECORDER_OK = 0xA510;//指令成功

    public final int GooLink_RECORDER_NO_SDCARD = 0xA511;//摄像头内无SD卡

    public final int GooLink_RECORDER_LOW_SDCARD = 0xA512;//摄像头内SD卡剩余容量不足

    public final int GooLink_RECORDER_WRONG_LOGIC = 0xA513;//控制逻辑错误

    public final int GooLink_RECORDER_RESP_YES = 0xA514;//正在录像状态

    public final int GooLink_RECORDER_RESP_NO = 0xA515;//非正在录像状态

    public final int GooLink_RECORDER_LOW_SDCARD_STOP = 0xA516;//摄像头内SD卡剩余容量不足，录像停止

    /** 全景图片*/
    public final int GooLink_PANORAMIC_START = 0xA600;//设置全景开始

    public final int GooLink_PANORAMIC_OK = 0xA610;//设置成功

    public final int GooLink_PANORAMIC_WRONG = 0xA611;//错误，非云台底座

    /** 锁定控制*/
    public static final int GooLink_CONTROL_QUEST = 0xA700;//查询是否拥有控制权

    public static final int GooLink_CONTROL_RESP_YES = 0xA710;//有控制权

    public static final int GooLink_CONTROL_RESP_NO = 0xA711;//没有控制权

    /** 云台控制(360度)*/
    public  static  int GooLink_PTZ_ANGLE_SET ;//设置转动位置角度（0——360）0x0000——0x0168

    public  static final int GooLink_PTZ_STOP= 0xA802;//云台转动紧急停止

    public  static final int GooLink_PTZ_ZERO= 0xA803;//控制云台回到零点位置

    public  static final int GooLink_PTZ_ANGLE_QUERY= 0xA804;//获取云台当前位置角度

    public  static final int GooLink_PTZ_QUERY= 0xA805;//查询有无云台

    public  static final int GooLink_PTZ_SUCCESS = 0xA810;//控制成功

    public  static final int GooLink_PTZ_FAIL = 0xA811;//控制失败

    public  static final int GooLink_PTZ_ING = 0xA812;//正在执行其它命令，无法执行当前命令

    public  static final int GooLink_PTZ_BEYOND= 0xA813;//位置角度不在规定范围内

    public  static final int GooLink_PTZ_EXIST= 0xA814;//有云台

    public  static final int GooLink_PTZ_NONE= 0xA815;//无云台

    private CustomToolbar toolbar =null;

    private RelativeLayout real =null;

    private RelativeLayout longDistance =null;

    private RelativeLayout wholeScene =null;

    private RelativeLayout loading =null;

    private RelativeLayout video =null;

    private RelativeLayout image =null;

    private RelativeLayout refresh =null;

    private ImageView imageLongDistance =null;

    private TextView textLongDistance =null;

    private int connectType = 0; // 0-失败 1-Rstp 2-Goolink

    private IBackService iBackService;

    private Intent mServiceIntent;

    private LocalBroadcastManager mLocalBroadcastManager;

    private SocketReceive mReceiver;

    private GooLinkDataSource liveSource = null;

    private boolean isLongDistance = false;

    private  static GooLinkSource mGooLinkSource;

    private final  int sleep = 10000; //10秒

    private   String  pictureDirName ;

    private boolean isPause =  false;

    private boolean isResume = false;

    private  boolean mBound;

    private  boolean mDidCallUnbind;
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            iBackService = null;
            mDidCallUnbind = false;
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iBackService = IBackService.Stub.asInterface(service);
            mBound = true;
            send = new SocketSend(CameraMainActivity.this,iBackService);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_camera_main);

        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        real = (RelativeLayout)this.findViewById(R.id.relative_real);
        longDistance = (RelativeLayout)this.findViewById(R.id.relative_long_distance);
        wholeScene = (RelativeLayout)this.findViewById(R.id.relative_whole_scene);
        loading = (RelativeLayout)this.findViewById(R.id.relative_loading);
        video = (RelativeLayout)this.findViewById(R.id.relative_video);
        image = (RelativeLayout)this.findViewById(R.id.relative);
        refresh = (RelativeLayout)this.findViewById(R.id.relative_refresh);
        imageLongDistance = (ImageView)this.findViewById(R.id.image_long_distance);
        textLongDistance = (TextView)this.findViewById(R.id.text_long_distance);

        toolbar.setOnClickCuteToolbarListener(this);
        real.setOnClickListener(this);
        longDistance.setOnClickListener(this);
        wholeScene.setOnClickListener(this);
        video.setOnClickListener(this);
        image.setOnClickListener(this);
        refresh.setOnClickListener(this);
        startConnect();
        setAllowControl(-1);
        setGooLinkSend(DigitalTrans.intToByteArray(GooLink_PTZ_QUERY));//查看有无云台
    }

    @Override
    protected void onResume() {
        super.onResume();
        startConstantlyService(true);
        if(!backPressed&&isResume){
            stopService();
        }
        isResume = true;
        isPause = false ;
        if(isService==2){ //设备断开
            isShowDialog = true;
            sendData(Config.ACTION_DEVICE_FAILURE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopConstantlyService();
        if(!backPressed&&!isPause){
            startService();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopConnect();
        //stop();
    }

    @Override
    public void startConnect() {
        super.startConnect();
        mServiceIntent = new Intent(this, BackService.class);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        registerReceiver();
        bindService(mServiceIntent, conn, BIND_AUTO_CREATE);
        showProgressDialog("提示", "设备正在进行连接",30*1000);
    }

    @Override
    public void stopConnect() {
        super.stopConnect();
        if(connectType==1){
            unbindService();
            mLocalBroadcastManager.unregisterReceiver(mReceiver);
        }else {
            if (liveChannel != null) {
                liveChannel.stop();
                liveChannel = null;
                gClient.release();
            }
        }
        updateImage.removeCallbacks(runnable);
    }
    void unbindService() {
        if (mBound && !mDidCallUnbind) {
            mDidCallUnbind = true;
            unbindService(conn);
        }
    }

    @Override
    public void search(boolean flag) {
        super.search(flag);
        isShowDialog = true;
        if(connectType==1&&flag){
            send.sendData(Config.GETIPCISCONTROL, 0); // 设备的控制权
        }else if(connectType==2&&flag){
            setGooLinkSend(DigitalTrans.intToByteArray(GooLink_CONTROL_QUEST));//设备的控制权
        }
    }

    @Override
    public void onClickLeft() {
        backPressed = true;
        ZnkActivityUtil.finishActivity();
    }

    @Override
    public void onClickRight() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_real: //实时
                Config.XROT = 0.0f;
                if(connectType==1){
                    isPause = true;
                    Intent video = new Intent(getApplicationContext(),
                            RtspActivity.class);
                    startActivity(video);
                }else if(connectType==2){
                    isPause = true;
                    Intent video = new Intent(getApplicationContext(),
                            GoolinkActivity.class);
                    startActivity(video);

                }

                break;
            case R.id.relative_long_distance://远程
                if(connectType==1){
                    if(!isLongDistance){
                        send.sendData(Config.RECORDERSTART, 0);
                    }else {
                        send.sendData(Config.RECORDERSTOP, 0);
                    }
                }else if(connectType==2){
                     if(!isLongDistance){
                         setGooLinkSend(DigitalTrans.intToByteArray(GooLink_RECORDER_START));
                     }else {
                         setGooLinkSend(DigitalTrans.intToByteArray(GooLink_RECORDER_STOP));
                     }
                }
                break;
            case R.id.relative_whole_scene://全景
                if((int)wholeScene.getTag()==-1){
                    ZnkActivityUtil.showSimpleDialog("此功能暂不可用");
                    break;
                }else if((int)wholeScene.getTag()==0){
                    ZnkActivityUtil.showSimpleDialog("无设备控制权");
                    break;
                }

                if (connectType==1){
                    send.sendData(Config.REQUESTALLPICTURE, 0);
                } else if (connectType == 2) {
                    setGooLinkSend(DigitalTrans.intToByteArray(GooLink_PANORAMIC_START));
                }
                break;

            case R.id.relative_video://所有录像
                if((int)video.getTag()==-1){
                    ZnkActivityUtil.showSimpleDialog("此功能暂不可用");
                    break;
                }else if((int)video.getTag()==0){
                    ZnkActivityUtil.showSimpleDialog("无设备控制权");
                    break;
                }
                if(connectType==1){
                    isPause = true;
                    Intent video = new Intent(getApplicationContext(),
                            PreviewListActivity.class);
                    video.putStringArrayListExtra("videoList",new ArrayList<String>());
                    startActivity(video);
                }else if(connectType==2){
                    ZnkActivityUtil.showSimpleDialog("此功能暂不可用");
                }
                break;
            case R.id.relative://所有图片
                if((int)image.getTag()==-1){
                    ZnkActivityUtil.showSimpleDialog("此功能暂不可用");
                    break;
                }

                if(Config.DEVICE_CODE!=null){ //必须存在设备ID
                    isPause = true;
                    Intent image = new Intent(getApplicationContext(),
                            ImageListActivity.class);
                    image.putStringArrayListExtra("imageList",new ArrayList<String>());
                    startActivity(image);
                }
                break;
            case R.id.relative_refresh://检查固件更新
                if((int)refresh.getTag()==-1){
                    ZnkActivityUtil.showSimpleDialog("此功能暂不可用");
                    break;
                }else if((int)refresh.getTag()==0){
                    ZnkActivityUtil.showSimpleDialog("无设备控制权");
                    break;
                }
                if(connectType==1){
                    showProgressDialog("提示","固件正在升级",30*1000);
                    send.sendData(Config.GETIPCVERSION, 0);
                }else if(connectType==2){
                    ZnkActivityUtil.showSimpleDialog("此功能暂不可用");
                }

                break;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_RTSP_RECEIVE);
        filter.addAction(Config.ACTION_RTSP_FAILURE);
        filter.addAction(Config.ACTION_SOCKET_FAILURE);
        filter.addAction(Config.ACTION_SOCKET_SUCCESS);

        /*网络连接不可用*/
        filter.addAction(Config.ACTION_EXTERNAL_MESSAGE);
        mReceiver = new SocketReceive();
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    private class SocketReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Config.ACTION_RTSP_RECEIVE)) {
                String value = intent.getStringExtra(Config.ACTION_EXTERNAL_MESSAGE);
                ReceiveModel model = SocketSend.get(value, ReceiveModel.class);
                if(model!=null){
                    if("200".equals(model.getHeader())){
                        Message message = new Message();
                        message.obj = model;
                        rtspHandler.sendMessage(message);
                    }else if(Config.PREVIEWCTRL.equals(model.getHeader())){ //视频文件FTP发送结束通知消息
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        rtspHandler.sendMessage(message);
                    }else if(Config.NOTICECONTROL.equals(model.getHeader())){ //锁定控制（通知）
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        rtspHandler.sendMessage(message);
                    }else if(Config.NOTICECLIENTMAX.equals(model.getHeader())){ //客户端数量上限通知
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        rtspHandler.sendMessage(message);
                    }else if(Config.NOTICERECORDSTOP.equals(model.getHeader())){ //录像停止通知消息
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        rtspHandler.sendMessage(message);
                    }else {
                        tipErrorShow(SocketSend.showError(model.getHeader()));
                    }
                }else {
                    //tipErrorShow("返回数据解析错误");
                }
            }else if(intent.getAction().equals(Config.ACTION_SOCKET_SUCCESS)){//直连成功
                   connectType =1 ;
                   search(true);
                   send.sendData(Config.IPCRECODERSTATUE, 0); //远端录像的状态
                   setAllowControl(0);
                   dismissProgressDialog();
            }else if(intent.getAction().equals(Config.ACTION_SOCKET_FAILURE)){ //直连失败改为Goolink
                unbindService();
                mLocalBroadcastManager.unregisterReceiver(mReceiver);
                if (!MyRuntime.supported()) {
                    return;
                }
                if(Config.DEVICE_CODE==null&&!isShowDialog){
                    dismissProgressDialog();
                    isShowDialog = true;
                    sendData(Config.ACTION_DEVICE_FAILURE);
                    return;
                }
                try {
                    gClient = GlnkClient.getInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //初始化预连接
                gClient.init(getApplication(), AppUtils.getAppName(CameraMainActivity.this), fmt.format(new Date()),
                        UUID.randomUUID().toString(), AppUtils.getVersionCode(CameraMainActivity.this), 1);
                gClient.setStatusAutoUpdate(true);//设备状态自动更新
                int re = gClient.start();
                if(re==0){
                   play(Config.DEVICE_CODE.substring(6,16), Config.mUserName, Config.mPassword);
                }
            }
        }
    }

    //GooLink连接开始
    private boolean play(String gid, String user, String password) {
        liveSource = new GooLinkDataSource();
        liveChannel = new GlnkChannel(liveSource);
        /**
         * setMetaData的后三个参数:
         * channelNO - 通道号，从0到31.
         * streamType - 码流类型, 0:主, 1:次 码流 3: 通道
         * dataType - 流数据类型, 0:视频流, 1:音频流, 2:音视频流
         */
        liveChannel.setMetaData(gid, user, password, 0, 1, 2);
        int ret = liveChannel.start();
        if (ret < 0) {
            return false;
        }
        return true;
    }

    class GooLinkDataSource extends DataSourceListener2 {

        // 开始连接
        @Override
        public void onConnecting() {
        }

        // 连接处理函数
        //mode 当前设备的连接模式 =1: p2p, 2: relay, 3: 分发
        @Override
        public void onConnected(int mode, String ip, int port) {
            connectType =2 ;
            search(true);
            setGooLinkSend(DigitalTrans.intToByteArray(GooLink_RECORDER_QUEST));//查询录像状态
            setAllowControl(0);
            dismissProgressDialog();
            //定时器启动心跳保活

        }

        // 连接模式改变
        @Override
        public void onModeChanged(int mode, String ip, int port) {

        }

        /**
         *连接断开
         * @param errorCode -2: 设备离线 -1: 非法id 0: 连接已断开
         */
        @Override
        public void onDisconnected(int errorCode) {
             isShowDialog = true;
             dismissProgressDialog();
             sendData(Config.ACTION_DEVICE_FAILURE);
             Log.e("GooLink",""+errorCode);

        }

        /**重连开始*/
        @Override
        public void onReConnecting() {

        }

        /** 登录响应 1成功 2用户名或密码错误*/
        @Override
        public void onAuthorized(int result) {

        }

        /**每秒的数据流量*/
        @Override
        public void onDataRate(int bytesSecond) {

        }

        /**音视频模式信息*/
        @Override
        public void onAVStreamFormat(byte[] data) {

        }

        @Override
        public void onVideoData(final byte[] data, int frameIndex, int timestamp,
                                boolean isIFrame) {
            if(mGooLinkSource!=null){
                mGooLinkSource.onVideoData(data,frameIndex,timestamp,isIFrame);
            }
        }

        @Override
        public void onAudioData(byte[] data, int timestamp) {
            if(mGooLinkSource!=null){
                mGooLinkSource.onAudioData(data, timestamp);
            }

        }

        // 透明通道数据
        @Override
        public void onIOCtrlByManu(byte[] data) {
            if(data!=null) {
                // String str = DigitalTrans.bytetoString(data);
                // ZnkActivityUtil.showSimpleDialog(str);
                int result = DigitalTrans.byteArrayToInt(data); //10进制
                if(data.length==13){
                    pictureDirName =DigitalTrans.bytetoString(data);//全景文件名
                }
                Message message = new Message();
                message.what = result;
                gooLinkHandler.sendMessage(message);
                if(mGooLinkSource!=null){
                    mGooLinkSource.onIOCtrlByMenu(data);
                }
            }
        }

        @Override
        public void onIOCtrl(int type, byte[] data) {
            super.onIOCtrl(type, data);
        }

        @Override
        public void onTalkingResp(int result, int audio, int audioChannels,
                                  int audioSampleRate, int audioBitsPerSample) {
            if(mGooLinkSource!=null){
               mGooLinkSource.onTalkingResp(result, audio, audioChannels, audioSampleRate,
                        audioBitsPerSample);
            }

        }

        /**心跳保活*/
        @Override
        public void onKeepliveResp(int result) {
            super.onKeepliveResp(result);
        }
    }

    public interface GooLinkSource {

        void onVideoData(final byte[] data, int frameIndex, int timestamp,
                         boolean isIFrame);

        void onAudioData(byte[] data, int timestamp);

        void onIOCtrlByMenu(byte[] data);

        void onTalkingResp(int result, int audio, int audioChannels,
                           int audioSampleRate, int audioBitsPerSample);
    }

    public void setGooLinkSource(GooLinkSource gooLinkDataSource) {
        mGooLinkSource = gooLinkDataSource;
    }


    Handler rtspHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            ReceiveModel model = ((ReceiveModel)msg.obj);
            switch (model.getMsgType().toString()) {
                case Config.RECORDERSTART:
                    isLongDistance = true;
                    imageLongDistance.setImageResource(R.drawable.icon_video_stop);
                    textLongDistance.setText("停止");
                    break;
                case Config.RECORDERSTOP:
                    isLongDistance = false;
                    imageLongDistance.setImageResource(R.drawable.icon_long_distance);
                    textLongDistance.setText("远程录像");
                    break;
                case Config.IPCRECODERSTATUE:
                    if(model.getIsRecodering()==1){ //远端录像中
                        isLongDistance = true;
                        imageLongDistance.setImageResource(R.drawable.icon_video_stop);
                        textLongDistance.setText("停止");
                    }else {
                        isLongDistance = false;
                        imageLongDistance.setImageResource(R.drawable.icon_long_distance);
                        textLongDistance.setText("远程录像");
                    }
                    break;
                case Config.NOTICERECORDSTOP:
                    isLongDistance = false;
                    imageLongDistance.setImageResource(R.drawable.icon_long_distance);
                    textLongDistance.setText("远程录像");
                    break;
                case Config.NOTICECONTROL:
                     if(model.getIsControl()==1){ //允许控制
                         setAllowControl(1);
                     }else {
                         //setAllowControl(0);
                         showFailDialog("提示", "设备被占用，请选择其他设备");
                     }
                    break;
                case Config.GETIPCVERSION:
                    checkUpdate(model.getVersion());
                    break;
                case Config.UPDATEIPCVERSION:  //
                    tipErrorShow("固件升级成功");
                    break;
                case Config.REQUESTALLPICTURE:
                    wholeScene.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);
                    pictureDirName = model.getPictureDirName();
                    updateImage.postDelayed(runnable, sleep);//每10秒执行一次runnable.
                    break;
                case Config.GETIPCISCONTROL:
                    //dismissProgressDialog();
                    if(model.getIsControl()==1){ //允许控制
                        setAllowControl(1);
                    }else {
                        //setAllowControl(0);
                        showFailDialog("提示", "设备被占用，请选择其他设备");
                    }
                    break;

            }
            super.handleMessage(msg);
        }
    };

    Handler gooLinkHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GooLink_RECORDER_OK: //设置录制成功
                    if(!isLongDistance){
                        isLongDistance = true;
                        imageLongDistance.setImageResource(R.drawable.icon_video_stop);
                        textLongDistance.setText("停止");
                    }else {
                        isLongDistance = false;
                        imageLongDistance.setImageResource(R.drawable.icon_long_distance);
                        textLongDistance.setText("远程录像");
                    }
                    break;
                case GooLink_RECORDER_NO_SDCARD: //摄像头内无SD卡
                    tipErrorShow("摄像头内无SD卡");
                    break;
                case GooLink_RECORDER_LOW_SDCARD: //摄像头内SD卡剩余容量不足
                    tipErrorShow("摄像头内SD卡剩余容量不足");
                    break;
                case GooLink_RECORDER_WRONG_LOGIC: //控制逻辑错误
                    tipErrorShow("录像正在进行");
                    break;
                case GooLink_RECORDER_RESP_YES: //正在录像状态
                    isLongDistance = true;
                    imageLongDistance.setImageResource(R.drawable.icon_video_stop);
                    textLongDistance.setText("停止");
                    break;
                case GooLink_RECORDER_RESP_NO: //非正在录像状态
                    isLongDistance = false;
                    imageLongDistance.setImageResource(R.drawable.icon_long_distance);
                    textLongDistance.setText("远程录像");
                    break;
                case GooLink_PANORAMIC_OK: //设置全景成功
                      /*  wholeScene.setVisibility(View.GONE);
                        loading.setVisibility(View.VISIBLE);*/
                    break;
                case GooLink_PANORAMIC_WRONG: //错误，非云台底座
                    tipErrorShow("错误，非云台底座");
                    break;
                case GooLink_CONTROL_RESP_YES: //有锁定控制权
                    //dismissProgressDialog();
                    setAllowControl(1);
                    break;
                case GooLink_CONTROL_RESP_NO: //没有锁定控制权
                    //setAllowControl(0);
                    //dismissProgressDialog();
                    showFailDialog("提示", "设备被占用，请选择其他设备");
                    break;
                case GooLink_RECORDER_LOW_SDCARD_STOP: //摄像头内SD卡剩余容量不足，录像停止
                    //tipErrorShow("摄像头内SD卡剩余容量不足，录像停止");
                    isLongDistance = false;
                    imageLongDistance.setImageResource(R.drawable.icon_long_distance);
                    textLongDistance.setText("远程录像");
                    break;
                case GooLink_PTZ_SUCCESS: //控制成功
                    //tipErrorShow("控制成功");
                    break;
                case GooLink_PTZ_FAIL: //控制失败
                    //tipErrorShow("控制失败");
                    break;
                case GooLink_PTZ_ING: //正在执行其它命令，无法执行当前命令
                    //tipErrorShow("正在执行其它命令，无法执行当/前命令");
                    break;
                case GooLink_PTZ_BEYOND: //位置角度不在规定范围内
                    //tipErrorShow("位置角度不在规定范围内");
                    break;
                case GooLink_PTZ_EXIST: //有云台
                    setGooLinkSend(DigitalTrans.intToByteArray(GooLink_PTZ_ANGLE_QUERY));
                    break;
                case GooLink_PTZ_NONE: //无云台
                    break;
                default:
                    int length = msg.what;
                    if(length>100000){
                        wholeScene.setVisibility(View.GONE);
                        loading.setVisibility(View.VISIBLE);
                        updateImage.postDelayed(runnable, sleep);//每10秒执行一次runnable.
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };
     /** 权限控制按钮*/
    private void setAllowControl(int flag){
        int type = getIntent().getIntExtra("LoginType",-1);
        switch (type){
            case 0:  //匿名
                wholeScene.setTag(-1);
                video.setTag(flag);
                image.setTag(-1);
                refresh.setTag(-1);
                break;
            case 1://用户
                wholeScene.setTag(flag);
                video.setTag(1);
                image.setTag(1);
                refresh.setTag(flag);
                break;
            case -1://默认
                wholeScene.setTag(flag);
                video.setTag(flag);
                image.setTag(flag);
                refresh.setTag(flag);
                break;
        }
    }

    /** 控制按钮的点击状态*/
    private void setEnabled(boolean enable ,int id){
        findViewById(id).setEnabled(enable);
    }


    /** 检测上传全景图像*/
    Handler updateImage=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            getImageStatue();
            updateImage.postDelayed(this, sleep);
        }
    };



    /**
     * 检查升级
     * @param version
     */
    private void checkUpdate(final String version) {
        // 调用用户绑定白盒子API
        Client.requestLastVer(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), Config.FINFO, version,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        LastVer response = get(value.toString(), LastVer.class);
                        String ver = response.getLastVer();
                        if (!TextUtils.equals(ver, version)) {
                            getDeviceInfo(response.getLastVer(), ver);
                        }else {
                            dismissProgressDialog();
                            ZnkActivityUtil.showSimpleDialog("提示", "当前设备为最新版本");
                        }
                    }

                    @Override
                    public void onZNKFailure(String value) {
                          dismissProgressDialog();
                          if(TextUtils.equals("错误的手机号格式",value)){
                              ZnkActivityUtil.showSimpleDialog("提示", "找不到指定设备类型的版本信息");
                          }else {
                              ZnkActivityUtil.showSimpleDialog("提示", value);
                          }

                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        dismissProgressDialog();
                        showTokenFailure(value);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

    /**
     * 固件信息
     * @param version
     */
    private void getDeviceInfo(final String version,final String last) {
        // 调用用户绑定白盒子API
        Client.requestVerInfo(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), Config.FINFO, version,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        dismissProgressDialog();
                        VerInfo response = get(value.toString(), VerInfo.class);
                        send.last = last;
                        send.fileList = response.getFileList().get(0);
                        send.sendData(Config.UPDATEIPCVERSION, 0);
                    }

                    @Override
                    public void onZNKFailure(String value) {
                        dismissProgressDialog();
                        if(TextUtils.equals("错误的手机号格式",value)){
                            ZnkActivityUtil.showSimpleDialog("提示", "找不到指定设备类型的版本信息");
                        }else {
                            ZnkActivityUtil.showSimpleDialog("提示", value);
                        }
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        dismissProgressDialog();
                        showTokenFailure(value);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

    /**
     * 上传图片情况
     */
    private void getImageStatue() {
        // 调用用户绑定白盒子API
        Client.requestUploadState(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), Config.DEVICE_CODE, pictureDirName,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        String response = get(value.toString(), String.class);
                        if (response != null && !TextUtils.equals(response, "1")) {
                            wholeScene.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                            updateImage.removeCallbacks(runnable);
                        }

                    }

                    @Override
                    public void onZNKFailure(String value) {
                        updateImage.removeCallbacks(runnable);
                        wholeScene.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                        ZnkActivityUtil.showSimpleDialog("提示", value);
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        updateImage.removeCallbacks(runnable);
                        wholeScene.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                        showTokenFailure(value);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }



}