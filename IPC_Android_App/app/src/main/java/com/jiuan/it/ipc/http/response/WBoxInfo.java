package com.jiuan.it.ipc.http.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class WBoxInfo implements Parcelable {
    @SerializedName("WBoxCode")
    private String wBoxCode;

    @SerializedName("Name")
    private String name;

    @SerializedName("State")
    private Integer state;

    @SerializedName("Avtar")
    private String avtar;

    @SerializedName("LanIP")
    private String lanIP;

    @SerializedName("Location")
    private String location;

    @SerializedName("Mac")
    private String mac;

    @SerializedName("Manufacturer")
    private String manufacturer;

    @SerializedName("HVersion")
    private Long hVersion;

    @SerializedName("HInfo")
    private String hInfo;

    @SerializedName("FInfo")
    private String fInfo;

    public String getwBoxCode() {
        return wBoxCode;
    }

    public void setwBoxCode(String wBoxCode) {
        this.wBoxCode = wBoxCode;
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

    public String getLanIP() {
        return lanIP;
    }

    public void setLanIP(String lanIP) {
        this.lanIP = lanIP;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
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

    public String getfInfo() {
        return fInfo;
    }

    public void setfInfo(String fInfo) {
        this.fInfo = fInfo;
    }

    public static final Creator<WBoxInfo> CREATOR = new Creator<WBoxInfo>() {

        public WBoxInfo createFromParcel(Parcel source) {

            WBoxInfo wBoxInfo = new WBoxInfo();

            wBoxInfo.wBoxCode = source.readString();
            wBoxInfo.name = source.readString();
            wBoxInfo.state = source.readInt();
            wBoxInfo.avtar = source.readString();
            wBoxInfo.lanIP = source.readString();
            wBoxInfo.location = source.readString();
            wBoxInfo.mac = source.readString();
            wBoxInfo.manufacturer = source.readString();
            wBoxInfo.hVersion = source.readLong();
            wBoxInfo.hInfo = source.readString();
            wBoxInfo.fInfo = source.readString();
            return wBoxInfo;
        }

        public WBoxInfo[] newArray(int size) {
            return new WBoxInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(wBoxCode);
        dest.writeString(name);
        dest.writeInt(state);
        dest.writeString(avtar);
        dest.writeString(lanIP);
        dest.writeString(location);
        dest.writeString(mac);
        dest.writeString(manufacturer);
        dest.writeLong(hVersion);
        dest.writeString(hInfo);
        dest.writeString(fInfo);
    }
}
