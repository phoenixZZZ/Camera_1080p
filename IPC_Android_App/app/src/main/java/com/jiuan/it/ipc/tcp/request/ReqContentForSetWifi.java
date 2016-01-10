package com.jiuan.it.ipc.tcp.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2015/11/26.
 */
public class ReqContentForSetWifi {

    @SerializedName("SecurityMode")
    private String securityMode;

    @SerializedName("EncryptionType")
    private String encryptionType;

    @SerializedName("SSID")
    private String ssid;

    @SerializedName("PassPhrase")
    private String passPhrase;

    @SerializedName("Channel")
    private String channel;

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(String securityMode) {
        this.securityMode = securityMode;
    }

    public String getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
