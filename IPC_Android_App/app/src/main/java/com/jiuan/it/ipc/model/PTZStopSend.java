package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 控制摄像头转动紧急停止（新版云台）
 */
public class PTZStopSend extends  SendModel implements Parcelable {

    public static final Creator<PTZStopSend> CREATOR = new Creator<PTZStopSend>() {
        @Override
        public PTZStopSend createFromParcel(Parcel in) {
            PTZStopSend model = new PTZStopSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();

            return model;
        }

        @Override
        public PTZStopSend[] newArray(int size) {
            return new PTZStopSend[size];
        }
    };

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
    }
}