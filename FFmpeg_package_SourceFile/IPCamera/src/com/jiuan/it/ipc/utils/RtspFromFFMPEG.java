package com.jiuan.it.ipc.utils;



/** 
 * @author chao 
 * @date 2015-7-22上午10:50:30
 * @Title: RtspFromFFMPEG.java 
 * @Package com.example.myrtspplayer 
 */
public class RtspFromFFMPEG {
	
	static {
		System.loadLibrary("RtspFromFFMPEG");
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("avdevice-56");
	}
	
	public native int init(String url);

	public native int decode(byte[] data);
	
	public native int thread_RecvPacket(long seek_pos, int seek_req, int timebase_seekDen, int timebase_seekNum);
	
	public native int thread_RecvPacket_Mediacodec();

	public native int thread_DecodePacket(byte[] data, double[] pts_video,int IsRecord);

	public native int thread_DecodePacket_audio(byte[] data, double[] pts_audio);
	
	public native int thread_MediaCodecPacket(byte[] data);
	
	public native int[] decodeYUV420SP(byte[] buf, int width, int heigth);
	
	public native int PCM2G711a(byte[] InAudioData, byte[] outAudioData, int DataLen);
	
	public native int close();
}
