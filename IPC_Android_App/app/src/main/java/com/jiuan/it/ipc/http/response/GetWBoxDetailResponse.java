package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class GetWBoxDetailResponse {

    @SerializedName("WbName")
    private String wbName;

    @SerializedName("Location")
    private String location;

    @SerializedName("EquipSet")
    private EquipSet[] equipSet;

    public String getWbName() {
        return wbName;
    }

    public void setWbName(String wbName) {
        this.wbName = wbName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EquipSet[] getEquipSet() {
        return equipSet;
    }

    public void setEquipSet(EquipSet[] equipSet) {
        this.equipSet = equipSet;
    }
}
