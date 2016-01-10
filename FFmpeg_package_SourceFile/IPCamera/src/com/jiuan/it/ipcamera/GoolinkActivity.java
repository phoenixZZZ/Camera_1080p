package com.jiuan.it.ipcamera;

import glnk.client.DataSourceListener2;
import glnk.client.GlnkChannel;
import glnk.io.GlnkCode.PTZCMD;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import com.jiuan.it.ipc.utils.AudioDecode;
import com.jiuan.it.ipc.utils.H264DecodeUtil;
import com.jiuan.it.ipc.utils.MyApplication;
import com.jiuan.it.ipc.utils.MyRender;
import com.jiuan.it.ipc.utils.RecordVideo;
import com.jiuan.it.ipc.utils.Store;
import com.jiuan.it.ipc.utils.TUTKClient;
import com.jiuan.it.ipc.utils.VideoData;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class GoolinkActivity extends Activity {

	private static String TAG = "GoolinkActivity";
	private String UID;
	private String UserName;
	private String Password;

	private TextView titleTextView;
	private Button mAudioButton;
	private Button LowButton;
	private Button MidButton;
	private Button HighButton;
	private Button LEDButton;
	private Button AlarmButton;
	private Button RecordeControlButton;
	private ImageButton CameraButton;
	private ImageButton RecordButton;
	private ImageButton UPButton;
	private ImageButton DownButton;
	private ImageButton LeftButton;
	private ImageButton RightButton;
	private ImageButton SpeakButton;
	

	
	public static byte[] GLNK_VIDEO_QUALITY_HIGH = intToByteArray(0xA200);
	public static byte[] GLNK_VIDEO_QUALITY_MIDDLE = intToByteArray(0xA201);
	public static byte[] GLNK_VIDEO_QUALITY_LOW = intToByteArray(0xA202);
	public static byte[] GLNK_LED_ON = intToByteArray(0xA300);
	public static byte[] GLNK_LED_OFF = intToByteArray(0xA301);
	public static byte[] GLNK_ALARM_ON = intToByteArray(0xA400);
	public static byte[] GLNK_ALARM_OFF = intToByteArray(0xA401);
	public static byte[] GLNK_RECORDR_START = intToByteArray(0xA500);
	public static byte[] GLNK_RECORD_STOP = intToByteArray(0xA501);
	
	private Boolean ledOn = false;
	private Boolean alarmOn = false;
	private Boolean recordControlStart = false;
	private Boolean talkingStart = false;
	private static boolean isRecordVideo = false;


	private int frameTime = 0;
	private String s = null;
	private Boolean isAudioPlay = false;

	private GlnkChannel liveChannel = null;
	private MyLiveDataSource liveSource = null;

	// 视频显示
	private GLSurfaceView mProcessView;
	private LinearLayout mProcessView_Layout;
	static MyRender mRender;
	static H264DecodeUtil mFFmpegUtils = new H264DecodeUtil();
	private AudioDecode mAacDecodeUtil = new AudioDecode();
	private RecordVideo mRecordVideo = new RecordVideo();
	private AudioTrack mAudioTrack;


	public static int[] width = new int[1];
	public static int[] height = new int[1];

	private boolean isPlay = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 保持屏幕不锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		
		setContentView(R.layout.player);
		UID = this.getIntent().getExtras().getString("UID");
		intview();

		UserName = this.getIntent().getExtras().getString("UserName");
		Password = this.getIntent().getExtras().getString("Password");

		mProcessView = new GLSurfaceView(this);
		mRender = new MyRender(this);
		mProcessView_Layout.addView(mProcessView);
		mProcessView.setRenderer(mRender);

		int minBuffSize = AudioTrack.getMinBufferSize(8000,
				AudioFormat.CHANNEL_OUT_STEREO,// 双声道
				AudioFormat.ENCODING_PCM_16BIT);// 一个采样点16比特-2个字节
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
				AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				minBuffSize, AudioTrack.MODE_STREAM);
		mAudioTrack.setVolume(100f);
		mAudioTrack.play();
		// 清空缓存区数据
		MyApplication.videoDataToPlay.clear();
		// 连接开始
		if (play(UID, UserName, Password)) {
			Log.d(TAG, "play");
			mFFmpegUtils.registeriInit();
			mAacDecodeUtil.init();
			mAacDecodeUtil.findG711aDecoder();
			// 渲染线程
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					int readNum = 0;
					while (liveChannel != null) {
						if (isPlay && MyApplication.videoDataToPlay != null
								&& MyApplication.videoDataToPlay.size() > 0) {
							// 当读取数据时，确保缓冲区内的数据不低于所设定值
							if ((readNum++) == MyApplication.bufferSize) {
								if (MyApplication.videoDataToPlay.size() < MyApplication.bufferSize) {
									Log.e(TAG, "播放缓冲区内数据不足！！！");
									TUTKClient.isPlayVideo = false;
									continue;
								}
							}
							VideoData mVideoData = MyApplication.videoDataToPlay
									.remove(0);
							if (mVideoData.getVideoData() != null) {
								Log.d(TAG, "当前播放缓冲区的长度为："
										+ MyApplication.videoDataToPlay.size());
								mRender.update(mVideoData.getVideoData(),
										mVideoData.getWidth(),
										mVideoData.getHeight());
								Log.d(TAG,
										"Render完成第"
												+ mVideoData.getFrameNumber()
												+ "帧视频");
							}
						}
					}
				}
			}).start();
		}
		mAudioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isAudioPlay) {
					isAudioPlay = true;
					mAudioTrack.play();
					mAudioButton.setText("关闭");
//					new Thread(new Runnable() {
//
//						@Override
//						public void run() {
//							int i = 0;
//							// TODO Auto-generated method stub
//							while (isAudioPlay) {
//								if (audioData != null&&audioData.length>0) {
//									Log.d(TAG, "write次数" + (++i));
//									mAudioTrack.write(audioData, 0,
//											dataLength);
//								}
//
//							}
//						}
//					}).start();

				} else {
					isAudioPlay = false;
					mAudioButton.setText("音频");
					mAudioTrack.pause();
					mAudioTrack.flush();
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		if (liveChannel != null) {
			Log.d(TAG, "执行liveChannel.stop()");
			liveChannel.stop();
			liveChannel.release();
			liveChannel = null;
			mFFmpegUtils.close();
			mAacDecodeUtil.close();
		}
	}

	// 连接开始
	private Boolean play(String gid, String user, String passwd) {

		System.out.println("connect: " + gid);
		Log.d(TAG, "connect:" + gid);
		liveSource = new MyLiveDataSource();
		liveChannel = new GlnkChannel(liveSource);

		/**
		 * setMetaData的后三个参数:
		 * channelNO - 通道号，从0到31. 
		 * streamType - 码流类型, 0:主, 1:次 码流 3: 通道 
		 * dataType - 流数据类型, 0:视频流, 1:音频流, 2:音视频流
		 */

		liveChannel.setMetaData(gid, user, passwd, 0, 1, 2);
		int ret = liveChannel.start();
		if (ret < 0) {
			Log.e(TAG, "liveChannel.start失败" + ret);
			return false;
		}
		return true;
	}

	class MyLiveDataSource extends DataSourceListener2 {
		
		// 开始连接
		@Override
		public void onConnecting() {
			Log.d(TAG, "onConnecting");
		}

		// 连接处理函数
       //mode 当前设备的连接模式: =1: p2p, 2: relay, 3: 分发
		@Override
		public void onConnected(int mode, String ip, int port) {
			Log.d(TAG, "onConnected线程" + Thread.currentThread().getName());
			String s = "\nonConnected:\n" + " mode: " + mode + ", ip: " + ip
					+ ", port: " + port;
			Log.d(TAG, "onConnected线程" + s);
		}

		// 连接模式改变
		@Override
		public void onModeChanged(int mode, String ip, int port) {
			Log.d(TAG, "\nonModeChanged:\n" + " mode: " + mode + ",\n" + "ip: "
					+ ip);
		}

		// 连接断开
		@Override
		public void onDisconnected(int errcode) {
			Log.d(TAG, "onDisconnected线程" + Thread.currentThread().getName());
		}

		// 重连开始
		@Override
		public void onReConnecting() {
			Log.d(TAG, "onReConnecting");
		}

		// 登录响应 1成功 2用户名或密码错误
		@Override
		public void onAuthorized(int result) {
			if (result == 2) {
				Log.e(TAG, "onAuthorized" + "用户名或密码错误" + result);
			} else if (result == 1) {
				Log.d(TAG, "onAuthorized" + "登录成功 " + result);
			}

		}

		// 每秒的数据流量
		@Override
		public void onDataRate(int bytesPersecond) {
			Log.d(TAG, "onDataRate线程" + Thread.currentThread().getName());
			Log.d(TAG, "onDataRate线程" + "\nBitRate: " + bytesPersecond * 8
					/ 1024 + "kbps");
			System.out.println("onDataRate: " + bytesPersecond);
		}

		// 音视频模式信息
		@Override
		public void onAVStreamFormat(byte[] data) {
			// 音视频数据只出了一次
			Log.d(TAG, "GlnkLiveActivityAVStreamFormat" + "音视频模式信息"
					+ data.length);
		}

		@Override
		public void onVideoData(byte[] data, int frameIndex, int timestamp,
				boolean isIFrame) {
			Log.d(TAG, "onVideoData线程" + Thread.currentThread().getName());
			s = "\nFrameRate: " + 1000.00 / (timestamp - frameTime) + "fps";
			frameTime = timestamp;
			Log.d(TAG, "帧率" + s);

			Log.d(TAG, "对接收到的数据进行分析" + "data长度为：" + data.length
					+ " frameIndex：" + frameIndex + " timestamp：" + timestamp
					+ " isIFrame：" + isIFrame);
			VideoData mVideoData = new VideoData();
			mVideoData.setVideoData(mFFmpegUtils.decode(data, data.length,
					width, height));
			mVideoData.setWidth(width[0]);
			mVideoData.setHeight(height[0]);
			mVideoData.setFrameNumber(frameIndex);
			if (mVideoData.getVideoData() != null) {
				MyApplication.videoDataToPlay.add(mVideoData);
				while (MyApplication.videoDataToPlay.size() >= MyApplication.bufferSize) {
					isPlay = true;
				}
			} else {
				Log.e(TAG, "解码返回值为空");
			}

//			if (isRecordVideo) {
//				// mRecordVideo.record(data, data.length);
//				// 保存.MP4数据
//				if (Environment.getExternalStorageState().equals(
//						Environment.MEDIA_MOUNTED)) {
//
//					Store mStore = new Store();
//					mStore.store("Goolink.Mp4", data);
//					Log.d(TAG, "接收到的视频是：" + Arrays.toString(data));
//				}
//			}
			if (isRecordVideo) {
				mRecordVideo.record(data, data.length);
			}
		}

		@Override
		public void onAudioData(byte[] data, int timestamp) {
			// TODO 写文件
			Log.d(TAG, "onAudioData" + "音频数据" + data.length);
			byte[] outdata = new byte[4096]; 
			int re = mAacDecodeUtil.g711aDecode(data, data.length,outdata);
			 if(re!=-1&&isAudioPlay){
				 mAudioTrack.write(outdata, 0,re);
			 }
			// 保存.aac数据
//			if (Environment.getExternalStorageState().equals(
//					Environment.MEDIA_MOUNTED)) {
//				Store mStore = new Store();
//				mStore.store("Goolink.aac", data);
//			}

		}

		// 透明通道数据
		@Override
		public void onIOCtrlByManu(byte[] data) {
			Log.d(TAG, "onIOCtrlByManu" + "透明通道数据" + data.length);
		}

		@Override
		public void onTalkingResp(int result, int audiofmt, int audioChannels,
				int audioSampleRate, int audioBitsPerSample) {
			// TODO Auto-generated method stub
			super.onTalkingResp(result, audiofmt, audioChannels, audioSampleRate,
					audioBitsPerSample);
		}
		
	}

	void intview() {
		titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText("Goolink：" + UID);
		mAudioButton = (Button) findViewById(R.id.AudioButton);
		mProcessView_Layout = (LinearLayout) findViewById(R.id.playLinearLayout);

		CameraButton = (ImageButton) findViewById(R.id.CameraButton);
		RecordButton = (ImageButton) findViewById(R.id.RecordButton);
		LowButton = (Button) findViewById(R.id.LowButton);
		MidButton = (Button) findViewById(R.id.CentreButton);
		HighButton = (Button) findViewById(R.id.HighButton);
		LEDButton = (Button) findViewById(R.id.LightControlButton);
		AlarmButton = (Button) findViewById(R.id.AlarmButton);
		RecordeControlButton = (Button) findViewById(R.id.RecordeControlButton);
		UPButton = (ImageButton) findViewById(R.id.UpButton);
		DownButton = (ImageButton) findViewById(R.id.DownButton);
		LeftButton = (ImageButton) findViewById(R.id.LeftButton);
		RightButton = (ImageButton) findViewById(R.id.RightButton);
		SpeakButton = (ImageButton) findViewById(R.id.SpeakButton);
		
		CameraButton.setOnClickListener(new MyClikListener());
		RecordButton.setOnClickListener(new MyClikListener());
		LowButton.setOnClickListener(new MyClikListener());
		MidButton.setOnClickListener(new MyClikListener());
		HighButton.setOnClickListener(new MyClikListener());
		LEDButton.setOnClickListener(new MyClikListener());
		AlarmButton.setOnClickListener(new MyClikListener());
		RecordeControlButton.setOnClickListener(new MyClikListener());
		UPButton.setOnClickListener(new MyClikListener());
		DownButton.setOnClickListener(new MyClikListener());
		LeftButton.setOnClickListener(new MyClikListener());
		RightButton.setOnClickListener(new MyClikListener());
		SpeakButton.setOnClickListener(new MyClikListener());
	}

	class MyClikListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch (v.getId()) {
			//拍照
			case R.id.CameraButton:
				if (mFFmpegUtils.saveJPG("mnt/sdcard/IPC/"
						+ UUID.randomUUID().toString() + ".jpg") == 1) {
					Toast.makeText(getApplicationContext(), "成功保存一张图片",
							Toast.LENGTH_SHORT).show();
				}
				break;
            //录像			
			case R.id.RecordButton:
				if (!isRecordVideo) {
					isRecordVideo = true;
					RecordButton.setBackground(getResources().getDrawable(
							R.drawable.live_record_1));
					// 开始录制视频
					TUTKClient.mRecordVideo.start("mnt/sdcard/IPC/"
							+ UUID.randomUUID().toString() + ".mp4");
					Toast.makeText(getApplicationContext(), "开始录制视频",
							Toast.LENGTH_SHORT).show();

					

				} else {
					isRecordVideo = false;
					RecordButton.setBackground(getResources().getDrawable(
							R.drawable.live_record_0));
					// 结束录制视频
					TUTKClient.mRecordVideo.stop();
				}
				break;
		    //视频质量
			case R.id.HighButton:
		
				if (liveChannel != null) {
					int rsh = liveChannel
							.sendManuData(GLNK_VIDEO_QUALITY_HIGH);
					if (rsh < 0) {
						Log.e(TAG, "设置高视频质量失败");
						Toast.makeText(getApplicationContext(), "设置高视频质量失败",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "成功设置高视频质量",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), "透明通道未打开",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.CentreButton:
				
				if (liveChannel != null) {
					int rsc = liveChannel
							.sendManuData(GLNK_VIDEO_QUALITY_MIDDLE);
					if (rsc < 0) {
						Log.e(TAG, "设置中视频质量失败");
						Toast.makeText(getApplicationContext(), "设置中视频质量失败",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "成功设置中视频质量",
								Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(getApplicationContext(), "透明通道未打开",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.LowButton:
				if (liveChannel != null) {
					int rsl = liveChannel.sendManuData(GLNK_VIDEO_QUALITY_LOW);
					if (rsl < 0) {
						Log.e(TAG, "设置低视频质量失败");
						Toast.makeText(getApplicationContext(), "设置低视频质量失败",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "成功设置低视频质量",
								Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(getApplicationContext(), "透明通道未打开",
							Toast.LENGTH_SHORT).show();
				}
				break;
			//灯、警报、设备录像控制
			case R.id.LightControlButton:
				if(ledOn){
					if(liveChannel.sendManuData(GLNK_LED_OFF)<0){
						LEDButton.setText("关灯");
						Toast.makeText(getApplicationContext(), "关灯失败",
								Toast.LENGTH_SHORT).show();
					}else{
						LEDButton.setText("开灯");
						Toast.makeText(getApplicationContext(), "关灯成功",
								Toast.LENGTH_SHORT).show();
						ledOn =false;
					}
					
				}else {
					if(liveChannel.sendManuData(GLNK_LED_ON)<0){
						LEDButton.setText("开灯");
						Toast.makeText(getApplicationContext(), "开灯失败",
								Toast.LENGTH_SHORT).show();
					}else{
						LEDButton.setText("关灯");
						Toast.makeText(getApplicationContext(), "开灯成功",
								Toast.LENGTH_SHORT).show();
						ledOn =true;
					}
					
				}
				break;
			case R.id.AlarmButton:
				if(alarmOn){
					if(liveChannel.sendManuData(GLNK_ALARM_OFF)<0){
						AlarmButton.setText("警关");
						Toast.makeText(getApplicationContext(), "关闭警报失败",
								Toast.LENGTH_SHORT).show();
					}else{
						AlarmButton.setText("警开");
						Toast.makeText(getApplicationContext(), "关闭警成功",
								Toast.LENGTH_SHORT).show();
						alarmOn =false;
					}
					
					
				}else {
					if(liveChannel.sendManuData(GLNK_ALARM_ON)<0){
						AlarmButton.setText("警开");
						Toast.makeText(getApplicationContext(), "打开警报失败",
								Toast.LENGTH_SHORT).show();
					}else{
						AlarmButton.setText("警关");
						Toast.makeText(getApplicationContext(), "打开警报成功",
								Toast.LENGTH_SHORT).show();
						alarmOn =true;
					}
				}
				break;
			case R.id.RecordeControlButton:
				if(recordControlStart){
					if(liveChannel.sendManuData(GLNK_RECORD_STOP)<0){
						RecordeControlButton.setText("关闭");
						Toast.makeText(getApplicationContext(), "关闭录像控制失败",
								Toast.LENGTH_SHORT).show();
					}else{
						RecordeControlButton.setText("录像");
						Toast.makeText(getApplicationContext(), "关闭录像控制成功",
								Toast.LENGTH_SHORT).show();
						recordControlStart =false;
					}
				}else {
					if(liveChannel.sendManuData(GLNK_RECORDR_START)<0){
						RecordeControlButton.setText("录像");
						Toast.makeText(getApplicationContext(), "打开录像控制失败",
								Toast.LENGTH_SHORT).show();
					}else{
						RecordeControlButton.setText("关闭");
						Toast.makeText(getApplicationContext(), "打开录像控制成功",
								Toast.LENGTH_SHORT).show();
						recordControlStart =true;
					}
				}
				break;
			//云台控制
			case R.id.UpButton:
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_UP, 4);
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_STOP, 4);
				break;
			case R.id.DownButton:
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_DOWN, 4);
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_STOP, 4);
				break;
			case R.id.LeftButton:
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_LEFT, 4);
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_STOP, 4);
				break;
			case R.id.RightButton:
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_RIGHT, 4);
				liveChannel.sendPTZCmd(PTZCMD.PTZ_MV_STOP, 4);
				break;
			case R.id.SpeakButton:
				if(!talkingStart){
					if(liveChannel.startTalking()<0){
						Toast.makeText(getApplicationContext(), "对讲打开失败",
								Toast.LENGTH_SHORT).show();
					}else {
						Toast.makeText(getApplicationContext(), "对讲打开成功",
								Toast.LENGTH_SHORT).show();
						talkingStart = true;
					}
				}else {
					if(liveChannel.stopTalking()<0){
						Toast.makeText(getApplicationContext(), "停止对讲失败",
								Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getApplicationContext(), "停止对讲成功",
								Toast.LENGTH_SHORT).show();						
						talkingStart = false;
					}
					
				}
				break;

			}
		}

	}

	
	
	private static byte[] intToByteArray(int integer) {
		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
		DataOutputStream doutput = new DataOutputStream(boutput);
		try {
			doutput.writeInt(integer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] buf = boutput.toByteArray();

		return (buf);
	}
}
