package com.jiuan.it.ipc.utils;

public class AudioData {
	byte[] audioData;//帧数据
	long timeStamp;//时间戳
	int size;//帧大小
	int codeId;
	int frameNumber;//帧编码
	public byte[] getAudioData() {
		return audioData;
	}
	public void setAudioData(byte[] audioData) {
		this.audioData = audioData;
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
