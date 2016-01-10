package com.jiuan.it.ipc.http.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class EquipSet implements Parcelable {
    @SerializedName("Code")
    private String code;

    @SerializedName("EquipName")
    private String equipName;

    @SerializedName("IsOnline")
    private Integer isOnline;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEquipName() {
        return equipName;
    }

    public void setEquipName(String equipName) {
        this.equipName = equipName;
    }

    public Integer getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Integer isOnline) {
        this.isOnline = isOnline;
    }

    public static final Creator<EquipSet> CREATOR = new Creator<EquipSet>() {

        public EquipSet createFromParcel(Parcel source) {
            EquipSet mEquip = new EquipSet();
            mEquip.code = source.readString();
            mEquip.equipName = source.readString();
            mEquip.isOnline = source.readInt();
            return mEquip;
        }

        public EquipSet[] newArray(int size) {
            return new EquipSet[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(equipName);
        dest.writeInt(isOnline);
    }
}
