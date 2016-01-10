package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoImage implements Parcelable {
    private String strVideoFileName;  //录像文件
    private int    iImageCount;       //录像文件的预览图片数

    public static final Creator<VideoImage> CREATOR = new Creator<VideoImage>() {
        @Override
        public VideoImage createFromParcel(Parcel in) {
            VideoImage model = new VideoImage();
            model.strVideoFileName = in.readString();
            model.iImageCount = in.readInt();
            return model;
        }

        @Override
        public VideoImage[] newArray(int size) {
            return new VideoImage[size];
        }
    };

    public String getStrVideoFileName() {
        return strVideoFileName;
    }

    public void setStrVideoFileName(String strVideoFileName) {
        this.strVideoFileName = strVideoFileName;
    }

    public int getiImageCount() {
        return iImageCount;
    }

    public void setiImageCount(int iImageCount) {
        this.iImageCount = iImageCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(strVideoFileName);
        dest.writeInt(iImageCount);
    }
}
