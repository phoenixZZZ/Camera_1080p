package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 全景图片功能
 */
public class PanoramicCtrlSend extends  SendModel implements Parcelable {


    @SerializedName("PictureCount")
    private int pictureCount;

    public static final Creator<PanoramicCtrlSend> CREATOR = new Creator<PanoramicCtrlSend>() {
        @Override
        public PanoramicCtrlSend createFromParcel(Parcel in) {
            PanoramicCtrlSend model = new PanoramicCtrlSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.pictureCount = in.readInt();
            return model;
        }

        @Override
        public PanoramicCtrlSend[] newArray(int size) {
            return new PanoramicCtrlSend[size];
        }
    };

    public int getPictureCount() {
        return pictureCount;
    }

    public void setPictureCount(int pictureCount) {
        this.pictureCount = pictureCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(idcp);
        parcel.writeString(header);
        parcel.writeStringArray(target);
        parcel.writeInt(mark);
        parcel.writeStringArray(server);
        parcel.writeStringArray(client);
        parcel.writeInt(channel);
        parcel.writeInt(pictureCount);
    }
}