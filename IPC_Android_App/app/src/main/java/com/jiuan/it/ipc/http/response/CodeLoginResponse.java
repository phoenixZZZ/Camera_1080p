package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class CodeLoginResponse {

    @SerializedName("HGUID")
    private String id;

    @SerializedName("Token")
    private Token token;

    @SerializedName("UserName")
    private String userName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
