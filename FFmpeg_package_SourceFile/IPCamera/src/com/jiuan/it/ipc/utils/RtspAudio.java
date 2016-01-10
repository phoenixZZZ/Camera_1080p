package com.jiuan.it.ipc.utils;
/** 
 * @author chao 
 * @date 2015-7-31上午11:18:09
 * @Title: RtspVideo.java 
 * @Package com.example.myrtspplayer 
 */
public class RtspAudio {
	static {
		System.loadLibrary("RtspAudio");
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
	}
	public native int init(String url);

	public native byte[] decode();
	
	public native int thread_RecvPacket();
	
	public native int thread_RecvPacket_Mediacodec();
	
	public native int thread_DecodePacket(byte[] data, int len);
	
	public native int thread_MediaCodecPacket(byte[] data);

	public native int close();

}
