package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 报警控制
 */
public class AlarmCtrlSend extends  SendModel implements Parcelable {

    //控制开关
    public static final  int  IOTYPE_USER_IPCAM_ALARMOFF=0;

    public static final  int  IOTYPE_USER_IPCAM_ALARMON=1 ;

    @SerializedName("AlarmCtrlType")
    private int alarmType;  //控制LED灯

    public static final Creator<AlarmCtrlSend> CREATOR = new Creator<AlarmCtrlSend>() {
        @Override
        public AlarmCtrlSend createFromParcel(Parcel in) {
            AlarmCtrlSend model = new AlarmCtrlSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.alarmType = in.readInt();

            return model;
        }

        @Override
        public AlarmCtrlSend[] newArray(int size) {
            return new AlarmCtrlSend[size];
        }
    };

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
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
        parcel.writeInt(alarmType);
    }
}