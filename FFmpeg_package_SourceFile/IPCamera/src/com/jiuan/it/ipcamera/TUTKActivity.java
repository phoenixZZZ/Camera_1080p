package com.jiuan.it.ipcamera;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.jiuan.it.ipc.utils.AudioData;
import com.jiuan.it.ipc.utils.AudioDecode;
import com.jiuan.it.ipc.utils.RecordVideo;
import com.jiuan.it.ipc.utils.Store;
import com.jiuan.it.ipc.utils.TUTKClient;
import com.jiuan.it.ipc.utils.H264DecodeUtil;
import com.jiuan.it.ipc.utils.MyApplication;
import com.jiuan.it.ipc.utils.MyRender;
import com.jiuan.it.ipc.utils.TUTKClient.AudioThread.ErrorListener;
import com.jiuan.it.ipc.utils.TUTKClient.VideoThread.NetworkListener;
import com.jiuan.it.ipc.utils.VideoData;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.IOTCAPIs;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("NewApi")
public class TUTKActivity extends Activity implements NetworkListener,
		ErrorListener {
	// TODO 将视频接收的线程放在该Activty中试试
	private static String TAG = "TUTKActivity";
	private String UID;
	private static int audioFormat;

	private byte[] frameinfo;
	private static int stopAudio = -1;
	private static int speak = -1;
	private static int avCID = -1;
	private static boolean isRecord = false;
	private static File file;

	private TextView titleTextView;
	private Button mAudioButton;
	private Button ledButton;
	private Button alarmButton;
	private Button controlRecordeButton;
	private Button LowButton;
	private Button MidButton;
	private Button HighButton;
	private ImageButton UpButton;
	private ImageButton DownButton;
	private ImageButton LeftButton;
	private ImageButton RightButton;
	private ImageButton SpeakButton;
	private ImageButton CameraButton;
	private ImageButton MP4RecordButton;

	GLSurfaceView mProcessView;
	LinearLayout mProcessView_Layout;
	private static MyRender mRender;
	private static AudioTrack mAudioTrack;
	private Boolean AudioConnect = false;
	private static Boolean ledON = false;
	private static Boolean alarmON = false;
	private static Boolean recordeON = false;


	public static Boolean isDelete = false;// 跳过P帧的标志位
	public static boolean isRecordVideo = false;// 视频录制
	public static RecordVideo mRecordVideo;// 视频录制工具
	public static Boolean isPlayVideo = false;// 是否可以开始播放视频

	// final Thread videoThread = new Thread(new VideoThread(),
	// "Video Thread");
	final Thread videoThread = new Thread(new TUTKClient.VideoThread(this),
			"Video Thread");
	final Thread renderThread = new Thread(new RenderThread(), "Render Thread");

	// 解码音视频
	static H264DecodeUtil mH264DecodeUtil = new H264DecodeUtil();
	// static G711Decode mAudioDecode = new G711Decode();
	// static IPCRecorde mIPCRecorde = new IPCRecorde();
	static AudioDecode mAudioDecode = new AudioDecode();
	
	public static int[] width = new int[1];
	public static int[] height = new int[1];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// 保持屏幕不锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.player);
		intview();
		UID = this.getIntent().getExtras().getString("UID");
		titleTextView.setText("TUTK:" + UID);
		audioFormat = this.getIntent().getExtras().getInt("audioFormat");


		mProcessView = new GLSurfaceView(this);
		mRender = new MyRender(this);
		mProcessView_Layout.addView(mProcessView);
		mProcessView.setRenderer(mRender);
		int minBuffSize = AudioTrack.getMinBufferSize(8000,
				AudioFormat.CHANNEL_OUT_STEREO,// 单声道
				AudioFormat.ENCODING_PCM_16BIT);// 一个采样点16比特-2个字节
		Log.e(TAG, "帧大小" + minBuffSize);
		if (minBuffSize > 0) {
			mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
					8000,
					AudioFormat.CHANNEL_OUT_STEREO,// 单声道
					AudioFormat.ENCODING_PCM_16BIT, minBuffSize,
					AudioTrack.MODE_STREAM);
		}

		mH264DecodeUtil.registeriInit();
		//清空缓存区数据
		MyApplication.videoDataToPlay.clear();
		
		videoThread.start();
		renderThread.start();

		mAudioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!AudioConnect) {
					// 请求发送音频
					int ret = AVAPIs
							.avSendIOCtrl(TUTKClient.AV_CID,
									AVAPIs.IOTYPE_USER_IPCAM_AUDIOSTART,
									new byte[8], 8);
					if (ret < 0) {
						Log.e(TAG, "请求音频数据失败");
						Toast.makeText(TUTKActivity.this, "请求音频数据失败",
								Toast.LENGTH_SHORT).show();
					} else {
						mAudioButton.setText("关闭");
						AudioConnect = true;
						mAudioTrack.play();
						mAudioDecode.init();
						Log.d(TAG, "音频格式" + audioFormat);
						if (audioFormat == 1) {
							mAudioDecode.findAACDecoder();
						} else if (audioFormat == 0) {
							mAudioDecode.findG711aDecoder();
						}
						while (true) {
							// AVAPIs av = new AVAPIs();
							int r = AVAPIs.avCheckAudioBuf(TUTKClient.AV_CID);
							Log.d(TAG, "音频接收缓冲区帧数qqq" + ret);
							if (r < 0) {
								Log.e(TAG, "检查音频接收缓冲区帧数时出错" + ret);
								break;
							} else if (r < 25) {
								try {
									Thread.sleep(120);
									continue;
								} catch (InterruptedException e) {
									System.out.println(e.getMessage());
									break;
								}
							} else {
								break;
							}
						}
						// 音频接收与播放
						new Thread(new Runnable() {
							int AUDIO_BUF_SIZE = 2048;
							int FRAME_INFO_SIZE = 32;
							byte[] frameInfo = new byte[FRAME_INFO_SIZE];
							byte[] audioBuffer = new byte[AUDIO_BUF_SIZE];
							byte[] data = new byte[AUDIO_BUF_SIZE];
							byte[] times = new byte[4];
							int i = 0;

							@Override
							public void run() {
								// TODO Auto-generated method stub
								while (AudioConnect) {
									int ret = AVAPIs
											.avCheckAudioBuf(TUTKClient.AV_CID);
									Log.d(TAG, "音频接收缓冲区帧数" + ret);
									if (ret < 0) {
										Log.e(TAG, "检查音频接收缓冲区帧数时出错" + ret);
										break;
									} else if (ret < 3) {
										try {
											Thread.sleep(3000);
											continue;
										} catch (InterruptedException e) {
											System.out.println(e.getMessage());
											break;
										}
									}

									int[] frameNumber = new int[1];
									ret = AVAPIs.avRecvAudioData(
											TUTKClient.AV_CID, audioBuffer,
											AUDIO_BUF_SIZE, frameInfo,
											FRAME_INFO_SIZE, frameNumber);
									Log.d(TAG, "音频接收数据长度" + ret);
									if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
										Log.e(TAG, "远程会话断开" + ret);
										Toast.makeText(TUTKActivity.this,
												"远程会话断开，请重新连接！",
												Toast.LENGTH_SHORT).show();
										break;
									} else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
										Log.e(TAG, "远程连接超时" + ret);
										Toast.makeText(TUTKActivity.this,
												"远程连接超时，请重新连接！",
												Toast.LENGTH_SHORT).show();
										break;
									} else if (ret == AVAPIs.AV_ER_INVALID_SID) {
										Log.e(TAG, "会话不可用" + ret);
										Toast.makeText(TUTKActivity.this,
												"会话不可用，请重新连接！",
												Toast.LENGTH_SHORT).show();
										break;
									} else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
										Log.e(TAG, "丢失一帧数据" + ret);
										continue;
									} else if (ret == AVAPIs.AV_ER_DATA_NOREADY) {
										Log.e(TAG, "数据还没有准备好" + ret);
										try {
											Thread.sleep(30);
											continue;
										} catch (InterruptedException e) {
											System.out.println(e.getMessage());
											Toast.makeText(TUTKActivity.this,
													"连接异常，请重新连接！",
													Toast.LENGTH_SHORT).show();
											break;
										}
									}
									//TODO 目前摄像头有音频头，以后会去掉的，想着把下面的代码去掉
//									System.arraycopy(audioBuffer, 24, data, 0, ret);
									byte[] outdata = new byte[4096]; 
//									Store mStore = new Store();
									switch (audioFormat) {
									case 0:
//										mStore.store("tutk.wmv", audioBuffer);
										int re = mAudioDecode.g711aDecode(audioBuffer, ret,outdata);
										
										if(re!=-1){
											mAudioTrack.write(outdata,0, re);
										}
										
										break;
									case 1:
//										mStore.store("tutk.aac", audioBuffer);
										int r = mAudioDecode.aacDecode(audioBuffer, ret,outdata);

										if(r!=-1){
											mAudioTrack.write(outdata,0, r);
										}
										
										break;
									case 2:
//										mStore.store("tutk.pcm", audioBuffer);
										mAudioTrack.write(audioBuffer, 0, ret);
										break;
									default:
										break;
									}
									i++;
									Log.d(TAG, "i" + i + "音频接收第"
											+ frameNumber[0] + "帧音频的时间戳"
											+ Bytes2HexString(times) + "编码格式"
											+ Integer.toHexString(frameInfo[0]));
									Log.d(TAG, "音频帧大小" + ret);
								}
							}
						}).start();
					}
				} else {
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVAPIs.IOTYPE_USER_IPCAM_AUDIOSTOP, new byte[8], 8);
					if (ret >= 0) {
						AudioConnect = false;
						mAudioButton.setText("音频");
						mAudioTrack.pause();
						mAudioTrack.flush();
					}
				}
			}
		});

	}

	@SuppressLint("ClickableViewAccessibility")
	void intview() {
		titleTextView = (TextView) findViewById(R.id.title);
		mAudioButton = (Button) findViewById(R.id.AudioButton);
		mProcessView_Layout = (LinearLayout) findViewById(R.id.playLinearLayout);
		ledButton = (Button) findViewById(R.id.LightControlButton);
		alarmButton = (Button) findViewById(R.id.AlarmButton);
		controlRecordeButton = (Button) findViewById(R.id.RecordeControlButton);
		LowButton = (Button) findViewById(R.id.LowButton);
		MidButton = (Button) findViewById(R.id.CentreButton);
		HighButton = (Button) findViewById(R.id.HighButton);
		UpButton = (ImageButton) findViewById(R.id.UpButton);
		DownButton = (ImageButton) findViewById(R.id.DownButton);
		LeftButton = (ImageButton) findViewById(R.id.LeftButton);
		RightButton = (ImageButton) findViewById(R.id.RightButton);
		SpeakButton = (ImageButton) findViewById(R.id.SpeakButton);
		CameraButton = (ImageButton) findViewById(R.id.CameraButton);
		MP4RecordButton = (ImageButton) findViewById(R.id.RecordButton);

		ledButton.setOnClickListener(new MyClikListener());
		alarmButton.setOnClickListener(new MyClikListener());
		LowButton.setOnClickListener(new MyClikListener());
		MidButton.setOnClickListener(new MyClikListener());
		HighButton.setOnClickListener(new MyClikListener());
		UpButton.setOnClickListener(new MyClikListener());
		DownButton.setOnClickListener(new MyClikListener());
		LeftButton.setOnClickListener(new MyClikListener());
		RightButton.setOnClickListener(new MyClikListener());
		controlRecordeButton.setOnClickListener(new MyClikListener());
		SpeakButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//

				byte flags = (AVFrame.AUDIO_SAMPLE_8K << 2)
						| (AVFrame.AUDIO_DATABITS_16 << 1)
						| AVFrame.AUDIO_CHANNEL_MONO;
				long timestamp = System.currentTimeMillis();
				frameinfo = AVIOCTRLDEFs.SFrameInfo.parseContent(
						(short) AVFrame.MEDIA_CODEC_AUDIO_PCM, flags, (byte) 0,
						(byte) 1, (int) timestamp);
				//
				try {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						Log.d("TAG1", "ACTION_DOWN");
						isRecord = true;
						if (AudioConnect) {
							stopAudio = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
									AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTOP,
									new byte[8], 8);
							Log.d("TAG1", "stopAudio" + stopAudio);
						}
						// TODO 是否有相应的关闭方法
						int ioCid = IOTCAPIs
								.IOTC_Session_Get_Free_Channel(TUTKClient.IOTC_SID);

						Log.d("TAG1", "ioCid" + ioCid);

						speak = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
								AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTART,
								AVIOCTRLDEFs.SMsgAVIoctrlAVStream
										.parseContent(ioCid), 8);
						Log.d("TAG1", "speak" + speak);

						Log.d("TAG1", "avServStart开始");
						avCID = AVAPIs.avServStart(TUTKClient.IOTC_SID, null,
								null, 60, 0, ioCid);
						Log.d("TAG1", "avCID" + avCID);
						Toast.makeText(getApplicationContext(), "开始说话",
								Toast.LENGTH_SHORT).show();
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								while (isRecord) {
									record();
								}
							}
						}).start();

						break;
					case MotionEvent.ACTION_UP:
						Log.d("TAG1", "ACTION_UP");
						isRecord = false;
						Log.d("TAG1", "avCID" + avCID);
						if (avCID >= 0) {
							AVAPIs.avServStop(avCID);
							Log.d("TAG1", "avServStop");
						}
						if (speak == 0) {
							int stop = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
									AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SPEAKERSTOP,
									new byte[8], 8);
							Log.d("TAG1", "stop" + stop);
						}
						if (stopAudio == 0) {
							int starAudio = AVAPIs.avSendIOCtrl(
									TUTKClient.AV_CID,
									AVIOCTRLDEFs.IOTYPE_USER_IPCAM_AUDIOSTART,
									new byte[8], 8);
							Log.d("TAG1", "starAudio" + starAudio);
						}
						break;
					default:
						break;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("TAG1", "onTouch" + e);
				}
				return false;
			}
		});
		CameraButton.setOnClickListener(new MyClikListener());
		MP4RecordButton.setOnClickListener(new MyClikListener());
	}

	class MyClikListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			switch (v.getId()) {
			case R.id.LightControlButton:
				if (!ledON) {
					ledButton.setText("开灯");
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LEDON, new byte[8],
							8);
					if (ret == 0) {
						ledButton.setText("关灯");
						ledON = true;
					} else {
						Toast.makeText(getApplicationContext(), "开灯请求失败",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LEDOFF, new byte[8],
							8);
					if (ret == 0) {
						ledButton.setText("开灯");
						ledON = false;
					} else {
						Toast.makeText(getApplicationContext(), "关灯请求失败",
								Toast.LENGTH_SHORT).show();
					}
				}

				break;
			case R.id.AlarmButton:

				if (!alarmON) {
					alarmButton.setText("警开");
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVIOCTRLDEFs.IOTYPE_USER_IPCAM_ALARMON,
							new byte[8], 8);
					if (ret == 0) {
						alarmButton.setText("警关");
						alarmON = true;
					} else {
						Toast.makeText(getApplicationContext(), "打开报警请求失败",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVIOCTRLDEFs.IOTYPE_USER_IPCAM_ALAROFF,
							new byte[8], 8);
					if (ret == 0) {
						alarmButton.setText("警开");
						alarmON = false;
					} else {
						Toast.makeText(getApplicationContext(), "关闭报警请求失败",
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case R.id.RecordeControlButton:

				if (!recordeON) {
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORDRSTART,
							new byte[8], 8);
					if (ret == 0) {
						controlRecordeButton.setText("控制设备停止录像");
						recordeON = true;
					} else {
						Toast.makeText(getApplicationContext(), "控制设备打开录像失败",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
							AVIOCTRLDEFs.IOTYPE_USER_IPCAM_RECORDSTOP,
							new byte[8], 8);
					if (ret == 0) {
						controlRecordeButton.setText("控制设备开始录像");
						recordeON = false;
					} else {
						Toast.makeText(getApplicationContext(), "控制设备关闭录像失败",
								Toast.LENGTH_SHORT).show();
					}

				}
				break;
			case R.id.HighButton:
				int ret = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
						AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlSetStreamCtrlReq.parseContent(
								TUTKClient.AV_CID,
								(byte) AVIOCTRLDEFs.AVIOCTRL_QUALITY_HIGH), 8);
				if (ret < 0) {
					Log.e(TAG, "High视频质量设置失败" + ret);
					Toast.makeText(getApplicationContext(), "高视频质量设置失败",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(), "高视频质量设置成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.CentreButton:
				int res = AVAPIs
						.avSendIOCtrl(
								TUTKClient.AV_CID,
								AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ,
								AVIOCTRLDEFs.SMsgAVIoctrlSetStreamCtrlReq
										.parseContent(
												TUTKClient.AV_CID,
												(byte) AVIOCTRLDEFs.AVIOCTRL_QUALITY_MIDDLE),
								8);
				if (res < 0) {
					Log.e(TAG, "中视频质量设置失败" + res);
					Toast.makeText(getApplicationContext(), "中视频质量设置失败",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(), "中视频质量设置成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.LowButton:
				int r = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
						AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ,
						AVIOCTRLDEFs.SMsgAVIoctrlSetStreamCtrlReq.parseContent(
								TUTKClient.AV_CID,
								(byte) AVIOCTRLDEFs.AVIOCTRL_QUALITY_LOW), 8);
				if (r < 0) {
					Log.e(TAG, "中视频质量设置失败" + r);
					Toast.makeText(getApplicationContext(), "低视频质量设置失败",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(), "低视频质量设置成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.UpButton:
				int Up = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
						AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND,
						AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd.parseContent(
								(byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_UP, (byte) 1,
								(byte) 0, (byte) 0, (byte) 0,
								(byte) TUTKClient.AV_CID), 8);
				if (Up < 0) {
					Log.e(TAG, "UpButton" + Up);
					Toast.makeText(getApplicationContext(), "Up命令发送失败",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(), "Up命令发送成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.DownButton:
				int Down = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
						AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND,
						AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd.parseContent(
								(byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_DOWN,
								(byte) 1, (byte) 0, (byte) 0, (byte) 0,
								(byte) TUTKClient.AV_CID), 8);
				if (Down < 0) {
					Log.e(TAG, "UpButton");
					Toast.makeText(getApplicationContext(), "Down命令发送失败",
							Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "Down命令发送成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.LeftButton:
				int Left = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
						AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND,
						AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd.parseContent(
								(byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_LEFT,
								(byte) 1, (byte) 0, (byte) 0, (byte) 0,
								(byte) TUTKClient.AV_CID), 8);
				if (Left < 0) {
					Log.e(TAG, "LeftButton" + Left);
					Toast.makeText(getApplicationContext(), "Left命令发送失败",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(), "Left命令发送成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.RightButton:
				int Right = AVAPIs.avSendIOCtrl(TUTKClient.AV_CID,
						AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND,
						AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd.parseContent(
								(byte) AVIOCTRLDEFs.AVIOCTRL_PTZ_RIGHT,
								(byte) 1, (byte) 0, (byte) 0, (byte) 0,
								(byte) TUTKClient.AV_CID), 8);
				if (Right < 0) {
					Log.e(TAG, "RightButton" + Right);
					Toast.makeText(getApplicationContext(), "Right命令发送失败",
							Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(getApplicationContext(), "Right命令发送成功",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.CameraButton:
				if (mH264DecodeUtil.saveJPG("mnt/sdcard/IPC/"
						+ UUID.randomUUID().toString() + ".jpg") == 1) {
					Toast.makeText(getApplicationContext(), "成功保存一张图片",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.RecordButton:
				if (!TUTKClient.isRecordVideo) {
					MP4RecordButton.setBackground(getResources().getDrawable(
							R.drawable.live_record_1));
					// 开始录制视频
					TUTKClient.mRecordVideo.start("mnt/sdcard/IPC/"
							+ UUID.randomUUID().toString() + ".mp4");
					Toast.makeText(getApplicationContext(), "开始录制视频",
							Toast.LENGTH_SHORT).show();
					TUTKClient.isRecordVideo = true;
				} else {
					MP4RecordButton.setBackground(getResources().getDrawable(
							R.drawable.live_record_0));
					// 结束录制视频
					TUTKClient.mRecordVideo.stop();
					Toast.makeText(getApplicationContext(), "结束录制视频",
							Toast.LENGTH_SHORT).show();
					TUTKClient.isRecordVideo = false;
				}
				break;

			}
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// 是否总是执行
		Log.d(TAG, "onDestroy");
		TUTKClient.stop();
		mAudioTrack.stop();
		mAudioTrack.release();

	}
	
	public static class RenderThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int readNum = 0;
			while (TUTKClient.isConnect) {
				if (TUTKClient.isPlayVideo) {
					
					if (MyApplication.videoDataToPlay != null
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
							Log.d(TAG, "当前播放缓冲区的长度为："+MyApplication.videoDataToPlay.size());
							mRender.update(mVideoData.getVideoData(), mVideoData.getWidth(), mVideoData.getHeight());
							Log.d(TAG, "Render完成第"+mVideoData.getFrameNumber()+"帧视频");
						}
					}
				}
			}
		}

	}


	/** 开始录音 */

	public void record() {
		Log.d("TAG1", "开始录音");
		int frequency = 8000;
		int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ "/IPC/"
				+ UUID.randomUUID().toString()
				+ ".pcm");
		Log.d("TAG1", "record-file" + file.toString());
		// Delete any previous recording.
		if (file.exists())
			file.delete();
		// Create the new file.
		try {
			file.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create "
					+ file.toString());
		}
		try {
			// Create a DataOuputStream to write the audio data into the saved file.
			OutputStream os = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(os);
			DataOutputStream dos = new DataOutputStream(bos);
			// Create a new AudioRecord object to record the audio.
			int bufferSize = AudioRecord.getMinBufferSize(frequency,
					channelConfiguration, audioEncoding);
			Log.d("TAG1", "bufferSize" + bufferSize);
			AudioRecord audioRecord = new AudioRecord(
					MediaRecorder.AudioSource.MIC, frequency,
					channelConfiguration, audioEncoding, bufferSize);
			byte[] buffer = new byte[bufferSize];
			audioRecord.startRecording();
			while (isRecord) {
				int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
				Log.d("TAG1", "bufferReadResult" + bufferReadResult);
				if (avCID >= 0) {
					int r = AVAPIs.avSendAudioData(avCID, buffer,
							buffer.length, frameinfo, frameinfo.length);
					Log.d("TAG1", "avSendAudioData" + r);

				}
				System.out.println("ssssss---->>>" + bufferReadResult);
				for (int i = 0; i < bufferReadResult; i++)
					dos.writeShort(buffer[i]);
			}
			audioRecord.stop();
			dos.close();
		} catch (Throwable t) {
			Log.e("AudioRecord", "Recording Failed");
		}
	}

	@Override
	public void networkError() {
		// TODO Auto-generated method stub
		Looper.prepare();
		Log.d("接收到断网", "接收到断网");
		Toast.makeText(this, "连接已断开，请退出后重新连接", Toast.LENGTH_LONG).show();
		Looper.loop();
	}

	@Override
	public void someError() {
		// TODO Auto-generated method stub
		Looper.prepare();// 这样好像把子线程就给关了，正式现在想要的，如果子线程不想关那就得用Handler
		Log.d("遇到错误", "遇到错误");
		Toast.makeText(this, "音频接收时遇到错误，请重新连接", Toast.LENGTH_LONG).show();
		Looper.loop();
	}

	/**
	 * 将byte数组变成字符串
	 * 
	 * @param b
	 *            待处理字符串
	 * @param len
	 *            字符串长度
	 * @return 产生的字符串
	 */
	public static String Bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			ret += hex.toUpperCase();
		}
		return ret;
	}
}
