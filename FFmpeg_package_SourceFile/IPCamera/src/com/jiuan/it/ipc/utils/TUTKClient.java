package com.jiuan.it.ipc.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import android.util.Log;

import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.St_SInfo;



public class TUTKClient {
	public static byte[] video = new byte[2764800];
	public static byte[] audio;
	private static String TAG = "TUTKClient";
	public static int IOTC_SID;
	public static int AV_CID;
	public static Boolean isConnect = false;//连接成功的标志位
	public static Boolean isDelete = false;//跳过P帧的标志位
	public static Boolean isPlayVideo = false;//是否可以开始播放视频
	public static boolean isRecordVideo = false;//视频录制
	public static RecordVideo mRecordVideo = new RecordVideo() ;//视频录制工具
	static H264DecodeUtil mH264DecodeUtil = new H264DecodeUtil();// 解码音视频
   
	public static int[] width = new int[1];
	public static int[] height = new int[1];
	

	public static boolean start(String uid, String Username, String Password) {
		mRecordVideo = new RecordVideo();
		Log.e(TAG, "start");
		// 初始化IOTC module
		int ret = IOTCAPIs.IOTC_Initialize(0, "m1.iotcplatform.com",
				"m2.iotcplatform.com", "m4.iotcplatform.com",
				"m5.iotcplatform.com");
		if (ret != IOTCAPIs.IOTC_ER_NoERROR) {
			Log.e(TAG, "IOTCAPIs_Device exit...!!\n");
			System.out.printf("IOTCAPIs_Device exit...!!\n");
			Log.d(TAG, "IOTC_Initialize返回值"+ret);
			return false;
		}

		// 初始化AV module
		AVAPIs.avInitialize(3);
		// 通过UID连接设备 成功则返回IOTC session ID
		int sid = IOTCAPIs.IOTC_Connect_ByUID(uid);
		if (sid < 0) {
			if (sid == IOTCAPIs.IOTC_ER_CAN_NOT_FIND_DEVICE) {
				Log.e(TAG, "IOTC服务器无法找到指定的设备");
			}
			Log.e(TAG, "通过UID连接设备失败" + sid);
			return false;
		} else {
			IOTC_SID = sid;
			Log.d(TAG, "成功连接设备后返回的SID值为：" + IOTC_SID);
		}
		System.out.printf("Step 2: call IOTC_Connect_ByUID(%s).......\n", uid);
		St_SInfo s_Info = new St_SInfo();
		int r =IOTCAPIs.IOTC_Session_Check(IOTC_SID, s_Info);
		if(r ==0){
			switch(s_Info.Mode){
			case 0:
				Log.d(TAG, "连接方式为：P2P mode");
				break;
			case 1:
				Log.d(TAG, "连接方式为： Relay mode");
				break;
			case 2:
				Log.d(TAG, "连接方式为： LAN mode");
				break;
			}
		}

		int[] srvType = new int[1];
		int[] pnResend = new int[1];
		// 通过账号密码开启Client 成功则返回AV channel ID
		int avIndex = AVAPIs.avClientStart2(sid, Username, Password, 20000,
				srvType, 0,pnResend);
		Log.d(TAG, "srvType"+srvType[0]);
		System.out.printf("Step 2: call avClientStart(%d).......\n", avIndex);
		if (avIndex < 0) {
			if(avIndex == AVAPIs.AV_ER_WRONG_ACCPWD_LENGTH){
				Log.e(TAG, "密码长度错误" + avIndex);
			}
			if(avIndex == AVAPIs.AV_ER_WRONG_VIEWACCorPWD){
				Log.e(TAG, "账号或密码错误" + avIndex);
			}
			System.out.printf("avClientStart failed[%d]\n", avIndex);
			Log.e(TAG, "avClientStart failed[%d]\n" + avIndex);
			return false;
		} else {
			AV_CID = avIndex;
			isConnect = true;
			Log.d(TAG, "成功开启Client后获得CID值为：" + AV_CID);
		}
		Log.d(TAG, "start+isConnect值为：" + isConnect);
		return true;
	}

	public static void stop() {
		Log.d(TAG, "stop");
		Log.d(TAG, "AV_CID"+AV_CID);
		if (AV_CID >= 0&&IOTC_SID>0) {
			//发送视频停止命令
			int IOTYPE_USER_IPCAM_STOP	= 0x02FF;
			int ret = AVAPIs.avSendIOCtrl(AV_CID, IOTYPE_USER_IPCAM_STOP, new byte[8],
					8);
			Log.d(TAG, "发送视频停止命令"+ret);
			if (ret < 0) {
				Log.e(TAG, "发送视频停止命令失败");
			}
			AVAPIs.avClientStop(AV_CID);
			Log.d(TAG, "avClientStop OK");
			System.out.printf("avClientStop OK\n");
			IOTCAPIs.IOTC_Session_Close(IOTC_SID);
			Log.d(TAG, "IOTC_Session_Close OK");
			System.out.printf("IOTC_Session_Close OK\n");
		}
		isConnect = false;
		AVAPIs.avDeInitialize();
		IOTCAPIs.IOTC_DeInitialize();
		Log.d(TAG, "StreamClient exit...");
		System.out.printf("StreamClient exit...\n");
	}

