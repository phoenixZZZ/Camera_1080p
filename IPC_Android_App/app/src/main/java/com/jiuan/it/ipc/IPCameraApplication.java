package com.jiuan.it.ipc;

import android.app.Application;
import android.content.Context;

import com.jiuan.it.ipc.model.AudioData;
import com.jiuan.it.ipc.model.VideoData;
import com.jiuan.it.ipc.tools.LogcatHelper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import im.fir.sdk.FIR;

public class IPCameraApplication extends Application {
	
	public static final int DEVICE_DEFAULT_PORT = 5765;//默认端口
	public static final int PHONE_DEFAULT_PORT = 5765;
	public static final int MAX_DATA_PACKET_LENGTH = 128;
	public static final byte[] K1 = {99,111,109,46,106,105,117,97,110,46,105,116,116,101,115,116};
	public static final byte[] ACC = {49, 49, 49, 49};
	public static final byte[] SUC = {49, 49, 49, 49};
//	public static final byte[] IC = {98, 99, 100, 51, 98, 99, 49, 98, 98, 54, 50, 54, 52, 56, 53, 56};
	public static final byte[] IC = {67, 79, 77, 46, 74, 73, 85, 65, 78, 46, 68, 69, 86, 73, 67, 69};
	public static boolean UDP_BROADCAST_START = true;
	public static boolean TWO_WAY_CERTIFY_START = false;
	public static boolean TCPSERVER_RECIVE = true;
	public static long T1 ;
	public static long T2 ;
	public static byte [] ID = getByteArray(16);
	public static byte[] R1 = getByteArray(16);
	public static byte [] TOKEN = new byte[4];
	public static byte[] RECIVE_DATA = getByteArray(128);
	public static byte[] RESULT = getByteArray(128);
	public static String DEVICE_IP ;
	
	public static long getT1() {
		return T1;
	}
	public static void setT1(long t1) {
		T1 = t1;
	}
	public static long getT2() {
		return T2;
	}
	public static void setT2(long t2) {
		T2 = t2;
	}
	//生成随机数R1
	//byte[] r1 = Data.intToByteArray(new Random().nextInt(10) + 1);
	
	public static byte[] getR1() {
		return R1;
	}
	public static void setR1(byte[] r1) {
		System.arraycopy(r1, 0, R1, 0, 4);
		System.arraycopy(r1, 0, R1, 4, 4);
		System.arraycopy(r1, 0, R1, 8, 4);
		System.arraycopy(r1, 0, R1, 12, 4);
	}
	
	public static byte[] getID() {
		return ID;
	}
	public static void setID(byte[] iD) {
		ID = iD;
	}

	public static byte[] getTOKEN() {
		return TOKEN;
	}

	public static void setTOKEN(byte[] TOKEN) {
		IPCameraApplication.TOKEN = TOKEN;
	}

	public static byte[] getRECIVE_DATA() {
		return RECIVE_DATA;
	}
	public static void setRECIVE_DATA(byte[] rECIVE_DATA) {
		RECIVE_DATA = rECIVE_DATA;
	}
	
	public static String getDEVICE_IP() {
		return DEVICE_IP;
	}
	public static void setDEVICE_IP(String dEVICE_IP) {
		DEVICE_IP = dEVICE_IP;
	}
	public static boolean isTWO_WAY_CERTIFY_START() {
		return TWO_WAY_CERTIFY_START;
	}
	public static void setTWO_WAY_CERTIFY_START(boolean tCPCLIENT_START) {
		TWO_WAY_CERTIFY_START = tCPCLIENT_START;
	}

	public static byte[] getRESULT() {
		return RESULT;
	}
	public static void setRESULT(byte[] result) {
		RESULT = result;
	}

	public static byte[] getByteArray(int num) {
		if (num < 0) return null;
		byte[] result = new byte[num];
		for (int i = 0; i < num; i++) {
			result[i] = 48;
		}

		return result;
	}

	//音视频缓存
	public static List<VideoData> videoDataToPlay = null;//视频播放缓冲区

	public static final int bufferSize = 2;//视频播放缓冲区大小

	public static List<AudioData> audioDataList = null;//音频缓冲区

	private Context context;
	@Override
	public void onCreate() {
		super.onCreate();
		//保存打印信息到本地
		LogcatHelper.getInstance(this).start();
		videoDataToPlay = Collections.synchronizedList(new LinkedList<VideoData>());
		audioDataList = Collections.synchronizedList(new LinkedList<AudioData>());
		setupFIR();
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}


	private void setupFIR() {
		FIR.init(this);
	}
}
