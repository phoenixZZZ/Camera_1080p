package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class RtspModel implements Parcelable {

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

    @SerializedName("OperateType")
    private int type;

    @SerializedName("ChannelID")
    private int channel;

    @SerializedName("RTPport")
    private int port;

    @SerializedName("IsEncryptConn")
    private int isEncryptConn;

    @SerializedName("RevMsgTYPE")
    private String msgType;

    @SerializedName("Url")
    private String url;

    @SerializedName("Description")
    private String description;

    @SerializedName("PTZCommandType")
    private int commandType;

    @SerializedName("MediaCodecType")
    private int mediaType;

    @SerializedName("StreamCtrlType")
    private int streamType;

    @SerializedName("LEDCtrlType")
    private int ledType;

    @SerializedName("AlarmCtrlType")
    private int alarmType;

    @SerializedName("PTZCommandStep")
    private int commandStep;

    @SerializedName("PTZCommandAngle")
    private int commandAngle;

    @SerializedName("PanoramicCtrlType")
    private int panoramicCtrlType;

    @SerializedName("PanoramicCtrlNum")
    private int panoramicCtrlNum;

    @SerializedName("PreviewCtrlType")
    private int ctrlType;

    @SerializedName("PreviewCtrlBase")
    private String ctrlBase;

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

    @SerializedName("HaveName")
    private int reqType;

    @SerializedName("NameString")
    private String[] fileName;

    @SerializedName("ClientFtp")
    private String[] ftp;

    @SerializedName("ClientUdp")
    private String[] udp;


    public static final Creator<RtspModel> CREATOR = new Creator<RtspModel>() {
        @Override
        public RtspModel createFromParcel(Parcel in) {
            RtspModel model = new RtspModel();
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
            model.msgType = in.readString();
            model.url = in.readString();
            model.description = in.readString();
            model.commandType = in.readInt();
            model.mediaType = in.readInt();
            model.streamType = in.readInt();
            model.ledType = in.readInt();
            model.alarmType = in.readInt();
            model.commandStep = in.readInt();
            model.commandAngle = in.readInt();
            model.panoramicCtrlType = in.readInt();
            model.panoramicCtrlNum = in.readInt();
            model.ctrlType = in.readInt();
            model.ctrlBase = in.readString();
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
        public RtspModel[] newArray(int size) {
            return new RtspModel[size];
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

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }

    public int getLedType() {
        return ledType;
    }

    public void setLedType(int ledType) {
        this.ledType = ledType;
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
        parcel.writeStringArray(target);
        parcel.writeInt(mark);
        parcel.writeStringArray(server);
        parcel.writeStringArray(client);
        parcel.writeInt(type);
        parcel.writeInt(channel);
        parcel.writeInt(port);
        parcel.writeInt(isEncryptConn);
        parcel.writeString(msgType);
        parcel.writeString(url);
        parcel.writeString(description);
        parcel.writeInt(commandType);
        parcel.writeInt(mediaType);
        parcel.writeInt(streamType);
        parcel.writeInt(ledType);
        parcel.writeInt(alarmType);
        parcel.writeInt(commandStep);
        parcel.writeInt(commandAngle);
        parcel.writeInt(panoramicCtrlType);
        parcel.writeInt(panoramicCtrlNum);
        parcel.writeInt(ctrlType);
        parcel.writeString(ctrlBase);
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


