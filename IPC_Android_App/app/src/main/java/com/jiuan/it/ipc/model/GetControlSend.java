package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 锁定控制（查询）
 */
public class GetControlSend extends  SendModel implements Parcelable {

    public static final Creator<GetControlSend> CREATOR = new Creator<GetControlSend>() {
        @Override
        public GetControlSend createFromParcel(Parcel in) {
            GetControlSend model = new GetControlSend();
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
        public GetControlSend[] newArray(int size) {
            return new GetControlSend[size];
        }
    };

    public String getIdcp() {
        return idcp;
    }

    public void setIdcp(String idcp) {
        this.idcp = idcp;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String[] getTarget() {
        return target;
    }

    public void setTarget(String[] target) {
        this.target = target;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public String[] getServer() {
        return server;
    }

    public void setServer(String[] server) {
        this.server = server;
    }

    public String[] getClient() {
        return client;
    }

    public void setClient(String[] client) {
        this.client = client;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int  channel) {
        this.channel = channel;
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

    }
}