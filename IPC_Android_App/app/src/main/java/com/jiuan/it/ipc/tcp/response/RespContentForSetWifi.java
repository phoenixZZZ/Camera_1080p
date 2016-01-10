package com.jiuan.it.ipc.tcp.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2015/11/26.
 */
public class RespContentForSetWifi {

    @SerializedName("RespOrder")
    private String respOrder;

    public String getRespOrder() {
        return respOrder;
    }

    public void setRespOrder(String respOrder) {
        this.respOrder = respOrder;
    }
}
