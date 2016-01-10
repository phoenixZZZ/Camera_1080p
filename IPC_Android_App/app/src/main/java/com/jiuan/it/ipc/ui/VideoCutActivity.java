package com.jiuan.it.ipc.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.model.PreviewHeader;
import com.jiuan.it.ipc.model.PreviewReceiveModel;
import com.jiuan.it.ipc.model.VideoHeader;
import com.jiuan.it.ipc.model.VideoImage;
import com.jiuan.it.ipc.tools.DensityUtils;
import com.jiuan.it.ipc.tools.DigitalTrans;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.ui.view.SurfaceRender;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.jiuan.it.ipc.utils.CombineVideo;
import com.jiuan.it.ipc.utils.RtspFromFFMPEG;
import com.jiuan.it.ipc.utils.RtspRecordVideo;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class VideoCutActivity extends  PreviewActivity implements
        CustomToolbar.OnClickCustomToolbarListener{

    /** 渲染器*/
    private GLSurfaceView mProcessView;
    /** 渲染器自定义接口 */
    private  static SurfaceRender mRender;
    /** 视频播放（JNI） */
    private RtspFromFFMPEG mRtspFromFFMPEG ;
    /** 音频播放（JNI） */
    private RtspRecordVideo mRecordVideo;
    /** 合成视频（JNI） */
    private CombineVideo mCombineVideo;
    /** 是否中断播放 */
    private boolean isPlay = false;
    /** 是否中断音频 */
    private boolean isAudioPlay = false;
    /** 接包线程锁 */
    private Lock lock_RecvPacket ;
    /** 解包、播放线程锁 */
    private Lock lock_DecodeFrame ;
    /** 音频线程锁 */
    //private Lock lock_DecodeFrame_audio ;
    /** 游标线程锁 */
    //private Lock lock_seek ;
    /**打印TAG*/
    private static final String TAG = "VideoCutActivity";
   /* *//**返回*//*
    private TextView img_back;
    *//**确认*//*
    private TextView txt_enter;*/
    /** 横向listview */
    private GridLayout recyclerView;
    /** 封面图按钮 */
    private ImageView img_bg;
    /** 阴影色块left */
    private ImageView img_left;
    /** 阴影色块right */
    private ImageView img_right;
    /** 显示时间 */
    private TextView txt_time;
    /** 封面容器 */
    private RelativeLayout relative;
    /** 进度条 */
    private RelativeLayout relative1;
    /** 视频播放 */
    private LinearLayout player ;
    /** 数据集 */
    private ArrayList<PreviewHeader> headerData;
    /** 数据集 */
    private ArrayList<VideoHeader> videoHeaders;
    /** 数据集 Add By Hjr 20151207*/
    private ArrayList<VideoImage> videoImageHeaders;
    /** 屏幕宽度 */
    private int screenWidth;
    /** 左边拖动按钮 */
    private Button txt_left;
    /** 右边拖动按钮 */
    private Button txt_right;
    /** 中间拖动按钮 */
    private SeekBar seekBar;
    /** 按下时X抽坐标 */
    private float DownX;
    /** 拖动条容器 */
    private RelativeLayout.LayoutParams layoutParams_progress;
    /** 阴影背景容器 */
    private RelativeLayout.LayoutParams layoutParams_yin;
    /** 游标容器 */
    //private RelativeLayout.LayoutParams seekLayout;
    /** 拖动条的宽度 */
    private int width_progress = 0;
    /** 拖动条的间距 */
    private int Margin_progress = 0;
    /** 阴影框的宽度 */
    private int width1_progress = 0;
    /** 不能超过右边多少 */
    private int right_margin = 0;
    /** 最少保留的多少秒长度 */
    private int last_length = 0;
    /** 左边啦了多少 */
    //private int left_lenth = 0;
    /** 滚动的长度 */
   // private int Scroll_lenth = 0;
    /** 所有图片长度 */
    //private int img_widthAll = 0;
    /** 拖拽时间戳 */
    private long seek_pos = 0;
    /** 拖拽标记*/
    private int seek_req;
    /** timebase分母*/
    private int timebase_seekDen;
    /** timebase分子*/
    private int timebase_seekNum;
    /** 文件名称列表*/
    private List<String> fileList;
    /** 文件存在标识列表*/
    private HashMap<String,Boolean> mapList;
    /** 文件下载目录*/
    private String downloadPath ;
    /** 当前游标视频文件名称 */
    private String currentName = null;
    /** 当前播放视频文件名称 */
    private String playName = null;
    /** 当前播放视频文件所在位置 */
    private int currentPosition = 0;
    /** 单个图像宽度 */
    private float imageItemWidth;
    /** 单个图像秒数 */
    //private float imageItemSeconds;
    /** 毫秒级的移动长度 */
    private float msecLength;
    /** Seek宽度 */
    private int seekWidth = 0;
    /** 视频总时长*/
    private int totalSeconds = 0;
    /** 游标的横坐标落点 */
    private double lastX =0;
    /** 延迟参数 */
    private final int sleep = 60;
    /** 倍数*/
    private final int base = 1000;
    /** 文件拆分定值 */
    private final int split = 5;

    private final int[] iRelFlag = new int[1];

    private double ChangedLastX = 0;

    private String m_iFileName = "";

    private int m_iCurrentCount = 0;

    private boolean isPause = false;

    private boolean isCut = false;

    private boolean isFlag = false;

    private CustomToolbar toolbar =null;
    @Override
    protected int getContentViewId() {
        return R.layout.activity_video_cut;
    }

    @Override
    protected void findViews() {
        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        recyclerView = (GridLayout) findViewById(R.id.recyclerview_horizontal);
        player = (LinearLayout) findViewById(R.id.player);
        img_bg = (ImageView) findViewById(R.id.video_new_cut_img_bg);
        img_left = (ImageView) findViewById(R.id.video_new_cut_img_left);
        img_right = (ImageView) findViewById(R.id.video_new_cut_img_right);
        relative = (RelativeLayout) findViewById(R.id.video_new_cut_relative);
        txt_time = (TextView) findViewById(R.id.video_new_cut_txt_time);
        relative1 = (RelativeLayout) findViewById(R.id.video_new_cut_relative1);
        txt_left = (Button) findViewById(R.id.video_new_cut_txt_left);
        seekBar = (SeekBar) findViewById(R.id.video_new_cut_txt_center);
        txt_right = (Button) findViewById(R.id.video_new_cut_txt_right);
        //屏幕宽度
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        //标题栏宽度
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) relative.getLayoutParams();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth;
        relative.setLayoutParams(layoutParams);
        toolbar.setOnClickCuteToolbarListener(this);
    }

    @Override
    protected void init() {
        //检测下载
        fileContains(true);
        //注册接收器
        registerReceiver();
        //播放器相关设置
        mRtspFromFFMPEG = new RtspFromFFMPEG();
        mCombineVideo = new CombineVideo();
        mProcessView = new GLSurfaceView(this);
        mRender = new SurfaceRender(this);
        player.addView(mProcessView);
        mProcessView.setRenderer(mRender);
        mRender.setmProcessView(mProcessView);
        mRecordVideo = new RtspRecordVideo();
        lock_RecvPacket = new ReentrantLock();
        lock_DecodeFrame = new ReentrantLock();

        seekBar.setEnabled(true);
        float margin = getResources().getDimension(R.dimen.btn_width);
        seekWidth =(screenWidth- DensityUtils.dp2px(this, margin * 2))*base;
        seekBar.setMax(seekWidth);
        msecLength =  (((float) seekWidth/ (float) totalSeconds));
        Log.e(TAG, "msecLength" + msecLength );
        imageItemWidth = seekWidth/headerData.size();//单图片长度
        timebase_seekDen = headerData.get(0).getTimeBaseDen();
        timebase_seekNum = headerData.get(0).getTimeBaseNum();
        seekBar.setProgress(0);

        initRelayout();
    }

    /**
     * FTP的所在目录下遍历是否存在,进行指令下载
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void fileContains(boolean flag){
        //游标所在文件位置 move = lastX*(float)msecLength;
        currentPosition = (int) (lastX/imageItemWidth)/split;
        //游标所在文件名称
        currentName = headerData.get((int) (lastX/imageItemWidth)).getNamerec();
        for (int i = currentPosition; i < fileList.size(); i++) {
            String  name = fileList.get(i); //文件名称
            boolean exists =mapList.get(name); //文件存在标记
            if(!exists){ // 不存在
                //FTP请求指令
                send.ctrlBase = null;
                send.ctrlName = ctrlName;
                send.ctrlNameStart = name;
                send.ctrlNameEnd = name;
                send.sendData(Config.PREVIEWCTRL, 6);
                Log.e(TAG, "开始下载：" + "第" + i + "位置" + name);
                if(flag){
                    showProgressDialog("提示","正在下载",30*1000);
                }
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
    }


    /**
     * 播放器播放信息
     * @param fileName
     */
    int width, height;
    public void loadRtsp(String fileName, final boolean combine) {
        isPlay = true;
        playName =fileName;
        lock_RecvPacket.lock();
        int ret = mRtspFromFFMPEG.init(downloadPath+ File.separator+fileName,timebase_seekDen, timebase_seekNum, 0, iRelFlag);
        width = 0;
        height = 0;
        if (iRelFlag[0] == 3){
            width = 1920;
            height = 1080;
        }
        else if (iRelFlag[0] == 4){
            width = 640;
            height = 480;
        }
        else if (iRelFlag[0] == 5){
            width = 320;
            height = 240;
        }
        lock_RecvPacket.unlock();
        if(ret != -1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                while (isPlay) {
                    if (isPause == false){
                        lock_RecvPacket.lock();
                        //接收数据包
                        int result = mRtspFromFFMPEG.thread_RecvPacket(seek_pos, seek_req, 0);//, timebase_seekDen, timebase_seekNum
                        if(result == 1 && seek_req == 1){
                            seek_req = 0;
                        }else if(result==-1){
                            isPlay = false;
                            //游标所在文件名称
                            int seek = (int)(lastX/imageItemWidth);
                            markHandler.removeCallbacks(runnable);
                            for (int i = 0; i <videoHeaders.size(); i++) {
                                if(playName.equals(videoHeaders.get(i).getNameFile())){
                                    if(i+1>=videoHeaders.size()){
                                        lock_RecvPacket.unlock();
                                        Log.e(TAG, "文件结束");
                                        Message  message = new Message();
                                        message.what = 5;
                                        handler.sendMessage(message);
                                        return;
                                    }else {
                                        lock_RecvPacket.unlock();
                                        Message  message = new Message();
                                        message.obj =videoHeaders.get(i+1).getNameFile();
                                        message.what = 0;
                                        lastX = (seek + 1) * imageItemWidth;
                                        //ChangedLastX = lastX;
                                        handler.sendMessage(message);
                                        return;
                                    }
                                    //break;
                                }
                            }
                        }
                        else
                        {
                            lock_DecodeFrame.lock();
                            //解析数据包
                            int ret_video = mRtspFromFFMPEG.thread_DecodePacket(data_video, pts_video, isRecord, IsHaveStop, TimeStamp_Stop, lVideoLastPts, 1);
                            if (ret_video == 0) {
                                //游标计时器开启
                                markHandler.postDelayed(runnable, 0);
                                //数据播放
                                if (!combine){
                                    mRender.update(data_video, width, height);
                                    if (pts_video[0] > 0){
                                        dblVideoLastPts[0] = pts_video[0];
                                    }else{
                                        dblVideoLastPts[0] = 0;
                                    }
                                    try {
                                        Thread.sleep(sleep); //时间等待
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if(ret_video==2){
                                if(combine){
                                    result =  mRecordVideo.stop();
                                    if(result==0){
                                        isPlay =false;
                                        Log.e(TAG, "裁剪完成");
                                        initRelayout();
                                        Intent video = new Intent(getApplicationContext(),
                                                ShareVideoActivity.class);
                                        String path =downloadPath + File.separator + cutFileName + ext;
                                        video.putExtra("PATH",path);
                                        startActivity(video);

                                    }
                                }
                            }
                            lock_DecodeFrame.unlock();
                        }
                        lock_RecvPacket.unlock();
                    }
                    else{
                        try {
                            Thread.sleep(sleep); //时间等待
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                }
            }).start();
        }   }
    @Override
    protected void widgetListener() {
        /** 播放 */
        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int seek = (int) (lastX/ imageItemWidth);//时间戳
                if (!img_bg.isShown()) { //暂停
                    img_bg.setVisibility(View.VISIBLE);
                    //游标计时器停止
                    markHandler.removeCallbacks(runnable);
                    if (isPlay) {
                        Message message = new Message();
                        message.obj = playName;
                        message.what = 1;
                        handler.sendMessage(message);
                    }

                } else { //播放
                    img_bg.setVisibility(View.GONE);
                    String namerec = headerData.get(seek).getNamerec();
                    seekBar.setProgress((int)(seek*imageItemWidth));
                    if (mapList.get(namerec)) {
                        Message message = new Message();
                        message.obj = namerec;
                        message.what = 2;
                        handler.sendMessage(message);
                    }

                }
            }
        });

        /** 左边拖动按钮 */
        txt_left.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        getLayoutParams(event, 0);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        LeftMoveLayout(event.getRawX() - DownX);
                        seekBar.setVisibility(View.GONE);
                        break;
                    case MotionEvent.ACTION_UP:
                        layoutParams_progress = null;
                        layoutParams_yin = null;
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        /** 右边拖动按钮 */
        txt_right.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        getLayoutParams(event, 1);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        RightMoveLayout(DownX - event.getRawX());
                        seekBar.setVisibility(View.GONE);
                        break;
                    case MotionEvent.ACTION_UP:
                        layoutParams_progress = null;
                        layoutParams_yin = null;
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        /** 中间拖动按钮 */
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImp());
    }

    @Override
    protected void initGetData() {
        super.initGetData();
        if (getIntent().getExtras() != null) {
            headerData = getIntent().getParcelableArrayListExtra(KF);
            videoHeaders = getIntent().getParcelableArrayListExtra(KS);
            videoImageHeaders = getIntent().getParcelableArrayListExtra(KI);
            //视频文件下载目录
            downloadPath = ftpPath + File.separator + ctrlName;
            //文件名称列表
            fileList = new ArrayList<String>();
            mapList = new HashMap<String, Boolean>();
            for (int i = 0; i < headerData.size(); i++) {
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth/headerData.size(), LinearLayout.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Bitmap bitmap = Tools.getLocalBitmap(headerData.get(i).getImage(), 3);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                recyclerView.addView(imageView);
                String namerec = headerData.get(i).getNamerec();
                boolean exists =false;
                if(new File(downloadPath+File.separator+headerData.get(i).getNamerec()).exists()){
                    exists = true;
                }
                if(!fileList.contains(namerec)){
                    fileList.add(namerec);
                    mapList.put(namerec,exists);
                }
            }
            if(videoHeaders!=null&&videoHeaders.size()>0){
                totalSeconds = (int)videoHeaders.get(0).getOverall();
            }

            Log.e(TAG, "总时长"+totalSeconds);
        }
    }

    private void initRelayout(){
        layoutParams_progress = (RelativeLayout.LayoutParams) relative1.getLayoutParams();
        layoutParams_progress.width = screenWidth;
        layoutParams_progress.leftMargin = 0;
        layoutParams_progress.rightMargin = 0;
        init.sendMessage(new Message());
    }
    private Handler init = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            relative1.setLayoutParams(layoutParams_progress);
            relative1.setBackground(null);
            setToolbar(false);
            width1_progress = 0;

            layoutParams_yin = (RelativeLayout.LayoutParams) img_left.getLayoutParams();
            layoutParams_yin.width = width1_progress;
            img_left.setLayoutParams(layoutParams_yin);

            layoutParams_yin = (RelativeLayout.LayoutParams) img_right.getLayoutParams();
            layoutParams_yin.width = width1_progress;
            img_right.setLayoutParams(layoutParams_yin);

            seekBar.setVisibility(View.VISIBLE);
            layoutParams_yin = null;
            layoutParams_progress = null;
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClickLeft() {
        if(!isCut){
            ZnkActivityUtil.finishActivity();
        }else {
            initRelayout();
        }

    }

    @Override
    public void onClickRight() {
        if(!isFlag &&isCut){ //裁剪
            isFlag =true;
            showProgressDialog("提示","视频合成",30*1000);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean flag = true;
                    do{
                        if(detectedFile()){//遍历目录全部下载完成
                            videoCutOnclick();
                            flag =false;
                        }
                    }while(flag);
                }
            }).start();
        }else if(isFlag &&isCut) {
            isFlag =false;
            initRelayout();
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setToolbar(boolean flag){
        isCut =flag;
        if(!isCut){
            toolbar.setTextColorRight(Color.GRAY);
        }else {
            toolbar.setTextColorRight(Color.WHITE);
        }
    }
    private class OnSeekBarChangeListenerImp implements
            SeekBar.OnSeekBarChangeListener {

        // 触发操作，拖动
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            lastX = progress;
        }

        // 表示进度条刚开始拖动，开始拖动时候触发的操作
        public void onStartTrackingTouch(SeekBar seekBar) {
            isPause = true;
        }

        // 停止拖动时候
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            isPause = false;
            sendVideo();
        }
    }
    /**
     * 移动起始播放位置
     */
    private void sendVideo() {
        int seek = (int) (lastX/imageItemWidth);//时间戳
        String namerec = headerData.get(seek).getNamerec();
        if(!img_bg.isShown()&&isPlay){ // 播放中......
            if(!namerec.equals(playName)){
                if(mapList.get(namerec)){ //是否存在
                    Message  message = new Message();
                    message.obj =namerec;
                    message.what = 6;
                    handler.sendMessage(message);
                }else {
                    Message  message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }else {
                Message  message = new Message();
                message.obj =namerec;
                message.what = 6;
                handler.sendMessage(message);
            }
        }else {
            getSeekData(seek);
        }

    }
    /**
     *视频裁剪点击事件
     */
    private void  videoCutOnclick(){
        //1-合成视频
        if(headerData.size()==1){
            initRelayout();
            Intent video = new Intent(getApplicationContext(),
                    ShareVideoActivity.class);
            String path =downloadPath + File.separator + headerData.get(0).getNamerec();
            video.putExtra("PATH",path);
            startActivity(video);
            return;
        }
        String combine = CombineVideo();
        if(combine==null){
            Log.e(TAG,"合成失败");
            dismissProgressDialog();
            return;
        }
        //2-设置裁剪的信息
        layoutParams_progress = (RelativeLayout.LayoutParams) relative1.getLayoutParams();
        int left =(int) (layoutParams_progress.leftMargin*base / imageItemWidth);
        int right =(int) ((screenWidth-layoutParams_progress.rightMargin)*base / imageItemWidth);
        long leftDataTime= headerData.get(left).getDataTimeWhole();
        long rightDataTime= headerData.get(right).getDataTimeWhole();

        if(left ==right){
            if(right+1!=headerData.size()){
                rightDataTime = headerData.get(right+1).getDataTimeWhole();
            }else {
                rightDataTime = videoHeaders.get(right).getOverall();
            }
        }
        TimeStamp_Stop= rightDataTime;
        seek_pos = leftDataTime;
        seek_req = 1;
        timebase_seekDen = headerData.get(left).getTimeBaseDen();
        timebase_seekNum = headerData.get(left).getTimeBaseNum();
        //3-暂停视频、裁剪
        int what = 4;
        if (!img_bg.isShown())//播放中......
            what = 3;

        Message message = new Message();
        message.obj = combine;
        message.what = what;
        handler.sendMessage(message);

    }
    /**
     *合成文件
     */
    private String  CombineVideo(){
        String  inFileName1 = null;
        String  inFileName2 = null;
        String  outFileName = null;
        if(fileList.size()==1){
            return fileList.get(0);
        }
        for (int i = 1; i < fileList.size() ; i++) {
            inFileName1 = downloadPath+File.separator+fileList.get(i-1);
            if(i>1&&i<=fileList.size()-1){
              inFileName1 = downloadPath+File.separator+outFileName;
            }
            inFileName2 = fileList.get(i);
            outFileName = fmt.format(new Date()) + String.valueOf(i)+".mp4";
            int result = mCombineVideo.combine(inFileName1, downloadPath+File.separator+inFileName2,
                    downloadPath+File.separator+outFileName, 0);
            if(result<0){
                return  null;
            }

        }
        return outFileName;
    }
    /**
     *检测下载文件
     */
    private boolean detectedFile(){
        for (int i = 0; i < fileList.size(); i++) {
             if(!mapList.get(fileList.get(i))){
                 return  false;
             }
        }
        return  true;
    }

    //Add By Hjr 2015/12/07 Start
    //seekpos : 快进的时间戳
    private double getSeekLength(long seekpos){
        int iPictureNums = (int)(lastX/imageItemWidth);
        long SingleVideoDuration = 0;
        long SumVideoDuration = 0;
        int SumVideoImageCount = 0;
        String strVideoFileName = "";
        Log.e(TAG, "iPictureNums: " + iPictureNums );
        if(headerData.size() > iPictureNums) {
            //获得当前录像文件的预览图片数，以及已经播放完的录像时长（控件上位置）
            strVideoFileName = headerData.get(iPictureNums).getNamerec();
            m_iFileName = strVideoFileName;
            for(int i = 0; i < videoImageHeaders.size(); i++){
                if (strVideoFileName.equals(videoImageHeaders.get(i).getStrVideoFileName()) == true){
                    m_iCurrentCount = videoImageHeaders.get(i).getiImageCount();
                    break;
                }
                else{
                    SumVideoImageCount = SumVideoImageCount + videoImageHeaders.get(i).getiImageCount();
                }
            }
            ChangedLastX = SumVideoImageCount * imageItemWidth;

            //获得单个录像文件的录像时长
            for(int i = 0; i < videoHeaders.size(); i++){
                if (strVideoFileName.equals(videoHeaders.get(i).getNameFile()) == true){
                    SingleVideoDuration = videoHeaders.get(i).getTimeFile();
                    break;
                }
                else{
                    SumVideoDuration = SumVideoDuration + videoHeaders.get(i).getTimeFile();
                }
            }

            //计算当前的播放的时间位置，计算方法：（当前录像文件的控件长 / 当前录像时长） * 当前位置的时间戳 + 已经播放完的录像时长
            double seek = ((m_iCurrentCount) * imageItemWidth) / (SingleVideoDuration);
            if (seekpos == 0){
                lastX  = ChangedLastX + seek * lVideoLastPts[0];
            }
            else{
                lastX  = ChangedLastX + seek * seekpos;
            }
            Log.e(TAG, "#################lastX" + lastX );
        }
        return  lastX;
    }
    //Add By Hjr 2015/12/07 End

    /**
     *获得游标点的信息
     */
    private  void getSeekData(int seek){
       if(seek+1<=headerData.size()){
           //获得当前位置预览图的时间戳
           seek_pos = headerData.get(seek).getDataTimeSingle();
           getSeekLength(seek_pos);
           seek_req = 1;
       }
    }
    /**
     *切换不同文件的播放位置
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int ret = -1;
            switch (msg.what){
                case 0:
                    isPlay = false;
                    lock_RecvPacket.lock();
                    lock_DecodeFrame.lock();
                    ret = mRtspFromFFMPEG.close();
                    lock_DecodeFrame.unlock();
                    lock_RecvPacket.unlock();
                    if(ret==1&&!isPlay){
                        Log.e(TAG, "切换播放");
                        isRecord = 0;
                        IsHaveStop = 0;
                        TimeStamp_Stop= 0;
                        loadRtsp(msg.obj.toString(),false);
                    }
                    break;
                case 1:
                    int seek = (int) (lastX/imageItemWidth);//时间戳
                    isPlay = false;
                    img_bg.setVisibility(View.VISIBLE);
                    lock_RecvPacket.lock();
                    lock_DecodeFrame.lock();
                    ret = mRtspFromFFMPEG.close();
                    lock_DecodeFrame.unlock();
                    lock_RecvPacket.unlock();
                    if (ret == 1) {
                        getSeekData(seek);
                    }
                    break;
                case 2:
                    isRecord = 0;
                    IsHaveStop = 0;
                    TimeStamp_Stop= 0;
                    ChangedLastX = 0;
                    m_iFileName = "";
                    m_iCurrentCount = 0;
                    loadRtsp(msg.obj.toString(), false);
                    break;
                case 3:
                    isPlay = false;
                    lock_RecvPacket.lock();
                    lock_DecodeFrame.lock();
                    ret = mRtspFromFFMPEG.close();
                    lock_DecodeFrame.unlock();
                    lock_RecvPacket.unlock();
                    if(ret == 1){
                        //裁剪开启
                        cutFileName =  fmt.format(new Date());
                        mRecordVideo.start(downloadPath + File.separator + cutFileName + ext);
                        isRecord = 1;
                        IsHaveStop = 1;
                        loadRtsp(msg.obj.toString(),true);
                    }
                    break;
                case 4:
                    //裁剪开启
                    cutFileName =  fmt.format(new Date());
                    mRecordVideo.start(downloadPath + File.separator + cutFileName + ext);
                    isRecord = 1;
                    IsHaveStop = 1;
                    loadRtsp(msg.obj.toString(), true);
                    break;
                case 5:
                    isPlay = false;
                    img_bg.setVisibility(View.VISIBLE);
                    lock_RecvPacket.lock();
                    lock_DecodeFrame.lock();
                    ret = mRtspFromFFMPEG.close();
                    lock_DecodeFrame.unlock();
                    lock_RecvPacket.unlock();
                    if (ret == 1) {
                        img_bg.setVisibility(View.VISIBLE);
                        markHandler.removeCallbacks(runnable);
                        seekBar.setProgress(0);
                        lastX = 0;
                        getSeekData(0);
                    }
                    break;
                case 6:
                    int seeks = (int) (lastX/imageItemWidth);//时间戳
                    isPlay = false;
                    lock_RecvPacket.lock();
                    lock_DecodeFrame.lock();
                    ret = mRtspFromFFMPEG.close();
                    lock_DecodeFrame.unlock();
                    lock_RecvPacket.unlock();
                    if (ret == 1) {
                        getSeekData(seeks);
                        loadRtsp(msg.obj.toString(), false);
                    }
                    break;

            }

        }
    };
    /**游标执行*/
    private Handler markHandler = new Handler( );

    private Runnable runnable = new Runnable( ) {
        public void run ( ) {
          if(isPlay){
              seekBar.setProgress((int) getSeekLength(0));
          }
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        closeDevice();
    }
    private  void closeDevice(){
        if(isPlay){
            isPlay = false;
            //游标计时器停止
            markHandler.removeCallbacks(runnable);
            lock_RecvPacket.lock();
            lock_DecodeFrame.lock();
            mRtspFromFFMPEG.close();
            lock_DecodeFrame.unlock();
            lock_RecvPacket.unlock();
        }
    }

    @Override
    protected void requestVideo(PreviewReceiveModel model) {
        super.ftpPreviewFail(model);
        tipErrorShow("请求发送录像文件" + model.getDescription());
        Log.e(TAG, "请求发送录像文件" + model.getDescription());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void ftpVideo(PreviewReceiveModel model) {
        super.ftpPreviewFail(model);
        tipErrorShow("设备端发送录像文件成功");
        dismissProgressDialog();
        Log.e(TAG, "录像文件" + model.getCtrlFileName());
        mapList.remove(model.getCtrlFileName());
        mapList.put(model.getCtrlFileName(), true);
        fileContains(false);
        if(currentName.equals(model.getCtrlFileName())&&
             !img_bg.isShown() &&!isPlay){ //等待播放曲目
            Message  message = new Message();
            message.obj =model.getCtrlFileName();
            message.what =2;
            handler.sendMessage(message);
        }

    }

    @Override
    protected void ftpVideoFail(PreviewReceiveModel model) {
        super.ftpPreviewFail(model);
        tipErrorShow("设备端发送录像文件失败");
        Log.e(TAG, "设备端发送录像文件失败");
    }

    /**
     *  配置获取LayoutParams
     * @param mark  0-left 1-right
     * @param event
     */
    public void getLayoutParams(MotionEvent event,int mark){
        DownX = event.getRawX();
        relative1.setBackgroundResource(R.drawable.img_video_new_progress_bg);
        setToolbar(true);
        layoutParams_progress = (RelativeLayout.LayoutParams) relative1.getLayoutParams();
        width_progress = layoutParams_progress.width;
        if(mark == 0){ //Left
            Margin_progress = layoutParams_progress.leftMargin;
            layoutParams_yin = (RelativeLayout.LayoutParams) img_left.getLayoutParams();
        }else {
            Margin_progress = layoutParams_progress.rightMargin;
            layoutParams_yin = (RelativeLayout.LayoutParams) img_right.getLayoutParams();
        }
        width1_progress = layoutParams_yin.width;


    }
    /**
     * 裁剪向右边拉
     * @param MoveX
     */
    private void LeftMoveLayout(float MoveX) {
        if (layoutParams_progress != null && layoutParams_yin != null) {
            if (Margin_progress + (int) MoveX > 0 && width_progress - (int) MoveX > last_length) {
                layoutParams_progress.width = width_progress - (int) MoveX;
                layoutParams_progress.leftMargin = Margin_progress + (int) MoveX;
                layoutParams_yin.width = width1_progress + (int) MoveX;

                relative1.setLayoutParams(layoutParams_progress);
                img_left.setLayoutParams(layoutParams_yin);

            }
        }
    }

    /**
     * 裁剪向左边拉
     * @param MoveX
     */
    private void RightMoveLayout(float MoveX) {
        if (layoutParams_progress != null && layoutParams_yin != null) {
            if (Margin_progress + (int) MoveX > right_margin && width_progress - (int) MoveX > last_length) {
                layoutParams_progress.width = width_progress - (int) MoveX;
                layoutParams_progress.rightMargin = Margin_progress + (int) MoveX;
                layoutParams_yin.width = width1_progress + (int) MoveX;

                relative1.setLayoutParams(layoutParams_progress);
                img_right.setLayoutParams(layoutParams_yin);
            }
        }
    }



}
