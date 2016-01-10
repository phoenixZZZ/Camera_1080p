package com.jiuan.it.ipc.tcp.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2015/11/26.
 */
public class WifiCommandForGetWifi {

    @SerializedName("OrderID")
    private String orderID;

    @SerializedName("RN")
    private String rn;

    @SerializedName("RespContent")
    private RespContentForGetWifi[] respContent;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getRn() {
        return rn;
    }

    public void setRn(String rn) {
        this.rn = rn;
    }

    public RespContentForGetWifi[] getRespContent() {
        return respContent;
    }

    public void setRespContent(RespContentForGetWifi[] respContent) {
        this.respContent = respContent;
    }
}
