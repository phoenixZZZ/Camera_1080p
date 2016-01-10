package com.jiuan.it.ipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class PreviewCtrlSend extends  SendModel implements Parcelable {


    @SerializedName("PreviewCtrlType")
    private int ctrlType;

    @SerializedName("PreviewCtrlCount")
    private int count;

    @SerializedName("PreviewCtrlBase")
    private String ctrlBase;

    @SerializedName("PreviewCtrlName")
    private String ctrlName;

    @SerializedName("PreviewCtrlNameStart")
    private String ctrlNameStart;

    @SerializedName("PreviewCtrlNameEnd")
    private String ctrlNameEnd;

    @SerializedName("ClientFtp")
    private String[] ftp;


    public static final Creator<PreviewCtrlSend> CREATOR = new Creator<PreviewCtrlSend>() {
        @Override
        public PreviewCtrlSend createFromParcel(Parcel in) {
            PreviewCtrlSend model = new PreviewCtrlSend();
            model.idcp = in.readString();
            model.header = in.readString();
            model.target = in.createStringArray();
            model.mark = in.readInt();
            model.server = in.createStringArray();
            model.client = in.createStringArray();
            model.channel = in.readInt();
            model.ctrlType = in.readInt();
            model.ctrlBase = in.readString();
            model.ctrlName = in.readString();
            model.ctrlNameStart = in.readString();
            model.ctrlNameEnd = in.readString();
            model.ftp = in.createStringArray();
            model.count = in.readInt();
            return model;
        }

        @Override
        public PreviewCtrlSend[] newArray(int size) {
            return new PreviewCtrlSend[size];
        }
    };


    public int getCtrlType() {
        return ctrlType;
    }

    public void setCtrlType(int ctrlType) {
        this.ctrlType = ctrlType;
    }

    public String getCtrlBase() {
        return ctrlBase;
    }

    public void setCtrlBase(String ctrlBase) {
        this.ctrlBase = ctrlBase;
    }

    public String getCtrlName() {
        return ctrlName;
    }

    public void setCtrlName(String ctrlName) {
        this.ctrlName = ctrlName;
    }

    public String[] getFtp() {
        return ftp;
    }

    public void setFtp(String[] ftp) {
        this.ftp = ftp;
    }

    public String getCtrlNameStart() {
        return ctrlNameStart;
    }

    public void setCtrlNameStart(String ctrlNameStart) {
        this.ctrlNameStart = ctrlNameStart;
    }

    public String getCtrlNameEnd() {
        return ctrlNameEnd;
    }

    public void setCtrlNameEnd(String ctrlNameEnd) {
        this.ctrlNameEnd = ctrlNameEnd;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(idcp);
        parcel.writeString(header);
        parcel.writeStringArray(target);
        parcel.writeInt(mark);
        parcel.writeStringArray(server);
        parcel.writeStringArray(client);
        parcel.writeInt(channel);
        parcel.writeInt(ctrlType);
        parcel.writeString(ctrlBase);
        parcel.writeString(ctrlName);
        parcel.writeString(ctrlNameStart);
        parcel.writeString(ctrlNameEnd);
        parcel.writeStringArray(ftp);
        parcel.writeInt(count);
    }
}


