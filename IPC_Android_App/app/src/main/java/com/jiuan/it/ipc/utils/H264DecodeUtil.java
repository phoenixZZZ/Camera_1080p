package com.jiuan.it.ipc.utils;

public class H264DecodeUtil {
	static {
		System.loadLibrary("h264decodeutil");
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
	}

	public native int registeriInit();

	public native int decode(byte[] data, int len,int[] width,int[] height, byte[] outData);

	public native int saveJPG(String str);
	
	public native int close();
}
