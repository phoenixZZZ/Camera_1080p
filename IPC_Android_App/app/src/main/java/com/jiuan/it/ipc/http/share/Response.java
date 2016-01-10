package com.jiuan.it.ipc.http.share;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("Result")
    private int result;

    @SerializedName("ResultMessage")
    private String resultMessage;

    @SerializedName("ReturnValue")
    private JsonElement returnValue;

    @SerializedName("TS")
    private long timestamp;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public JsonElement getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(JsonElement returnValue) {
        this.returnValue = returnValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
