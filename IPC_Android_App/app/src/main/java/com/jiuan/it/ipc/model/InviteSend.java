package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class InviteSend extends  SendModel implements Parcelable {


    public static final  int  Unknown=0;

    public static final  int  Video=1;

    public static final  int  AudioReceive=2;//接收音频 (语音)

    public static final  int  AudioSend=3; //发送音频 (对讲)

    /**
     * 缺省值设定为1 ,0: Unknown; 1:Video; 2:Audio_S; 手机侧接收音频 3:Audio_R; 手机侧发送音频 4:Video+Audio
     */
    @SerializedName("OperateType")
    private int type;

    @SerializedName("RTPport")
    private int port; //手机侧发送音频的数据端口

    @SerializedName("IsEncryptConn")
    private int isEncryptConn; //是否使用流加密传输, 默认值为0



    public static final Parcelable.Creator<InviteSend> CREATOR = new Parcelable.Creator<InviteSend>() {
        @Override
        public InviteSend createFromParcel(Parcel in) {
            InviteSend model = new InviteSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.type = in.readInt();
            model.channel = in.readInt();
            model.port = in.readInt();
            model.isEncryptConn = in.readInt();

            return model;
        }

        @Override
        public InviteSend[] newArray(int size) {
            return new InviteSend[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getIsEncryptConn() {
        return isEncryptConn;
    }

    public void setIsEncryptConn(int isEncryptConn) {
        this.isEncryptConn = isEncryptConn;
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
        parcel.writeInt(type);
        parcel.writeInt(channel);
        parcel.writeInt(port);
        parcel.writeInt(isEncryptConn);

    }
}