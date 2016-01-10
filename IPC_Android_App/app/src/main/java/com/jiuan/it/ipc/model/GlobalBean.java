package com.jiuan.it.ipc.model;


import com.jiuan.it.ipc.http.response.Token;

public class GlobalBean {

    // HGUID
    private String hguid;

    // Token
    private Token token;

    // 用户昵称
    private String username;

    // 手机号
    private String phoneNum;

    // 内网白盒Token
    private String whiteBoxToken;


    public String getHguid() {
        return hguid;
    }

    public void setHguid(String hguid) {
        this.hguid = hguid;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getWhiteBoxToken() {
        return whiteBoxToken;
    }

    public void setWhiteBoxToken(String whiteBoxToken) {
        this.whiteBoxToken = whiteBoxToken;
    }
}