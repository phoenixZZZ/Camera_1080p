package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * （15）控制摄像头转动（新版云台）
 */
public class PTZSetSend extends  SendModel implements Parcelable {


    @SerializedName("PTZAngleSet")
    private int angleSet;  //角度参数n（取值0——360）

    public static final Creator<PTZSetSend> CREATOR = new Creator<PTZSetSend>() {
        @Override
        public PTZSetSend createFromParcel(Parcel in) {
            PTZSetSend model = new PTZSetSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.angleSet = in.readInt();

            return model;
        }

        @Override
        public PTZSetSend[] newArray(int size) {
            return new PTZSetSend[size];
        }
    };

    public int getAngleSet() {
        return angleSet;
    }

    public void setAngleSet(int angleSet) {
        this.angleSet = angleSet;
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
        parcel.writeInt(angleSet);
    }
}