package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class UpdateVersionSend extends  SendModel implements Parcelable {

    @SerializedName("CurVersion")
    private String curVersion;

    @SerializedName("UpdateVersion")
    private String updateVersion;

    @SerializedName("strUpdateFileName")
    private String updateFileName;

    @SerializedName("strUpdateFileUrl")
    private String updateFileUrl;

    @SerializedName("CRC")
    private String crc;

    @SerializedName("Length")
    private int length;


    public static final Creator<UpdateVersionSend> CREATOR = new Creator<UpdateVersionSend>() {
        @Override
        public UpdateVersionSend createFromParcel(Parcel in) {
            UpdateVersionSend model = new UpdateVersionSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.curVersion = in.readString();
            model.updateVersion = in.readString();
            model.updateFileName = in.readString();
            model.updateFileUrl = in.readString();
            model.crc = in.readString();
            model.length = in.readInt();

            return model;
        }

        @Override
        public UpdateVersionSend[] newArray(int size) {
            return new UpdateVersionSend[size];
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

    public String getCurVersion() {
        return curVersion;
    }

    public void setCurVersion(String curVersion) {
        this.curVersion = curVersion;
    }

    public String getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(String updateVersion) {
        this.updateVersion = updateVersion;
    }

    public String getUpdateFileName() {
        return updateFileName;
    }

    public void setUpdateFileName(String updateFileName) {
        this.updateFileName = updateFileName;
    }

    public String getUpdateFileUrl() {
        return updateFileUrl;
    }

    public void setUpdateFileUrl(String updateFileUrl) {
        this.updateFileUrl = updateFileUrl;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
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
        parcel.writeString(curVersion);
        parcel.writeString(updateVersion);
        parcel.writeString(updateFileName);
        parcel.writeString(updateFileUrl);
        parcel.writeString(crc);
        parcel.writeInt(length);
    }
}