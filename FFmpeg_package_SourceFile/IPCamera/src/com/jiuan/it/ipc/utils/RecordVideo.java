package com.jiuan.it.ipc.utils;

public class RecordVideo {
	static {
		System.loadLibrary("RecordVideo");
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
	}
	public native int start(String str);
	public native int record(byte[] data, int len);
	public native int stop();
}
