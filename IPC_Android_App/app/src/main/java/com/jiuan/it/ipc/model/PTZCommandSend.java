package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * 控制摄像头转动
 */
public class PTZCommandSend extends  SendModel implements Parcelable {

    //控制摄像头转动
    public static final  int  AVIOCTRL_PTZ_STOP=0;

    public static final  int  AVIOCTRL_PTZ_UP=1;

    public static final  int  AVIOCTRL_PTZ_DOWN=2;

    public static final  int  AVIOCTRL_PTZ_LEFT=3;

    public static final  int  AVIOCTRL_PTZ_RIGHT=6;

    public static final  int  AVIOCTRL_PTZ_START=20;

    @SerializedName("PTZCommandType")
    private int commandType; //控制摄像头转动

    @SerializedName("PTZCommandStep")
    private int commandStep; //转动步数参数

    @SerializedName("PTZCommandAngle")
    private int commandAngle; //转动角度参数


    public static final Creator<PTZCommandSend> CREATOR = new Creator<PTZCommandSend>() {
        @Override
        public PTZCommandSend createFromParcel(Parcel in) {
            PTZCommandSend model = new PTZCommandSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.commandType = in.readInt();
            model.commandStep = in.readInt();
            model.commandAngle = in.readInt();
            return model;
        }

        @Override
        public PTZCommandSend[] newArray(int size) {
            return new PTZCommandSend[size];
        }
    };


    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    public int getCommandStep() {
        return commandStep;
    }

    public void setCommandStep(int commandStep) {
        this.commandStep = commandStep;
    }

    public int getCommandAngle() {
        return commandAngle;
    }

    public void setCommandAngle(int commandAngle) {
        this.commandAngle = commandAngle;
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
        parcel.writeInt(commandStep);
        parcel.writeInt(commandAngle);
        parcel.writeInt(commandType);
    }
}