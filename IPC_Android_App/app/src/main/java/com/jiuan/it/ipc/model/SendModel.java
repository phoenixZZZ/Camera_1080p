package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class SendModel  {

    @SerializedName("C2IDCP")
    protected String idcp;   //1.0

    @SerializedName("Header")
    protected String header; //指令标记

    @SerializedName("Target")
    protected String[] target; //IPC-IPAdress:IPC-Port

    @SerializedName("CSeq")
    protected int mark;  //默认值为1，每个消息都有序号来标记

    @SerializedName("Server")
    protected String[] server;//IPC-IPAdress:IPC-Port

    @SerializedName("Client")
    protected String[] client; //Client-IPAddress:Client-Port

    @SerializedName("ChannelID")
    protected int channel;  //会话ID

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


}