package com.jiuan.it.ipc.http.response;

import com.google.gson.annotations.SerializedName;

public class UpdateWhiteBoxResponse {

    @SerializedName("IsSuccess")
    private Integer isSuccess;

    @SerializedName("Error")
    private String error;

    public Integer getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(Integer isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getErrors() {
        return error;
    }

    public void setErrors(String error) {
        this.error = error;
    }
}
