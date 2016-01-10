package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 控制摄像头回到零点位置（新版云台）
 */
public class PTZZeroSend extends  SendModel implements Parcelable {

    public static final Creator<PTZZeroSend> CREATOR = new Creator<PTZZeroSend>() {
        @Override
        public PTZZeroSend createFromParcel(Parcel in) {
            PTZZeroSend model = new PTZZeroSend();
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
        public PTZZeroSend[] newArray(int size) {
            return new PTZZeroSend[size];
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