	public static boolean startIpcamStream() {
		Log.d(TAG, "AV_CID  " + AV_CID);
		// 请求发送视频
		int IOTYPE_USER_IPCAM_START = 0x1FF;
		int ret = AVAPIs.avSendIOCtrl(AV_CID, IOTYPE_USER_IPCAM_START, new byte[8],
				8);
		if (ret < 0) {
			Log.e(TAG, "请求视频数据失败");
			System.out.printf("start_ipcam_stream failed[%d]\n", ret);
			return false;
		}
//		// 请求发送音频
//		int IOTYPE_USER_IPCAM_AUDIOSTART = 0x300;
//		ret = av.avSendIOCtrl(AV_CID, IOTYPE_USER_IPCAM_AUDIOSTART,
//				new byte[8], 8);
//		if (ret < 0) {
//			Log.e(TAG, "请求音频数据失败");
//			System.out.printf("start_ipcam_stream failed[%d]\n", ret);
//			return false;
//		}

		return true;
	}

	public static class VideoThread implements Runnable {
		static final int VIDEO_BUF_SIZE = 100000;
		static final int FRAME_INFO_SIZE = 16;
		static H264DecodeUtil mH264DecodeUtil = new H264DecodeUtil();// 解码音视频
		public interface NetworkListener{
			void networkError();
		}
		private NetworkListener mNetworkListener;
		public VideoThread(NetworkListener listener) {
			mNetworkListener = listener;
		}
		@Override
		public void run() {
			System.out.printf("[%s] Start\n", Thread.currentThread().getName());
			// 初始化音频解码器
			mH264DecodeUtil.registeriInit();
			byte[] frameInfo = new byte[FRAME_INFO_SIZE];
			byte[] videoBuffer = new byte[VIDEO_BUF_SIZE];
			byte[] time = new byte[4];
			while (isConnect) {
				int[] frameNumber = new int[1];
				int[] outBufSize = new int[1];
				int[] outFrmSize = new int[1];
				int[] outFrmInfoBufSize = new int[1];
				int ret = AVAPIs.avRecvFrameData2(AV_CID, videoBuffer,
						VIDEO_BUF_SIZE, outBufSize, outFrmSize, frameInfo,
						FRAME_INFO_SIZE, outFrmInfoBufSize, frameNumber);
				Log.d(TAG, "实际接收数据长度:"+ret+" videoBuffer.length"+videoBuffer.length);
				if (ret == AVAPIs.AV_ER_DATA_NOREADY) {
					try {
						Thread.sleep(30);
						continue;
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
						break;
					}
				} else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {

					System.out.printf("[%s] Lost video frame number[%d]\n",
							Thread.currentThread().getName(), frameNumber[0]);
					Log.e(TAG, "丢失的完整帧的id" + frameNumber[0]+"!!!!!!!!!!!!!!!!!!!");
					isDelete = true;
					continue;
				} else if (ret == AVAPIs.AV_ER_INCOMPLETE_FRAME) {
					System.out.printf(
							"[%s] Incomplete video frame number[%d]\n", Thread
									.currentThread().getName(), frameNumber[0]);
					Log.e(TAG, "丢失的不完整帧的id" + frameNumber[0]+"!!!!!!!!!!!!!!!!!!!");
					isDelete = true;
					continue;
				} else if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
					System.out.printf("[%s] AV_ER_SESSION_CLOSE_BY_REMOTE\n",
							Thread.currentThread().getName());
					Log.e(TAG, "远程站点已经关闭IOTC会话"
							+ Thread.currentThread().getName());
					mNetworkListener.networkError();
					break;
				} else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
					System.out.printf("[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n",
							Thread.currentThread().getName());
					Log.e(TAG, "IOTC会话断开\n" + Thread.currentThread().getName());
					mNetworkListener.networkError();
					break;
				} else if (ret == AVAPIs.AV_ER_INVALID_SID) {
					System.out.printf("[%s] Session cant be used anymore\n",
							Thread.currentThread().getName());
					Log.e(TAG, "IOTC会话在指定AV通道中是无效的\n"
							+ Thread.currentThread().getName());
					mNetworkListener.networkError();
					break;
				}
				//确保帧信息无误
				if (frameInfo[2] == 0) {
					Log.d(TAG, "第" + frameNumber[0] + "帧数据是P帧大小为"+outBufSize[0]);
				} else if (frameInfo[2] == 1) {
					Log.d(TAG, "第" + frameNumber[0] + "帧数据是I帧大小为"+outBufSize[0]);
				}
				//抛弃多余P帧数据
				if(isDelete&&frameInfo[2] == 0){
					Log.e(TAG, "跳过了第" + frameNumber[0] + "帧数据，该帧是是P帧");
					continue;
				}
				
