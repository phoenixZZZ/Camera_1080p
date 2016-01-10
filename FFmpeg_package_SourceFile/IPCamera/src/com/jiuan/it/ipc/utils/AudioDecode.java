package com.jiuan.it.ipc.utils;

public class AudioDecode {
	

	static {
		System.loadLibrary("AudioDecode");
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
	}
	
	public native int init();
	public native int findAACDecoder();
	public native int findG711aDecoder();
	public native int aacDecode(byte[] data,int len,byte[] outdata);
	public native int g711aDecode(byte[] data,int len,byte[] outdata);
	public native int close();

}
