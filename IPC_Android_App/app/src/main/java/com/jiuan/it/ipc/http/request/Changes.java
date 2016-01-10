package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequestBody;
import com.google.gson.annotations.SerializedName;

public class Changes extends BaseRequestBody {

    @SerializedName("Field")
    private String field;

    @SerializedName("Value")
    private String value;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Changes() {

    }

    public Changes(String field, String value) {
        this.field = field;
        this.value = value;
    }
}
