package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 查询有无云台（新版云台）
 */
public class PTZQuerySend extends  SendModel implements Parcelable {

    public static final Creator<PTZQuerySend> CREATOR = new Creator<PTZQuerySend>() {
        @Override
        public PTZQuerySend createFromParcel(Parcel in) {
            PTZQuerySend model = new PTZQuerySend();
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
        public PTZQuerySend[] newArray(int size) {
            return new PTZQuerySend[size];
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