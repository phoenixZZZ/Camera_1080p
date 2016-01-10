package com.jiuan.it.ipc.model;

public class AudioData {
	byte[] audioData;//帧数据
	int timeStamp;//时间戳
	int size;//帧大小
	int codeId;
	int frameNumber;//帧编码
	public byte[] getAudioData() {
		return audioData;
	}
	public void setAudioData(byte[] audioData) {
		//Modify By Hjr 20151105 Start
		//修正声音播放异常的问题
		this.audioData = new byte[4096];
		this.audioData = audioData.clone();
		//Modify By Hjr 20151105 End
	}
	public int getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(int timeStamp) {
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
