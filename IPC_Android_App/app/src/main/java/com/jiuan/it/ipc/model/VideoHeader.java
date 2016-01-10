package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoHeader implements Parcelable {

    private int 	 videoSum; 	//录像文件个数 4
    private long 	 overall; 	//总时长 8
    private int   seqNumFile;	//文件序号，0——N-1， 4
    private String   nameFile;	//录像文件名称 32
    private long 	 timeFile;  	//文件时长   8

    public static final Creator<VideoHeader> CREATOR = new Creator<VideoHeader>() {
        @Override
        public VideoHeader createFromParcel(Parcel in) {
            VideoHeader model = new VideoHeader();
            model.videoSum = in.readInt();
            model.overall = in.readLong();
            model.seqNumFile = in.readInt();
            model.nameFile = in.readString();
            model.timeFile = in.readLong();
            return model;
        }

        @Override
        public VideoHeader[] newArray(int size) {
            return new VideoHeader[size];
        }
    };

    public int getVideoSum() {
        return videoSum;
    }

    public void setVideoSum(int videoSum) {
        this.videoSum = videoSum;
    }

    public long getOverall() {
        return overall;
    }

    public void setOverall(long overall) {
        this.overall = overall;
    }

    public int getSeqNumFile() {
        return seqNumFile;
    }

    public void setSeqNumFile(int seqNumFile) {
        this.seqNumFile = seqNumFile;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public long getTimeFile() {
        return timeFile;
    }

    public void setTimeFile(long timeFile) {
        this.timeFile = timeFile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(videoSum);
        dest.writeLong(overall);
        dest.writeInt(seqNumFile);
        dest.writeString(nameFile);
        dest.writeLong(timeFile);


    }
}
