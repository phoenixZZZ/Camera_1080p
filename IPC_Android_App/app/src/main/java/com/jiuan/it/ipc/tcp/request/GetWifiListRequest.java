package com.jiuan.it.ipc.tcp.request;

import com.jiuan.it.ipc.tcp.BaseRequest;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2015/10/8.
 */
public class GetWifiListRequest extends BaseRequest {

    private static final String ROUTE_TYPE = "Wifi";

    // 白盒ID
    @SerializedName("WBID")
    private String wbID;

    // 手机ID
    @SerializedName("PhoneID")
    private String phoneID;

    // Wifi指令详细内容
    @SerializedName("WifiCommand")
    private WifiCommandForGetWifi wifiCommand;

    public GetWifiListRequest() {
        super.setType(ROUTE_TYPE);
        super.setTs(System.currentTimeMillis());
    }

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

    @Override
    public String toJson() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\"Type\":\"");
        sb.append(super.getType());
        sb.append("\",");

        sb.append("\"TS\":");
        sb.append(super.getTs());
        sb.append(",");

        sb.append("\"WBID\":\"");
        sb.append(getWbID());
        sb.append("\",");

        sb.append("\"PhoneID\":\"");
        sb.append(getPhoneID());
        sb.append("\",");

        sb.append("\"WifiCommand\":{");
        sb.append("\"OrderID\":\"");
        sb.append(getWifiCommand().getOrderID());
        sb.append("\",");
        sb.append("\"RN\":\"");
        sb.append(getWifiCommand().getRn());
        sb.append("\",");
        sb.append("\"ReqContent\":{");
        sb.append("\"Order\":\"");
        sb.append(getWifiCommand().getReqContentForGetWifi().getOrder());
        sb.append("\"}}}");

        return sb.toString();
    }
}
