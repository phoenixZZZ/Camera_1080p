package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class LastVer {

    @SerializedName("LastVer")
    private String lastVer; //最新固件版本号

    @SerializedName("Description")
    private String description;  //此版本描述信息

    @SerializedName("UpTime")
    private long upTime;  //固件上传时间

    @SerializedName("ForceUp")
    private int forceUp;  //是否需要强制升级 0：否 1：是

    public String getLastVer() {
        return lastVer;
    }

    public void setLastVer(String lastVer) {
        this.lastVer = lastVer;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getForceUp() {
        return forceUp;
    }

    public void setForceUp(int forceUp) {
        this.forceUp = forceUp;
    }
}
