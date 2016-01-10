package com.jiuan.it.ipc.http.share;

import com.google.gson.annotations.SerializedName;

public abstract class RequestBody {

    @SerializedName("SC")
    private String sc;

    @SerializedName("SV")
    private String sv;

    public String getSc() {
        return sc;
    }

    public void setSc(String sc) {
        this.sc = sc;
    }

    public String getSv() {
        return sv;
    }

    public void setSv(String sv) {
        this.sv = sv;
    }

}
