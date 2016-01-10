package com.jiuan.it.ipc.utils;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


import android.app.Application;

public class MyApplication extends Application {
	
	//TUTK goolink 账号
	public static final String TUTK_ACCOUNT = "tutk_name";
	public static final String Goolink_ACCOUNT = "goolink_name";
	public static final String RTSP_ACCOUNT = "rtsp_name";
	
	public static final String ACCOUNT_NUM_KEY = "account_num";
    public static final String ACCOUNT_NAME_KEY = "account_name";
    
	
	//音视频缓存
     public static List<VideoData> videoDataToPlay = null;//视频播放缓冲区
     public static final int bufferSize = 2;//视频播放缓冲区大小
	 public static List<AudioData> audioDataList = null;//音频缓冲区
	
	@Override
	public void onCreate() {
		super.onCreate();
		//保存打印信息到本地
	 LogcatHelper.getInstance(this).start();
	 videoDataToPlay = Collections.synchronizedList(new LinkedList<VideoData>());
	 audioDataList = Collections.synchronizedList(new LinkedList<AudioData>()); 
	}

}
