package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class EqInfo {
    @SerializedName("EquipCode")
    private String equipCode;

    @SerializedName("Name")
    private String name;

    @SerializedName("State")
    private Integer state;

    @SerializedName("Avtar")
    private String avtar;

    @SerializedName("Manufacturer")
    private String manufacturer;

    @SerializedName("HVersion")
    private Long hVersion;

    @SerializedName("HInfo")
    private String hInfo;

    @SerializedName("IsAdded")
    private Integer isAdded;

    @SerializedName("WBoxID")
    private String wBoxID;

    public String getEquipCode() {
        return equipCode;
    }

    public void setEquipCode(String equipCode) {
        this.equipCode = equipCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getAvtar() {
        return avtar;
    }

    public void setAvtar(String avtar) {
        this.avtar = avtar;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Long gethVersion() {
        return hVersion;
    }

    public void sethVersion(Long hVersion) {
        this.hVersion = hVersion;
    }

    public String gethInfo() {
        return hInfo;
    }

    public void sethInfo(String hInfo) {
        this.hInfo = hInfo;
    }

    public Integer getIsAdded() {
        return isAdded;
    }

    public void setIsAdded(Integer isAdded) {
        this.isAdded = isAdded;
    }

    public String getwBoxID() {
        return wBoxID;
    }

    public void setwBoxID(String wBoxID) {
        this.wBoxID = wBoxID;
    }
}
