package com.jiuan.it.ipc.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 2015/12/2.
 */
public class ShareModel {

    @SerializedName("ShareID")
    private String shareID ;
    @SerializedName("ShareUrl")
    private String shareUrl ;

    public String getShareID() {
        return shareID;
    }

    public void setShareID(String shareID) {
        this.shareID = shareID;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }
}
