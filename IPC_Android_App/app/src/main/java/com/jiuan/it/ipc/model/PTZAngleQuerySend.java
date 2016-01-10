package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 查询当前云台位置角度值（新版云台）
 */
public class PTZAngleQuerySend extends  SendModel implements Parcelable {

    public static final Creator<PTZAngleQuerySend> CREATOR = new Creator<PTZAngleQuerySend>() {
        @Override
        public PTZAngleQuerySend createFromParcel(Parcel in) {
            PTZAngleQuerySend model = new PTZAngleQuerySend();
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
        public PTZAngleQuerySend[] newArray(int size) {
            return new PTZAngleQuerySend[size];
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