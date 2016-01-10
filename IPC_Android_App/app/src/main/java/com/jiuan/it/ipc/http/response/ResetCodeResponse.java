package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class ResetCodeResponse {

    @SerializedName("HGUID")
    private String id;

    @SerializedName("Token")
    private Token token;

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


}
