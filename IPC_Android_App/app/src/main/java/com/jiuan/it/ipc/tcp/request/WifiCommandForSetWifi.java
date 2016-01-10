package com.jiuan.it.ipc.tcp.request;

/**
 * Created by Administrator on 2015/11/26.
 */
public class WifiCommandForSetWifi {

    private String orderID;

    private String rn;

    private ReqContentForSetWifi reqContentForSetWifi;

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

    public ReqContentForSetWifi getReqContentForSetWifi() {
        return reqContentForSetWifi;
    }

    public void setReqContentForSetWifi(ReqContentForSetWifi reqContentForSetWifi) {
        this.reqContentForSetWifi = reqContentForSetWifi;
    }
}
