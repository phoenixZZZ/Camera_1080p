package com.jiuan.it.ipc.tcp.response;

import com.google.gson.annotations.SerializedName;
import com.jiuan.it.ipc.tcp.BaseResponse;

/**
 * Created by Administrator on 2015/11/26.
 */
public class GetWifiListResponse extends BaseResponse {

    // 白盒ID
    @SerializedName("WBID")
    private String wbID;

    // 手机ID
    @SerializedName("PhoneID")
    private String phoneID;

    // Wifi指令详细内容
    @SerializedName("WifiCommand")
    private WifiCommandForGetWifi wifiCommand;

    public String getWbID() {
        return wbID;
    }

    public void setWbID(String wbID) {
        this.wbID = wbID;
    }

    public String getPhoneID() {
        return phoneID;
    }

    public void setPhoneID(String phoneID) {
        this.phoneID = phoneID;
    }

    public WifiCommandForGetWifi getWifiCommand() {
        return wifiCommand;
    }

    public void setWifiCommand(WifiCommandForGetWifi wifiCommand) {
        this.wifiCommand = wifiCommand;
    }
}
