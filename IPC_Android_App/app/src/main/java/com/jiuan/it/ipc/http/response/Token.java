package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("AccessToken")
    private String accessToken;

    @SerializedName("AccessExpire")
    private long accessExpire;

    @SerializedName("RefreshToken")
    private String refreshToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getAccessExpire() {
        return accessExpire;
    }

    public void setAccessExpire(long accessExpire) {
        this.accessExpire = accessExpire;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
