package com.jiuan.it.ipc.tcp;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2015/11/26.
 */
public class BaseResponse {

    @SerializedName("Type")
    private String type;

    @SerializedName("TS")
    private String ts;

    @SerializedName("RespCode")
    private String respCode;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }
}
