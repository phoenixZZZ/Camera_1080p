package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PreviewSendModel implements Parcelable {

    @SerializedName("C2IDCP")
    private String idcp;

    @SerializedName("Header")
    private String header;

    @SerializedName("Target")
    private String[] target;

    @SerializedName("CSeq")
    private int mark;

    @SerializedName("Server")
    private String[] server;

    @SerializedName("Client")
    private String[] client;

    @SerializedName("ChannelID")
    private int channel;

    @SerializedName("PreviewCtrlType")
    private int ctrlType;

    @SerializedName("PreviewCtrlBase")
    private String ctrlBase;

    @SerializedName("PreviewCtrlName")
    private String ctrlName;

    @SerializedName("PreviewCtrlNameStart")
    private String ctrlNameStart;

    @SerializedName("PreviewCtrlNameEnd")
    private String ctrlNameEnd;

    @SerializedName("ClientFtp")
    private String[] ftp;


    public static final Creator<PreviewSendModel> CREATOR = new Creator<PreviewSendModel>() {
        @Override
        public PreviewSendModel createFromParcel(Parcel in) {
            PreviewSendModel model = new PreviewSendModel();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.ctrlType = in.readInt();
            model.ctrlBase = in.readString();
            model.ctrlName = in.readString();
            model.ctrlNameStart = in.readString();
            model.ctrlNameEnd = in.readString();
            model.ftp = in.createStringArray();
           // model.udp = in.createStringArray();
            return model;
        }

        @Override
        public PreviewSendModel[] newArray(int size) {
            return new PreviewSendModel[size];
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

    public int getCtrlType() {
        return ctrlType;
    }

    public void setCtrlType(int ctrlType) {
        this.ctrlType = ctrlType;
    }

    public String getCtrlBase() {
        return ctrlBase;
    }

    public void setCtrlBase(String ctrlBase) {
        this.ctrlBase = ctrlBase;
    }

    public String getCtrlName() {
        return ctrlName;
    }

    public void setCtrlName(String ctrlName) {
        this.ctrlName = ctrlName;
    }

    public String[] getFtp() {
        return ftp;
    }

    public void setFtp(String[] ftp) {
        this.ftp = ftp;
    }

    public String getCtrlNameStart() {
        return ctrlNameStart;
    }

    public void setCtrlNameStart(String ctrlNameStart) {
        this.ctrlNameStart = ctrlNameStart;
    }

    public String getCtrlNameEnd() {
        return ctrlNameEnd;
    }

    public void setCtrlNameEnd(String ctrlNameEnd) {
        this.ctrlNameEnd = ctrlNameEnd;
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
        parcel.writeInt(ctrlType);
        parcel.writeString(ctrlBase);
        parcel.writeString(ctrlName);
        parcel.writeString(ctrlNameStart);
        parcel.writeString(ctrlNameEnd);
        parcel.writeStringArray(ftp);

    }
}


