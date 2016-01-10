package com.jiuan.it.ipc.http.share;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public abstract class RequestImpl extends Request {

    @Override
    public String getContent() {
        RequestBody obj = getRequestBody();
        if (obj == null) {
            return null;
        }
        Gson gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        return gson.toJson(obj);
    }

    protected abstract RequestBody getRequestBody();

}
