package com.jiuan.it.ipc.utils;

public class CombineVideo {
	static {
		System.loadLibrary("CombineVideo");
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
	}
	
	public native int combine(String inFileName1,String inFileName2,String outFileName,int isAudio);
	
}
