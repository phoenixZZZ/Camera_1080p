package com.jiuan.it.ipc.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IBackService;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.model.InviteSend;
import com.jiuan.it.ipc.model.PTZCommandSend;
import com.jiuan.it.ipc.model.ReceiveModel;
import com.jiuan.it.ipc.service.BackService;
import com.jiuan.it.ipc.socket.SocketSend;
import com.jiuan.it.ipc.tools.BitmapUtils;
import com.jiuan.it.ipc.tools.DigitalTrans;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.ui.view.SurfaceRender;
import com.jiuan.it.ipc.utils.RtspFromFFMPEG;
import com.jiuan.it.ipc.utils.RtspRecordVideo;
import java.io.File;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RtspActivity extends RealTimeActivity implements View.OnClickListener{

    private GLSurfaceView mProcessView;

    private  static SurfaceRender mRender;

    private static AudioTrack mAudioTrack;

    private SocketReceive mReceiver;

    private RtspFromFFMPEG mRtspFromFFMPEG ;

    private boolean isPlay = false;

    private boolean isAudioPlay = false;

    private boolean isLed = false;

    private boolean isLock = false;

    private Lock lock_RecvPacket ,lock_DecodeFrame,lock_DecodeFrame_audio;

    private RtspRecordVideo mRecordVideo;

    private static final String TAG = "RTSPActivity";

    private final  int   CONNECT_FAIL= -1;

    private final  int   CONNECT_SUCCESS = 0;

    private String select="" ;
    private boolean IsChangeQuality = false;
    private byte[]  data_video = new byte[1920*1080*3];
    private byte[]   data_audio = new byte[1920*1080*3];
    private final double[] pts_video = new double[1];
    private final double[] pts_audio = new double[1];
    private final double[] dblVideoLastPts = new double[1];
    private final double[] dblAudioLastPts = new double[1];
    public final long[] lVideoLastPts = new long[1];
    private final int[] iRelFlag = new int[1];
    private int width = 1920,height = 1080;

    private IBackService iBackService;

    private Intent mServiceIntent;

    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean flag =  true; //是否开启STOP/START
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnClick();
        Configuration();
        init();
        start();
        //send.sendData(Config.GETIPCISCONTROL, 0); // 设备的控制权
        //send.sendData(Config.IPCPTZQUERY, 0); // 查询有无云台

    }
  /*  @Override
    public void search(boolean flag) {
        super.search(flag);
        isShowDialog = true;
        if(!flag){
            //send.sendData(Config.GETIPCISCONTROL, 0); // 设备的控制权
        }
    }*/

    @Override
    public void sendPTZ(int seekProgress) {
        super.sendPTZ(seekProgress);
        send.sendData(Config.IPCPTZANGLESET, seekProgress); // 查询当前云台位置角度值
    }

    @Override
    public void loadConnect() {
        super.loadConnect();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }

            @Override
            protected Void doInBackground(Void... params) {
                loadRTSP();
                return null;
            }

        }.execute();
    }

    @Override
    public void start() {
        super.start();
        isPlay = true;
        loadConnect();
        mServiceIntent = new Intent(this, BackService.class);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        registerReceiver();
    }

    @Override
    public void stop() {
        super.stop();
        showProgressDialog("提示", "正在关闭中", 15 * 1000);
        CloseTask closeTask = new CloseTask();
        closeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.btn_left:
                send.sendData(Config.PTZCOMMAND, PTZCommandSend.AVIOCTRL_PTZ_LEFT);
                break;
            case  R.id.btn_select:
                String value =  ((Button)viewPort.findViewById(R.id.btn_select)).getText().toString();
                showPopupWindow(0, 0, view.findViewById(R.id.btn_select), value);
                break;
            case  R.id.btn_photo:
                String imagePath = Tools.createFile(Config.IMAGE, deviceName, null, null,this);
                BitmapUtils bitmap = new BitmapUtils();
                boolean flag= bitmap.createMyBitmap(data_video,width,height,imagePath+ File.separator
                        + fmt.format(new Date()) + ".jpg");
                if(flag){
                    tipErrorShow("保存图片成功");
                }
                break;
            case  R.id.btn_microphone:
                break;
            case  R.id.btn_audio:
                if (!isAudioPlay) {
                    isAudioPlay = true;
                    setOnClickState(R.id.btn_audio, R.drawable.ic_audio_pressed_true);
                } else {
                    isAudioPlay =false ;
                    setOnClickState(R.id.btn_audio, R.drawable.ic_audio_pressed_false);
                }
                break;
            case  R.id.btn_lock:
                if (!isLock) {
                    isLock = true;
                    setOnClickState(R.id.btn_lock, R.drawable.ic_lock_pressed_true);
                    disableRotation(this);
                } else {
                    isLock = false;
                    setOnClickState(R.id.btn_lock,R.drawable.ic_lock_pressed_false);
                    enableRotation(this);
                }
                break;
            case  R.id.btn_right:
                send.sendData(Config.PTZCOMMAND, PTZCommandSend.AVIOCTRL_PTZ_RIGHT);
                break;
        }

    }
    @Override
    public void setLeftbtn() {
        super.setLeftbtn();
        send.sendData(Config.PTZCOMMAND, PTZCommandSend.AVIOCTRL_PTZ_LEFT);
    }

    @Override
    public void setRightbtn() {
        super.setLeftbtn();
        send.sendData(Config.PTZCOMMAND, PTZCommandSend.AVIOCTRL_PTZ_RIGHT);
    }

    @Override
    public void selectItem(int position,String value) {
        super.selectItem(position, value);
        select = value;
        for (int i = 0; i <quality.length; i++) {
            if(value.equals(quality[i])){
                send.sendData(Config.STREAMCTRL, 4 - i);
                //showProgressDialog("提示", "视频质量切换",30*1000);
                break;
            }
        }
        popupWindow.dismiss();
    }

    public void setOnClick(){
        /** 对讲不可用*/
        viewPort.findViewById(R.id.btn_microphone).setEnabled(false);
        viewLand.findViewById(R.id.btn_microphone).setEnabled(false);

        viewPort.findViewById(R.id.btn_select).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_select).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_photo).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_photo).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_audio).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_audio).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_lock).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_lock).setOnClickListener(this);

        btn_left.setOnClickListener(this);
        btn_right.setOnClickListener(this);

    }

    public void init(){
        mRtspFromFFMPEG = new RtspFromFFMPEG();
        mProcessView = new GLSurfaceView(this);
        mRender = new SurfaceRender(this);
        player.addView(mProcessView);
        mProcessView.setRenderer(mRender);
        mRender.setmProcessView(mProcessView);
        int minBuffSize = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_MONO,// 单声道
                AudioFormat.ENCODING_PCM_16BIT);// 一个采样点16比特-2个字节
        if (minBuffSize > 0) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    16000,
                    AudioFormat.CHANNEL_OUT_MONO,// 单声道
                    AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
                    AudioTrack.MODE_STREAM);
        }
        mAudioTrack.play();
        mRecordVideo = new RtspRecordVideo();
        lock_RecvPacket = new ReentrantLock();
        lock_DecodeFrame = new ReentrantLock();
        lock_DecodeFrame_audio = new ReentrantLock();

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_RTSP_RECEIVE);
        filter.addAction(Config.ACTION_RTSP_FAILURE);
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
                        handlerClick.sendMessage(message);
                    }else if(Config.PREVIEWCTRL.equals(model.getHeader())){ //视频文件FTP发送结束通知消息
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        handlerClick.sendMessage(message);
                    }else if(Config.NOTICECONTROL.equals(model.getHeader())){ //锁定控制（通知）
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        handlerClick.sendMessage(message);
                    }else if(Config.NOTICECLIENTMAX.equals(model.getHeader())){ //客户端数量上限通知
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        handlerClick.sendMessage(message);
                    }else if(Config.NOTICERECORDSTOP.equals(model.getHeader())){ //录像停止通知消息
                        model.setMsgType(model.getHeader());
                        //设备端发送信息
                        Message message = new Message();
                        message.obj = model;
                        handlerClick.sendMessage(message);
                    }else {
                        tipErrorShow(SocketSend.showError(model.getHeader()));
                    }
                }else {
                    //tipErrorShow("返回数据解析错误");
                }

            }
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case CONNECT_FAIL:
                    hint.setText(getResources().getString(R.string.tip_connect_fail));
                    break;
                case CONNECT_SUCCESS:
                    hint.setText(getResources().getString(R.string.tip_connect_success)); //设备名称
                    ((Button)viewPort.findViewById(R.id.btn_select)).setText(select);
                    ((Button)viewLand.findViewById(R.id.btn_select)).setText(select);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    Handler handlerClick = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            ReceiveModel model = ((ReceiveModel)msg.obj);
            switch (model.getMsgType().toString()) {
                case Config.INVITE:
                    isAudioPlay = true;
                    setOnClickState(R.id.btn_audio, R.drawable.ic_audio_pressed_true);
                    break;
                case Config.BYE:
                    isAudioPlay =false ;
                    setOnClickState(R.id.btn_audio, R.drawable.ic_audio_pressed_false);
                    break;
                case Config.STREAMCTRL:
                    ((Button)viewPort.findViewById(R.id.btn_select)).setText(select);
                    ((Button)viewLand.findViewById(R.id.btn_select)).setText(select);
                    //dismissProgressDialog();
                    break;
                case Config.GETIPCISCONTROL:
                    if(model.getIsControl()==1){ //允许控制
                        setAllowControl(true,0);
                    }else {
                        setAllowControl(false,0);
                    }
                    break;
                case Config.IPCPTZANGLESET:  // 控制摄像头转动
                    int rsep = model.getResp();
                    if(rsep ==0){
                      tipErrorShow("控制成功");
                    }else if(rsep ==1){
                      //tipErrorShow("设置失败");
                    }else if(rsep ==2){
                      //tipErrorShow("正在执行其它命令，无法执行当前命令");
                    }else if(rsep ==3){
                      //tipErrorShow("角度位置不在规定范围内");
                    }
                    break;

                case Config.IPCPTZQUERY:  // 查询有无云台
                    int query = model.getResp();
                    if(query ==1){ //有云台
                        send.sendData(Config.IPCPTZANGLEQUERY, 0); // 查询当前云台位置角度值
                    }else if(query ==0){  //无云台
                        mSeekBar.setEnabled(false);
                    }
                    break;
                case Config.IPCPTZANGLEQUERY: // 查询当前云台位置角度值
                    int angle = model.getResp();
                    if(angle ==0){
                        mSeekBar.setEnabled(true);
                        if(model.getAngleCurrent()>180){
                            mSeekBar.setProgress(360-model.getAngleCurrent());
                        }else if(model.getAngleCurrent()<=180){
                            mSeekBar.setProgress(model.getAngleCurrent()+180);
                        }
                    }else if(angle ==2){
                        tipErrorShow("正在执行其它命令，无法执行当前命令");
                    }
                    break;

            }
            super.handleMessage(msg);
        }
    };

    public void loadRTSP() {
        int ret = -1;
        String ip ="rtsp://" + Config.SOCKET_SERVER+"/stream0";///cam1/h264";
        //String ip ="rtsp://" + Config.SOCKET_SERVER+":8554/test";///cam1/h264";
        lock_RecvPacket.lock();
        ret = mRtspFromFFMPEG.init(ip, 0, 0, 1, iRelFlag);
        if (iRelFlag[0] == 3){
            width = 1920;
            height = 1080;
            select = quality[2];
        }
        else if (iRelFlag[0] == 4){
            width = 640;
            height = 480;
            select = quality[1];
        }
        else if (iRelFlag[0] == 5){
            width = 320;
            height = 240;
            select = quality[0];
        }
        IsChangeQuality = false;
        lock_RecvPacket.unlock();
        if(ret != -1){
            //解码渲染线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isPlay){
                        lock_RecvPacket.lock();
                        if (isAudioPlay)
                            mRtspFromFFMPEG.thread_RecvPacket(0,0,1);
                        else
                            mRtspFromFFMPEG.thread_RecvPacket(0, 0, 0);
                        lock_RecvPacket.unlock();
                    }
                    Log.e(TAG,"Thread1 is Over!!!");
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(isPlay){
                        lock_DecodeFrame.lock();
                        int ret_video = mRtspFromFFMPEG.thread_DecodePacket(data_video, pts_video, 0,0,0,lVideoLastPts, 0);
                        if (ret_video ==0) {
                            mRender.update(data_video, width, height);
                            Log.e(TAG, "正常播放视频！！！！！！");
                            if (pts_video[0] > 0)
                                dblVideoLastPts[0] = pts_video[0];
                            else
                                dblVideoLastPts[0] = 0;
                        }
                        else if(ret_video > 2 && ret_video < 6){
                            lock_DecodeFrame.unlock();
                            IsChangeQuality = true;
                            ChangeVideoQuality(ret_video);
                            Log.e(TAG, "切换视频质量！！！！！！");
                            break;
                        }
                        lock_DecodeFrame.unlock();
                    }
                    Log.e(TAG,"Thread2 is Over!!!");
                }
            }).start();

            //音频播放
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    while(isPlay){
                        if(isAudioPlay){
                            lock_DecodeFrame_audio.lock();
                            int ret_audio = mRtspFromFFMPEG.thread_DecodePacket_audio(data_audio, pts_audio);
                            if(ret_audio >= 0){
                                dblAudioLastPts[0] = pts_audio[0];
                                Log.e(TAG,"^^^^^^^^^^dblAudioLastPts[0]" + dblAudioLastPts[0]);
                                Log.e(TAG,"^^^^^^^^^^dblVideoLastPts[0]" + dblVideoLastPts[0]);

//                                while (dblAudioLastPts[0]*1000 - dblVideoLastPts[0] >= 4400){
                                //1080p 音视频同步算法逻辑 By Hjr 20160109 Start
                                while ((dblVideoLastPts[0]/1000) / dblAudioLastPts[0] < 1.5 && isPlay){
                                    try {
                                        Thread.sleep(10);
                                        continue;
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }

                                if ((dblVideoLastPts[0]/1000) / dblAudioLastPts[0] > 1.51){
                                    try {
                                        Thread.sleep(10);
                                        lock_DecodeFrame_audio.unlock();
                                        continue;
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }

                                //1080p 音视频同步算法逻辑 By Hjr 20160109 End
//                                if (dblAudioLastPts[0]*1000 - dblVideoLastPts[0] > 5400) {
//                                    continue;
//                                }
//                                else if (dblVideoLastPts[0] - dblAudioLastPts[0]*1000 > 5400) {
//                                    mRtspFromFFMPEG.ClearAudioBuffer();
//                                    dblAudioLastPts[0] = dblVideoLastPts[0];
//                                    continue;
//                                }

                                int re = mAudioTrack.write(data_audio, 0, ret_audio);
//                                if(re > 0){
//                                    dblAudioLastPts[0] = pts_audio[0];
//                                }

                            }
                            lock_DecodeFrame_audio.unlock();
                        }
                        else{
                            lock_DecodeFrame_audio.lock();
                            mRtspFromFFMPEG.ClearAudioBuffer();
                            lock_DecodeFrame_audio.unlock();
                        }
                    }
                    Log.e(TAG,"Thread3 is Over!!!");
                }
            }).start();
        }

    }
    private int ChangeVideoQuality(int video_Quality){
        closeDevice();
        isPlay = true;
        loadRTSP();
        return video_Quality;
    }
    private  void closeDevice(){
        isPlay = false;
        lock_RecvPacket.lock();
        lock_DecodeFrame.lock();
        lock_DecodeFrame_audio.lock();
        mRtspFromFFMPEG.close();
        lock_DecodeFrame_audio.unlock();
        lock_DecodeFrame.unlock();
        lock_RecvPacket.unlock();
        data_video=null;
        data_audio = null;
    }

    class CloseTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                mLocalBroadcastManager.unregisterReceiver(mReceiver);
                closeDevice();
            } catch (Exception e) {

            }
            return null;
        }

        //当在上面方法中调用publishProgress时，该方法触发该方法在UI线程中被执行
        protected void onProgressUpdate(Integer...progress){
        }

        protected void onPostExecute(Void result){
            backPressed = true;
            ZnkActivityUtil.finishActivity();
        }

        protected void onPreExecute(){

        }

    }
}
