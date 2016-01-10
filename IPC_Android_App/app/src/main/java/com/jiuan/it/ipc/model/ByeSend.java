package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ByeSend extends  SendModel implements Parcelable {

    /**
     * 缺省值设定为1 ,0: Unknown; 1:Video; 2:Audio_S; 手机侧接收音频 3:Audio_R; 手机侧发送音频 4:Video+Audio
     */
    @SerializedName("OperateType")
    private int type;

    public static final Creator<ByeSend> CREATOR = new Creator<ByeSend>() {
        @Override
        public ByeSend createFromParcel(Parcel in) {
            ByeSend model = new ByeSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.type = in.readInt();
            model.channel = in.readInt();

            return model;
        }

        @Override
        public ByeSend[] newArray(int size) {
            return new ByeSend[size];
        }
    };


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    }
}