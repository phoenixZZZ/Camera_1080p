package com.jiuan.it.ipc.ui;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IPCameraApplication;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.model.AudioData;
import com.jiuan.it.ipc.tools.DigitalTrans;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.utils.AudioDecode;
import com.jiuan.it.ipc.utils.H264DecodeUtil;
import com.jiuan.it.ipc.tools.BitmapUtils;
import com.jiuan.it.ipc.ui.view.SurfaceRender;
import com.jiuan.it.ipc.utils.RtspFromFFMPEG;
import com.jiuan.it.ipc.model.VideoData;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import glnk.io.GlnkCode;

public class GoolinkActivity extends RealTimeActivity implements View.OnClickListener ,
        CameraMainActivity.GooLinkSource{

    /** 视频显示控件 */
    private GLSurfaceView mProcessView;
    /** 视频渲染器 */
    static SurfaceRender mRender;
    /** JNI调取类 (录音)*/
    private H264DecodeUtil mFFmpegUtils ;
    /** JNI调取类 (音视频解码)*/
    private RtspFromFFMPEG rtsp;
    /** JNI调取类 (对讲)*/
    private AudioDecode mAacDecodeUtil;
    /** 音频播放*/
    private AudioTrack mAudioTrack;
    /**音频录制*/
    private RecordTask recorder;

    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;

    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    /**判断条件*/
    private boolean isLedOn = false;

    private boolean alarmOn = false;

    private boolean recordControlStart = false;

    private boolean talkingStart = false;

    private boolean isPlay = false;

    private boolean isAudioPlay = false;

    private boolean isMicrophonePlay = false;

    private boolean isLock = false;

    private Lock lock_video= new ReentrantLock();

    private Lock lock_audio= new ReentrantLock();

    private Lock lock_video_thread = new ReentrantLock();

    private Lock lock_audio_thread = new ReentrantLock();

    private Lock lock_recoder = new ReentrantLock();

    private volatile int iLastAudioTimeStamp;

    private volatile int iLastVideoTimeStamp;

    private String qualityName ="" ;

    private VideoData mVideoData;

    private  AudioData  mAudioData;

    private static final String TAG = "GooLinkActivity";

    public static int[] width = new int[1];

    public static int[] height = new int[1];

    private  byte[] outData_AudioData = new byte[4096];

    private  byte[] outData_VideoData = new byte[1920*1080*3];

    private CameraMainActivity mCameraMainActivity =new CameraMainActivity();

    private boolean flag =  true; //是否开启STOP/START

    /** 锁定控制*/
    public final int GooLink_CONTROL_QUEST = 0xA700;//查询是否拥有控制权

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

    private  boolean create = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnClick();
        Configuration();
        init();
        start();
        setGooLinkSend(DigitalTrans.intToByteArray(GooLink_CONTROL_QUEST)); //查询控制权
        setGooLinkSend(DigitalTrans.intToByteArray(GooLink_PTZ_QUERY));//查询有无云台
        // 清空缓存区数据
        IPCameraApplication.videoDataToPlay.clear();
        IPCameraApplication.audioDataList.clear();
        create = true;
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
                loadGooLink();
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void start() {
        super.start();
        loadConnect();
        mCameraMainActivity.setGooLinkSource(this);
    }

    @Override
    public void stop() {
        super.stop();
        showProgressDialog("提示", "正在关闭中", 15 * 1000);
        //th.start();
        CloseTask closeTask =new CloseTask();
        closeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void sendPTZ(int seekProgress) {
        super.sendPTZ(seekProgress);
        setGooLinkSend(DigitalTrans.intToByteArray(seekProgress));//设备的控制权
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.btn_left://摄像头左侧
                if(liveChannel!=null){
                    liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_LEFT, 4);
                    liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_STOP, 4);
                }
                break;
            case  R.id.btn_select: //视频图像选择
                String value =  ((Button)viewPort.findViewById(R.id.btn_select)).getText().toString();
                showPopupWindow(0, 0, view.findViewById(R.id.btn_select),value);
                break;
            case  R.id.btn_photo: //照片
                String imagePath = Tools.createFile(Config.IMAGE, deviceName, null, null,this);
                BitmapUtils bitmap = new BitmapUtils();
                boolean flag= bitmap.createMyBitmap(mVideoData.getVideoData(), mVideoData.getWidth(), mVideoData.getHeight(),imagePath+ File.separator
                        + fmt.format(new Date()) + ".jpg");
                if(flag){
                    tipErrorShow("保存图片成功");
                }
                break;
            case  R.id.btn_microphone://话筒
                if (!isMicrophonePlay) {
                    isMicrophonePlay = true;
                    setOnClickState(R.id.btn_microphone, R.drawable.voice_selector_true);
                    viewPort.findViewById(R.id.btn_audio).setEnabled(false);
                    viewLand.findViewById(R.id.btn_audio).setEnabled(false);
                } else if(isMicrophonePlay){
                    isMicrophonePlay = false;
                    setOnClickState(R.id.btn_microphone, R.drawable.voice_selector);
                    viewPort.findViewById(R.id.btn_audio).setEnabled(true);
                    viewLand.findViewById(R.id.btn_audio).setEnabled(true);
                }

                MicroPhoneTask microPhoneTask = new MicroPhoneTask();
                microPhoneTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case  R.id.btn_audio: //听筒
                if(!isAudioPlay){
                    isAudioPlay = true;
                    setOnClickState(R.id.btn_audio, R.drawable.audio_selector_true);
                    viewPort.findViewById(R.id.btn_microphone).setEnabled(false);
                    viewLand.findViewById(R.id.btn_microphone).setEnabled(false);
                }else if(isAudioPlay){
                    isAudioPlay = false;
                    setOnClickState(R.id.btn_audio, R.drawable.audio_selector);
                    viewPort.findViewById(R.id.btn_microphone).setEnabled(true);
                    viewLand.findViewById(R.id.btn_microphone).setEnabled(true);
                }
                AudioTask audioTask = new AudioTask();
                audioTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case  R.id.btn_lock: //锁屏
                if (!isLock) {
                    isLock = true;
                    setOnClickState(R.id.btn_lock, R.drawable.lock_selector_true);
                    disableRotation(this);
                } else {
                    isLock = false;
                    setOnClickState(R.id.btn_lock,R.drawable.lock_selector);
                    enableRotation(this);
                }
                break;
            case  R.id.btn_right://摄像头右侧
                if(liveChannel!=null){
                    liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_RIGHT, 4);
                    liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_STOP, 4);
                }
                break;
        }

    }

    @Override
    public void setLeftbtn() {
        super.setLeftbtn();
        if(liveChannel!=null){
            liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_LEFT, 4);
            liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_STOP, 4);
        }

    }

    @Override
    public void setRightbtn() {
        super.setLeftbtn();
        if(liveChannel!=null){
            liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_RIGHT, 4);
            liveChannel.sendPTZCmd(GlnkCode.PTZCMD.PTZ_MV_STOP, 4);
        }

    }

    @Override
    public void selectItem(int position,String value) {
        super.selectItem(position, value);
        for (int i = 0; i <quality.length; i++) {
            if(value.equals(quality[i])){
                sendHandle(control,8-i,value);
                break;
            }
        }
        popupWindow.dismiss();
    }

    public void setOnClick(){
        viewPort.findViewById(R.id.btn_select).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_select).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_photo).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_photo).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_microphone).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_microphone).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_audio).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_audio).setOnClickListener(this);
        viewPort.findViewById(R.id.btn_lock).setOnClickListener(this);
        viewLand.findViewById(R.id.btn_lock).setOnClickListener(this);

        btn_left.setOnClickListener(this);
        btn_right.setOnClickListener(this);

    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void init(){
        mProcessView = new GLSurfaceView(this);
        mRender = new SurfaceRender(this);
        player.addView(mProcessView);
        mProcessView.setRenderer(mRender);
        mRender.setmProcessView(mProcessView);

        int minBuffSize = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_STEREO, //双声道
                AudioFormat.ENCODING_PCM_16BIT); //一个采样点16比特-2个字节
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                minBuffSize, AudioTrack.MODE_STREAM);

        mAudioTrack.setVolume(100f);
        mFFmpegUtils = new H264DecodeUtil();
        mAacDecodeUtil = new AudioDecode();
        rtsp = new RtspFromFFMPEG();


    }
  /*  @Override
    public void search(boolean flag) {
        super.search(flag);
        isShowDialog = true;
        if(!flag){
            //liveChannel.sendManuData(DigitalTrans.intToByteArray(GooLink_CONTROL_QUEST)); //设备的控制权
        }
    }*/
    /**
     * 实现初始化操作
     */
    Handler init = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case 0:
                    hint.setText(getResources().getString(R.string.tip_connect_success)); //设备名称
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 实现控制项操作
     */
    Handler control = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case 6: //控制操作-高清
                    setGooLinkSend(DigitalTrans.intToByteArray(GooLink_VIDEO_QUALITY_HIGH));
                    ((Button)viewPort.findViewById(R.id.btn_select)).setText(msg.obj.toString());
                    ((Button)viewLand.findViewById(R.id.btn_select)).setText(msg.obj.toString());
                     qualityName = msg.obj.toString();
                    //showProgressDialog("提示","视频质量切换",30*1000);
                    break;
                case 7: //控制操作-清晰
                     setGooLinkSend(DigitalTrans.intToByteArray(GooLink_VIDEO_QUALITY_MIDDLE));
                    ((Button)viewPort.findViewById(R.id.btn_select)).setText(msg.obj.toString());
                    ((Button)viewLand.findViewById(R.id.btn_select)).setText(msg.obj.toString());
                    qualityName = msg.obj.toString();
                    //showProgressDialog("提示","视频质量切换",30*1000);
                    break;
                case 8: //控制操作-流畅
                    setGooLinkSend(DigitalTrans.intToByteArray(GooLink_VIDEO_QUALITY_LOW));
                    ((Button)viewPort.findViewById(R.id.btn_select)).setText(msg.obj.toString());
                    ((Button)viewLand.findViewById(R.id.btn_select)).setText(msg.obj.toString());
                     qualityName = msg.obj.toString();
                    //showProgressDialog("提示","视频质量切换",30*1000);
                    break;
                case 9:
                    ((Button)viewPort.findViewById(R.id.btn_select)).setText(qualityName);
                    ((Button)viewLand.findViewById(R.id.btn_select)).setText(qualityName);
                    break;

            }
            super.handleMessage(msg);
        }
    };
    public void loadGooLink(){
            mFFmpegUtils.registeriInit();
            mAacDecodeUtil.init();
            mAacDecodeUtil.findG711aDecoder();
            isPlay = true;
            // 渲染线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    while (isPlay && liveChannel != null) {
                        if (IPCameraApplication.videoDataToPlay != null
                                && IPCameraApplication.videoDataToPlay.size() > 0) {
                            lock_video_thread.lock();
                            mVideoData = IPCameraApplication.videoDataToPlay.remove(0);
                            if (mVideoData.getVideoData() != null) {
                                if (mVideoData.getTimeStamp() > 0)
                                    iLastVideoTimeStamp = mVideoData.getTimeStamp();
                                else
                                    iLastVideoTimeStamp = 0;
                                mRender.update(mVideoData.getVideoData(),mVideoData.getWidth(),mVideoData.getHeight());
                            }
                            lock_video_thread.unlock();
                        }
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    while (isPlay && liveChannel != null) {
                        if(isAudioPlay && IPCameraApplication.audioDataList != null
                                && IPCameraApplication.audioDataList.size() > 0){
                            lock_audio_thread.lock();
                            mAudioData = IPCameraApplication.audioDataList.remove(0);
                            if (mAudioData.getAudioData() != null) {
                                //Modify By Hjr 20151106 Start
                                //音视频同步，音频如果超过/落后于视频，该帧不进行播�
                                //500是个魔法数，有问题请修改
                                iLastAudioTimeStamp = mAudioData.getTimeStamp();
                                if (isAudioPlay) {
                                    if (iLastAudioTimeStamp - iLastVideoTimeStamp > 500) {
                                        continue;
                                    }
                                    else if (iLastVideoTimeStamp - iLastAudioTimeStamp  > 500) {
                                        IPCameraApplication.audioDataList.clear();
                                        iLastAudioTimeStamp = iLastVideoTimeStamp;
                                        continue;
                                    }
                                }
                                //Modify By Hjr 20151106 End
                                int ret = mAudioTrack.write(mAudioData.getAudioData(), 0, mAudioData.getFrameNumber());
                                if(ret > 0){
                                    Log.d(TAG, "write......"+mAudioData.getFrameNumber());
                                    iLastAudioTimeStamp = mAudioData.getTimeStamp();
                                }
                            }
                            lock_audio_thread.unlock();
                        }
                    }
                }
            }).start();

    }

    @Override
    public void onVideoData(byte[] data, int frameIndex, int timestamp, boolean isIFrame) {
        if (isPlay) {
            lock_video.lock();
            VideoData mVideoData = new VideoData();
            int ret = mFFmpegUtils.decode(data, data.length, width, height, outData_VideoData);
            if (ret == 0) {
                Log.d(TAG, "mFFmpegUtils.decode Sussecful!!");
                mVideoData.setVideoData(outData_VideoData);
                mVideoData.setWidth(width[0]);
                mVideoData.setHeight(height[0]);
                mVideoData.setFrameNumber(frameIndex);
                if (mVideoData.getVideoData() != null) {
                    mVideoData.setTimeStamp(timestamp);
                    IPCameraApplication.videoDataToPlay.add(mVideoData);
                }
                if(create){
                    create = false;
                    if(mVideoData.getWidth()==1920){
                        qualityName = quality[2];
                        sendHandle(control, 9,"" );
                    }else if(mVideoData.getWidth()==640){
                        qualityName = quality[1];
                        sendHandle(control, 9, "");
                    }else if(mVideoData.getWidth()==320){
                        qualityName = quality[0];
                        sendHandle(control, 9, "");
                    }
                }
            }
            lock_video.unlock();
        }
    }
    @Override
    public void onAudioData(byte[] data, int timestamp) {
        if (isAudioPlay) {
            lock_audio.lock();
            int re = mAacDecodeUtil.g711aDecode(data, data.length,outData_AudioData);
            if(re != -1){
                AudioData mAudioData = new AudioData();
                mAudioData.setAudioData(outData_AudioData);
                mAudioData.setFrameNumber(re);
                if (mAudioData.getAudioData() != null) {
                    mAudioData.setTimeStamp(timestamp);
                    IPCameraApplication.audioDataList.add(mAudioData);
                }

            }
            lock_audio.unlock();
        }
    }

    @Override
    public void onIOCtrlByMenu(byte[] data) {
        int result = DigitalTrans.byteArrayToInt(data); //10进制
        Message msg = new Message();
        msg.what = result;
        handler.sendMessage(msg);
    }

    @Override
    public void onTalkingResp(int result, int audiofmt, int audioChannels, int audioSampleRate, int audioBitsPerSample) {
        recorder = new RecordTask();
        recorder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what){
                case GooLink_VIDEO_QUALITY_OK: //视频质量成功
                    sendHandle(control, 9, "");
                    break;
                case GooLink_CONTROL_RESP_YES: //有锁定控制权
                    setAllowControl(true, 1);
                    break;
                case GooLink_CONTROL_RESP_NO: //没有锁定控制权
                    setAllowControl(false, 1);
                    break;
                case GooLink_PTZ_SUCCESS: //控制成功
                    tipErrorShow("控制成功");
                    break;
                case GooLink_PTZ_FAIL: //控制失败
                    //tipErrorShow("控制失败");
                    break;
                case GooLink_PTZ_ING: //正在执行其它命令，无法执行当前命令
                    //tipErrorShow("正在执行其它命令，无法执行当前命令");
                    break;
                case GooLink_PTZ_BEYOND: //位置角度不在规定范围内
                    //tipErrorShow("位置角度不在规定范围内");
                    break;
                case GooLink_PTZ_EXIST: //有云台
                    setGooLinkSend(DigitalTrans.intToByteArray(GooLink_PTZ_ANGLE_QUERY));
                    break;
                case GooLink_PTZ_NONE: //无云台
                    mSeekBar.setEnabled(false);
                    break;
                default:
                    mSeekBar.setEnabled(true);
                    int progress = msg.what;
                    if(progress>180){
                        mSeekBar.setProgress(360-progress);
                    }else if(progress<=180){
                        mSeekBar.setProgress(progress+180);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };


    /** 开始对讲*/
    class RecordTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = 640;//AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                //实例化AudioRecord
                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, channelConfig, audioEncoding, bufferSize);
                //定义缓冲
                byte[] buffer = new byte[bufferSize];
                //开始录音
                record.startRecording();

                //定义循环，根据isRecording的值来判断是否继续录制
                int iCount = 1;

                while(isMicrophonePlay){
                    lock_recoder.lock();
                    //从bufferSize中读取字节，返回读取的short个数
                    int bufferReadResult = record.read(buffer, 0, buffer.length);
                    if(bufferReadResult>0){
                        byte[] out = new byte[bufferReadResult / 2];
                        int dataLen = rtsp.PCM2G711a(buffer,out,bufferReadResult);
                        if (liveChannel !=null && dataLen > 0) {
                            liveChannel.sendAudioData(iCount, out);
                        }
                    }
                    iCount++;
                    lock_recoder.unlock();
                }
                //录制结束
                record.stop();

            } catch (Exception e) {

            }
            return null;
        }

        //当在上面方法中调用publishProgress时，该方法触发该方法在UI线程中被执行
        protected void onProgressUpdate(Integer...progress){
        }

        protected void onPostExecute(Void result){
        }

        protected void onPreExecute(){

        }

    }

    int i=0;
    /** 话筒开启*/
    class MicroPhoneTask extends AsyncTask<Void, Integer, Integer> {
        @Override
        protected Integer doInBackground(Void... arg0) {
            int ret = 0;
            i++;
            try {
                Log.e("GoolinkActivity","话筒开启"+i);
                if (liveChannel != null) {
                    if (isMicrophonePlay) {
                        Log.e("GoolinkActivity", "startTalking1" );
                        ret = liveChannel.startTalking();
                    } else if(!isMicrophonePlay){
                        lock_recoder.lock();
                        Log.e("GoolinkActivity", "stopTalking1");
                        liveChannel.stopTalking();
                        lock_recoder.unlock();

                    }
                }

            } catch (Exception e) {

            }
            return ret;
        }

        //当在上面方法中调用publishProgress时，该方法触发该方法在UI线程中被执行
        protected void onProgressUpdate(Integer...progress){

        }

        protected void onPostExecute(Integer result){
            Log.e("GoolinkActivity", "话筒结束" + i);
            if(result<0){
                isMicrophonePlay = false;
                setOnClickState(R.id.btn_microphone, R.drawable.ic_voice_pressed_false);
                tipErrorShow("对讲失败");
            }
            viewPort.findViewById(R.id.btn_microphone).setEnabled(true);
            viewLand.findViewById(R.id.btn_microphone).setEnabled(true);
        }

        protected void onPreExecute(){
        }

    }

    /** 听筒开启*/
    class AudioTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            i++;
            Log.e("GoolinkActivity","听筒开启"+i);
            try {
                if(isAudioPlay){
                    mAudioTrack.play();
                    Log.e("GoolinkActivity", "play" );
                }else if(!isAudioPlay){
                    Log.e("GoolinkActivity", "stop1");
                    lock_audio_thread.lock();
                    mAudioTrack.stop();
                    Log.e("GoolinkActivity", "stop2");
                    lock_audio_thread.unlock();
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        //当在上面方法中调用publishProgress时，该方法触发该方法在UI线程中被执行
        protected void onProgressUpdate(Integer...progress){
        }

        protected void onPostExecute(Void result){
            Log.e("GoolinkActivity", "听筒结束" + i);
            viewPort.findViewById(R.id.btn_audio).setEnabled(true);
            viewLand.findViewById(R.id.btn_audio).setEnabled(true);
        }

        protected void onPreExecute(){
        }

    }

    class CloseTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                if(isMicrophonePlay){
                    isMicrophonePlay = false;
                    lock_recoder.lock();
                    liveChannel.stopTalking();
                    lock_recoder.unlock();
                }
                if(isAudioPlay){
                    isAudioPlay = false;
                    lock_audio_thread.lock();
                    mAudioTrack.stop();
                    mAudioTrack.release();
                    lock_audio_thread.unlock();
                }
                isPlay = false;
                lock_video.lock();
                lock_video_thread.lock();
                mFFmpegUtils.close();
                lock_video_thread.unlock();
                lock_video.unlock();

                isAudioPlay = false;
                lock_audio.lock();
                lock_audio_thread.lock();
                mAacDecodeUtil.close();
                lock_audio_thread.unlock();
                lock_audio.unlock();
            } catch (Exception e) {
                 Log.e(TAG,e.toString());
            }
            return null;
        }

        //当在上面方法中调用publishProgress时，该方法触发该方法在UI线程中被执行
        protected void onProgressUpdate(Integer...progress){
        }

        protected void onPostExecute(Void result){
            dismissProgressDialog();
            backPressed = true;
            ZnkActivityUtil.finishActivity();
        }

        protected void onPreExecute(){

        }

    }
    Thread th = new Thread(new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
            if(isMicrophonePlay){
                isMicrophonePlay = false;
                lock_recoder.lock();
                liveChannel.stopTalking();
                lock_recoder.unlock();
            }
            if(isAudioPlay){
                isAudioPlay = false;
                lock_audio_thread.lock();
                mAudioTrack.stop();
                mAudioTrack.release();
                lock_audio_thread.unlock();
            }
            isPlay = false;
            lock_video.lock();
            lock_video_thread.lock();
            mFFmpegUtils.close();
            lock_video_thread.unlock();
            lock_video.unlock();

            isAudioPlay = false;
            lock_audio.lock();
            lock_audio_thread.lock();
            mAacDecodeUtil.close();
            lock_audio_thread.unlock();
            lock_audio.unlock();

            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessage(msg);
            } catch (Exception e) {
            Log.e(TAG,e.toString());
        }
        }
    });

    public Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            dismissProgressDialog();
            backPressed = true;
            ZnkActivityUtil.finishActivity();
        }

    };
}
