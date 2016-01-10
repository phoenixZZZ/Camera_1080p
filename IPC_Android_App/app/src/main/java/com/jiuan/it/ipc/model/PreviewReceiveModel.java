package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PreviewReceiveModel implements Parcelable {

    @SerializedName("C2IDCP")
    private String idcp;

    @SerializedName("Header")
    private String header;

    @SerializedName("RevMsgTYPE")
    private String msgType;

    @SerializedName("CSeq")
    private int mark;

    @SerializedName("Server")
    private String[] server;

    @SerializedName("Client")
    private String[] client;

    @SerializedName("OperateType")
    private int type;

    @SerializedName("ChannelID")
    private int channel;

    @SerializedName("Url")
    private String url;

    @SerializedName("Description")
    private String description;

    @SerializedName("ClientUdp")
    private String[] udp;

    @SerializedName("PanoramicCtrlType")
    private int panoramicCtrlType;

    @SerializedName("PanoramicCtrlNum")
    private int panoramicCtrlNum;

    @SerializedName("PreviewCtrlType")
    private int ctrlType;

    @SerializedName("HaveName")
    private int reqType;

    @SerializedName("NameString")
    private String[] fileName;

    @SerializedName("ClientFtp")
    private String[] ftp;

    @SerializedName("PreviewCtrlName")
    private String ctrlName;

    @SerializedName("PreviewCtrlFileName")
    private String ctrlFileName;

    @SerializedName("PreviewCtrlNameStart")
    private String ctrlNameStart;

    @SerializedName("PreviewCtrlNameEnd")
    private String ctrlNameEnd;

    @SerializedName("PreviewCtrlFileLast")
    private int ctrlFileLast;



    public static final Creator<PreviewReceiveModel> CREATOR = new Creator<PreviewReceiveModel>() {
        @Override
        public PreviewReceiveModel createFromParcel(Parcel in) {
            PreviewReceiveModel model = new PreviewReceiveModel();
            model.idcp = in.readString();
            model.header = in.readString();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.type = in.readInt();
            model.channel = in.readInt();
            model.msgType = in.readString();
            model.url = in.readString();
            model.description = in.readString();
            model.panoramicCtrlType = in.readInt();
            model.panoramicCtrlNum = in.readInt();
            model.ctrlType = in.readInt();
            model.ctrlName = in.readString();
            model.ctrlFileName = in.readString();
            model.ctrlNameStart = in.readString();
            model.ctrlNameEnd = in.readString();
            model.ctrlFileLast = in.readInt();
            model.reqType = in.readInt();
            model.fileName = in.createStringArray();
            model.ftp = in.createStringArray();
            model.udp = in.createStringArray();
            return model;
        }

        @Override
        public PreviewReceiveModel[] newArray(int size) {
            return new PreviewReceiveModel[size];
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int  channel) {
        this.channel = channel;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public int getCtrlType() {
        return ctrlType;
    }

    public void setCtrlType(int ctrlType) {
        this.ctrlType = ctrlType;
    }

    public int getPanoramicCtrlType() {
        return panoramicCtrlType;
    }

    public void setPanoramicCtrlType(int panoramicCtrlType) {
        this.panoramicCtrlType = panoramicCtrlType;
    }

    public String getCtrlName() {
        return ctrlName;
    }

    public void setCtrlName(String ctrlName) {
        this.ctrlName = ctrlName;
    }

    public int getReqType() {
        return reqType;
    }

    public void setReqType(int reqType) {
        this.reqType = reqType;
    }

    public String[] getFileName() {
        return fileName;
    }

    public void setFileName(String[] fileName) {
        this.fileName = fileName;
    }

    public String[] getFtp() {
        return ftp;
    }

    public void setFtp(String[] ftp) {
        this.ftp = ftp;
    }

    public String getCtrlFileName() {
        return ctrlFileName;
    }

    public void setCtrlFileName(String ctrlFileName) {
        this.ctrlFileName = ctrlFileName;
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

    public int getCtrlFileLast() {
        return ctrlFileLast;
    }

    public void setCtrlFileLast(int ctrlFileLast) {
        this.ctrlFileLast = ctrlFileLast;
    }

    public int getPanoramicCtrlNum() {
        return panoramicCtrlNum;
    }

    public void setPanoramicCtrlNum(int panoramicCtrlNum) {
        this.panoramicCtrlNum = panoramicCtrlNum;
    }

    public String[] getUdp() {
        return udp;
    }

    public void setUdp(String[] udp) {
        this.udp = udp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(idcp);
        parcel.writeString(header);
        parcel.writeInt(mark);
        parcel.writeStringArray(server);
        parcel.writeStringArray(client);
        parcel.writeInt(type);
        parcel.writeInt(channel);
        parcel.writeString(msgType);
        parcel.writeString(url);
        parcel.writeString(description);
        parcel.writeInt(panoramicCtrlType);
        parcel.writeInt(panoramicCtrlNum);
        parcel.writeInt(ctrlType);
        parcel.writeString(ctrlName);
        parcel.writeString(ctrlFileName);
        parcel.writeString(ctrlNameStart);
        parcel.writeString(ctrlNameEnd);
        parcel.writeInt(ctrlFileLast);
        parcel.writeInt(reqType);
        parcel.writeStringArray(fileName);
        parcel.writeStringArray(ftp);
        parcel.writeStringArray(udp);

    }
}


