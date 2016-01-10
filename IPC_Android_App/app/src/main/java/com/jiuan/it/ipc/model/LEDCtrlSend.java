package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 *  控制LED灯
 */
public class LEDCtrlSend extends  SendModel implements Parcelable {

    //控制开关
    public static final  int  IOTYPE_USER_IPCAM_LEDOFF=0;

    public static final  int  IOTYPE_USER_IPCAM_LEDON=1 ;

    @SerializedName("LEDCtrlType")
    private int ledType;

    public static final Creator<LEDCtrlSend> CREATOR = new Creator<LEDCtrlSend>() {
        @Override
        public LEDCtrlSend createFromParcel(Parcel in) {
            LEDCtrlSend model = new LEDCtrlSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.ledType = in.readInt();

            return model;
        }

        @Override
        public LEDCtrlSend[] newArray(int size) {
            return new LEDCtrlSend[size];
        }
    };

    public int getLedType() {
        return ledType;
    }

    public void setLedType(int ledType) {
        this.ledType = ledType;
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
        parcel.writeInt(ledType);
    }
}