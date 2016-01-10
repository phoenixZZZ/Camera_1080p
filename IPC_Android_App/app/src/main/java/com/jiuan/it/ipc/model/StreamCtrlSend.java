package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 控制视频质量
 */
public class StreamCtrlSend extends  SendModel implements Parcelable {

    public static final  int      AVIOCTRL_QUALITY_UNKNOWN =0;

    public static final  int      AVIOCTRL_QUALITY_MAX =1;

    public static final  int      AVIOCTRL_QUALITY_HIGH =2;

    public static final  int      AVIOCTRL_QUALITY_MIDDLE =3;

    public static final  int      AVIOCTRL_QUALITY_LOW =4;

    public static final  int      AVIOCTRL_QUALITY_MIN =5;

    @SerializedName("StreamCtrlType")
    private int streamType;  //控制视频质量

    public static final Creator<StreamCtrlSend> CREATOR = new Creator<StreamCtrlSend>() {
        @Override
        public StreamCtrlSend createFromParcel(Parcel in) {
            StreamCtrlSend model = new StreamCtrlSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.streamType = in.readInt();

            return model;
        }

        @Override
        public StreamCtrlSend[] newArray(int size) {
            return new StreamCtrlSend[size];
        }
    };

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
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
        parcel.writeInt(streamType);
    }
}