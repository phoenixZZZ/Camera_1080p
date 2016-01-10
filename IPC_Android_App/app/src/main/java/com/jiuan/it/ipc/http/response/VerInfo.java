package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VerInfo {

    @SerializedName("Description")
    private String description; //此版本描述信息

    @SerializedName("UpTime")
    private long upTime;  //固件上传时间

    @SerializedName("FileList")
    private List<FileList> fileList;  //固件文件列表

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUpTime() {
        return upTime;
    }

    public void setUpTime(long upTime) {
        this.upTime = upTime;
    }

    public List<FileList> getFileList() {
        return fileList;
    }

    public void setFileList(List<FileList> fileList) {
        this.fileList = fileList;
    }
}
