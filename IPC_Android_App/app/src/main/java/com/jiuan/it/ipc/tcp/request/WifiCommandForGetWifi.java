package com.jiuan.it.ipc.tcp.request;

/**
 * Created by Administrator on 2015/11/26.
 */
public class WifiCommandForGetWifi {

    private String orderID;

    private String rn;

    private ReqContentForGetWifi reqContentForGetWifi;

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

    public ReqContentForGetWifi getReqContentForGetWifi() {
        return reqContentForGetWifi;
    }

    public void setReqContentForGetWifi(ReqContentForGetWifi reqContentForGetWifi) {
        this.reqContentForGetWifi = reqContentForGetWifi;
    }
}