				VideoData mVideoData = new VideoData();
				mVideoData.setVideoData(mH264DecodeUtil.decode(videoBuffer,ret,width,height));//获取解码后的数据
				mVideoData.setFrameNumber(frameNumber[0]);
				mVideoData.setWidth(width[0]);
				mVideoData.setHeight(height[0]);
				mVideoData.setSize(width[0]*height[0]*3);
				// 录像
				if (isRecordVideo) {
					Log.e(TAG, "开始录制!!!");
//					mRecordVideo.record(mVideoData.getVideoData(),
//							mVideoData.getSize());
					mRecordVideo.record(videoBuffer, ret);
				}
				//保存至播放缓冲区
				if(mVideoData.getVideoData()!=null){
					MyApplication.videoDataToPlay.add(mVideoData);
					isDelete = false;//当解码正常时不用抛弃多余的P帧
					while(MyApplication.videoDataToPlay.size()>=MyApplication.bufferSize){
						isPlayVideo = true;
					}
				}else {
					Log.e(TAG, "解码返回值为空");
					isDelete = true;//当解码出错时要抛弃多余的P帧
				}
			}

			System.out.printf("[%s] Exit\n", Thread.currentThread().getName());
			isPlayVideo = false ;
			mH264DecodeUtil.close();
			
		}
	}

	public static class AudioThread implements Runnable {
		static final int AUDIO_BUF_SIZE = 1024;
		static final int FRAME_INFO_SIZE = 16;
		public interface ErrorListener{
			void someError();
		}
		private ErrorListener mErrorListener;
		public AudioThread(ErrorListener listener) {
			mErrorListener = listener;
		}
		@Override
		public void run() {
			System.out.printf("[%s] Start\n", Thread.currentThread().getName());
			byte[] frameInfo = new byte[FRAME_INFO_SIZE];
			byte[] audioBuffer = new byte[AUDIO_BUF_SIZE];
			byte[] times = new byte[4];
			int i=0;
			while (isConnect) {
				int ret = AVAPIs.avCheckAudioBuf(AV_CID);
                Log.d(TAG, "yinyinyin音频接收缓冲区帧数"+ret);
				if (ret < 0) {
					// Same error codes as below
					System.out.printf("[%s] avCheckAudioBuf() failed: %d\n",
							Thread.currentThread().getName(), ret);
					break;
				} else if (ret < 3) {
					try {
						Thread.sleep(120);
						continue;
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
						break;
					}
				}
				int[] frameNumber = new int[1];
				ret = AVAPIs.avRecvAudioData(AV_CID, audioBuffer, AUDIO_BUF_SIZE,
						frameInfo, FRAME_INFO_SIZE, frameNumber);
				 Log.d(TAG, "音频接收数据长度"+ret);
				if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
					System.out.printf("[%s] AV_ER_SESSION_CLOSE_BY_REMOTE\n",
							Thread.currentThread().getName());
					mErrorListener.someError();
					break;
				} else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
					System.out.printf("[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n",
							Thread.currentThread().getName());
					mErrorListener.someError();
					break;
				} else if (ret == AVAPIs.AV_ER_INVALID_SID) {
					System.out.printf("[%s] Session cant be used anymore\n",
							Thread.currentThread().getName());
					mErrorListener.someError();
					break;
				} else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
					continue;
				} else if(ret == AVAPIs.AV_ER_DATA_NOREADY){
					try {
						Thread.sleep(30);
						continue;
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
						break;
					}
				}else if (ret>0) {
					
				} 
					AudioData mAudioData = new AudioData();
					mAudioData.setAudioData(audioBuffer);
					mAudioData.setSize(audioBuffer.length);
					mAudioData.setCodeId(frameInfo[0]);
					mAudioData.setFrameNumber(frameNumber[0]);
					times[0] = frameInfo[15];
					times[1] = frameInfo[14];
					times[2] = frameInfo[13];
					times[3] = frameInfo[12];
					 i++;
					Log.d(TAG,"i"+i+"音频接收第"+frameNumber[0]+"帧音频的时间戳" + Bytes2HexString(times)+"编码格式"+Integer.toHexString(frameInfo[0]));
	                mAudioData.setTimeStamp(byteArrayToInt(times));
	                if(MyApplication.audioDataList.size()<10){
	                	Log.d(TAG, "音频接收audioDataList的长度"+MyApplication.audioDataList.size());
	                	MyApplication.audioDataList.add(mAudioData);
	                }else {
						while(true){
							MyApplication.audioDataList.remove(0);
							if(MyApplication.audioDataList.size()<10)
								break;
						}
						Log.d(TAG, "音频接收audioDataList的长度"+MyApplication.audioDataList.size());
						MyApplication.audioDataList.add(mAudioData);
					}
					Log.d(TAG, "音频接收audioBuffer.length" + audioBuffer.length);
				}
			System.out.printf("[%s] Exit\n", Thread.currentThread().getName());
		}
	}

	public static int byteArrayToInt(byte[] b) {
		ByteArrayInputStream bintput = new ByteArrayInputStream(b);
		DataInputStream dintput = new DataInputStream(bintput);
		int i = 0;
		try {
			i = dintput.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	/**
	 * 将byte数组变成字符串
	 * 
	 * @param b 待处理字符串
	 * @param len 字符串长度
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
