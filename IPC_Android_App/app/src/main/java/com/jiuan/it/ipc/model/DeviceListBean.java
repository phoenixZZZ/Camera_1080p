package com.jiuan.it.ipc.model;

public class DeviceListBean {

    // 设备唯一编号
    private String code;
    // IP地址
    private String lanIP ;
    // 名称
    private String name;
    // 设备类型
    private Integer eqType;
    // 设备所在白盒ID
    private String whiteBoxId;
    // 设备固件版本信息
    private String FInfo;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getEqType() {
        return eqType;
    }

    public void setEqType(Integer eqType) {
        this.eqType = eqType;
    }

    public String getWhiteBoxId() {
        return whiteBoxId;
    }

    public void setWhiteBoxId(String whiteBoxId) {
        this.whiteBoxId = whiteBoxId;
    }

    public String getLanIP() {
        return lanIP;
    }

    public void setLanIP(String lanIP) {
        this.lanIP = lanIP;
    }

    public String getFInfo() {
        return FInfo;
    }

    public void setFInfo(String FInfo) {
        this.FInfo = FInfo;
    }
}
