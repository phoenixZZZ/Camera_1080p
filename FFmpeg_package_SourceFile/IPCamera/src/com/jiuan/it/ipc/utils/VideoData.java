package com.jiuan.it.ipc.utils;

public class VideoData {
	byte[] videoData;//帧数据
	boolean isIFrame;//帧类型
	long timeStamp;//时间戳
	int size;//帧大小
	int codeId;//编码类型
	int frameNumber;//帧编号
	int width;
	int height;
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public byte[] getVideoData() {
		return videoData;
	}
	public void setVideoData(byte[] videoData) {
		this.videoData = videoData;
	}
	public boolean isIFrame() {
		return isIFrame;
	}
	public void setIFrame(boolean isIFrame) {
		this.isIFrame = isIFrame;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getCodeId() {
		return codeId;
	}
	public void setCodeId(int codeId) {
		this.codeId = codeId;
	}
	public int getFrameNumber() {
		return frameNumber;
	}
	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}
	
	

}
