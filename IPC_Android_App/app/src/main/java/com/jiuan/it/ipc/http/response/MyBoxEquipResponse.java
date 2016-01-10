package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class MyBoxEquipResponse {

    @SerializedName("WBoxInfos")
    private WBoxInfo[] wBoxInfos;

    @SerializedName("EquipInfos")
    private EqInfo[] eqInfos;

    public WBoxInfo[] getwBoxInfos() {
        return wBoxInfos;
    }

    public void setwBoxInfos(WBoxInfo[] wBoxInfos) {
        this.wBoxInfos = wBoxInfos;
    }

    public EqInfo[] getEqInfos() {
        return eqInfos;
    }

    public void setEqInfos(EqInfo[] eqInfos) {
        this.eqInfos = eqInfos;
    }
}
