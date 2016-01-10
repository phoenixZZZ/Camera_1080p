package com.jiuan.it.ipc.tcp;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class TcpResponseHandler {

    // 失败-请求缺少参数
    private static final String FAILURE_2001 = "2001";

    // 失败-请求内容或参数错误
    private static final String FAILURE_2002 = "2002";

    // 白盒令牌失效
    private static final String FAILURE_4000 = "4000";


    public void onFailure(String msg) {
        if (TextUtils.equals(msg, FAILURE_2001)) {
            onZNKFailure("失败-请求缺少参数");
        } else if (TextUtils.equals(msg, FAILURE_2002)) {
            onZNKFailure("失败-请求内容或参数错误");
        } else if (TextUtils.equals(msg, FAILURE_4000)) {
            onZNKFailure("白盒令牌失效");
        } else if (TextUtils.equals(msg, "0000")) {
            onZNKFailure("白盒已关闭");
        }
    }

    public void onStart() {

    }

    public void onSuccess(String strJson) {
    }

    /**
     * 正常结束会调用此方法
     */
    public void onFinish() {
    }


    public void onExceptionFinish() {
    }

    /**
     * 普通请求失败
     * @param value 失败信息
     */
    public void onZNKFailure(String value) {
    }

    /**
     * 把Json格式的字符串转换成实体类型的方法.
     * 注意:实体类中的变量需要用@SerializedName注释
     */
    public final <T> T jsonToObject(String value, Class<T> classOfT) {
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            return gson.fromJson(value, classOfT);
        } catch (JsonSyntaxException exception) {
            Log.d("JsonSyntaxException", exception.getMessage());
        }
        return null;
    }

}
