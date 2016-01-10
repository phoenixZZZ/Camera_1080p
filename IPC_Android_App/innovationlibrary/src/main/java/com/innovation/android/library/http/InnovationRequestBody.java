package com.innovation.android.library.http;

import com.google.gson.annotations.SerializedName;

public class InnovationRequestBody {

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
