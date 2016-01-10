package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class FileList {

    @SerializedName("Ver")
    private String ver; //固件文件版本号

    @SerializedName("Name")
    private String name;  //文件名

    @SerializedName("Url")
    private String url;  //固件文件下载地址

    @SerializedName("CRC")
    private String CRC;  //CRC校验值

    @SerializedName("Length")
    private Long length;  //固件文件的字节长度数

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCRC() {
        return CRC;
    }

    public void setCRC(String CRC) {
        this.CRC = CRC;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }
}
