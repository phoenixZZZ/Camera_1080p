package com.jiuan.it.ipc.tcp;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Modifier;

/**
 * Created by Administrator on 2015/10/8.
 */
public class BaseRequest {
    // 命令类型
    @SerializedName("Type")
    private String type;

    // 时间戳
    @SerializedName("TS")
    private Long ts;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String toJson() {

        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        return gson.toJson(this);
    }
}
