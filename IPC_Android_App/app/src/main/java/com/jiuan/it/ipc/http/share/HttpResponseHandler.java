package com.jiuan.it.ipc.http.share;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.orhanobut.logger.Logger;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpResponseHandler extends JsonHttpResponseHandler {

    // 成功
    private static final int SUCCESS = 1;

    // 失败
    private static final int FAILURE = 2;

    // 错误
    private static final int ERROR = 3;

    // 成功
    public static final String SUCCESS_1000 = "1000";

    // 服务器内部错误
    public static final String ERROR_5000 = "5000";

    @Override
    public void onStart() {
        onCustomStart();
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String content = response.toString();
        Response oaResponse = gson.fromJson(content, Response.class);
        Logger.json(content);

        switch (oaResponse.getResult()) {
            case SUCCESS:
                if (oaResponse.getResultMessage().equals(SUCCESS_1000)) {
                    onCustomSuccess(oaResponse.getReturnValue());
                }
                break;
            case FAILURE:
                onCustomFailure(oaResponse.getResultMessage());
                onCustomFailure(oaResponse.getResultMessage(), oaResponse.getReturnValue());
                break;
            case ERROR:
                if (oaResponse.getResultMessage().equals(ERROR_5000)) {
                    //onCustomFailure(ERROR_5000, null);
                    onCustomError();
                }
                break;
        }
        onCustomFinish();
    }

    /**
     * 请求开始
     */
    public void onCustomStart() {

    }

    /**
     * 请求成功
     *
     * @param value 返回内容
     */
    public void onCustomSuccess(JsonElement value) {

    }

    public void onCustomFailure(String msg) {

    }

    /**
     * 当返回错误时,需要对相关错误的内容进行处理时,会调用此方法
     *
     * @param msg         错误信息
     * @param serverValue 服务器错误内容
     */
    public void onCustomFailure(String msg, JsonElement serverValue) {

    }

    public void onCustomError() {

    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        super.onFailure(statusCode, headers, responseString, throwable);
        onCustomExceptionFinish();
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);
        onCustomExceptionFinish();
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);
        onCustomExceptionFinish();
    }

    /**
     * 正常结束会调用此方法
     */
    public void onCustomFinish() {

    }

    /**
     * 异常结束会调用此方法
     * 注意:如果重写onFailure相关方法时,请在方法结束处调用此方法.
     */
    public void onCustomExceptionFinish() {

    }

    /**
     * 把Json格式的字符串转换成实体类型的方法.
     * 注意:实体类中的变量需要用@SerializedName注释
     */
    public final <T> T get(String value, Class<T> classOfT) {
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            return gson.fromJson(value, classOfT);
        } catch (JsonSyntaxException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
