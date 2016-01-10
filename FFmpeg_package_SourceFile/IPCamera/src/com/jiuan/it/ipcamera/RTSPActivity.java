package com.jiuan.it.ipcamera;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.jiuan.it.ipc.utils.MyRender;
import com.jiuan.it.ipc.utils.RtspAudio;
import com.jiuan.it.ipc.utils.RtspFromFFMPEG;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author chao
 * @date 2015-8-12下午3:59:12
 * @Title: RTSPActivity.java
 * @Package com.jiuan.it.ipcamera
 */
public class RTSPActivity extends Activity {
	private static String TAG = "RTSPActivity";
	private String URL;

	private TextView titleTextView;
	private LinearLayout mProcessView_Layout;
	private Button mAudioButton;

	private RtspFromFFMPEG mRtspFromFFMPEG;
	private RtspAudio mRtspAudio;

	// 视频显示
	private GLSurfaceView mProcessView;
	static MyRender mRender;
	private static AudioTrack mAudioTrack;
	
	private Boolean isPlay = false;
	private Boolean isAudioPlay = false;
	private Lock videolock = new ReentrantLock();
	private Lock audioLock= new ReentrantLock();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 保持屏幕不锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.player);
		URL = this.getIntent().getExtras().getString("UID");
		intview();
		int minBuffSize = AudioTrack.getMinBufferSize(8000,
				AudioFormat.CHANNEL_OUT_STEREO,// 单声道
				AudioFormat.ENCODING_PCM_16BIT);// 一个采样点16比特-2个字节
		Log.d(TAG, "minBuffSize大小" + minBuffSize);
		if (minBuffSize > 0) {
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					8000,
					AudioFormat.CHANNEL_OUT_STEREO,// 单声道
					AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
					AudioTrack.MODE_STREAM);
		}
		mAudioTrack.play();
		mRtspFromFFMPEG = new RtspFromFFMPEG();
		mRtspAudio = new RtspAudio();
		if(mRtspFromFFMPEG.init(URL)!=-1&&mRtspAudio.init(URL)!=-1){
			isPlay = true;
			final byte[] data =new byte[1280*720*3 ];
			final int[] width = new int[1];
			final int[] height = new int[1];
			//解码渲染线程
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(isPlay){
						videolock.lock();
						 int ret = mRtspFromFFMPEG.decode(data);
						videolock.unlock();
					}
					
				}
			}).start();
			//渲染线程
			//解码渲染线程
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while(isPlay){
						if (null != data&&width[0]>0&&height[0]>0) {
							mRender.update(data, width[0], height[0]);
							Log.d(TAG, "成功渲染一帧");
						} 
					}
					
				}
			}).start();
			//音频播放
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					while(isPlay){
						audioLock.lock();
						byte data[] = new byte[1280*720*3];
						int len = 0;
						int ret = 0;
						//int ret = mRtspAudio.decode();
						if(ret > 0&&isAudioPlay){
							int re = mAudioTrack.write(data,0, ret);
							if(re>0){
								Log.d(TAG, "write一次音频数据");
							}else{
								Log.e(TAG, "write音频数据失败，返回值为："+re);
							}
						}
						audioLock.unlock();
					}
					
				}
			}).start();
		}
		
	}

	void intview() {
		titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText(URL);
		mProcessView_Layout = (LinearLayout) findViewById(R.id.playLinearLayout);
		mProcessView = new GLSurfaceView(this);
		mRender = new MyRender(this);
		mProcessView_Layout.addView(mProcessView);
		mProcessView.setRenderer(mRender);
		mAudioButton = (Button)findViewById(R.id.AudioButton);
		mAudioButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!isAudioPlay) {
					isAudioPlay = true;
					mAudioButton.setText("关闭");
					}else{
						isAudioPlay = false;
						mAudioButton.setText("音频");
					}
			}
		});
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		if(isPlay){
			isPlay = false;
			videolock.lock();
			mRtspFromFFMPEG.close();
			videolock.unlock();
			audioLock.lock();
			mRtspAudio.close();
			audioLock.unlock();
			mAudioTrack.pause();
			mAudioTrack.flush();
		}
		
		finish();
	}
}
