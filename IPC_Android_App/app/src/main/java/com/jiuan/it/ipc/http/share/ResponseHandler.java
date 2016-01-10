package com.jiuan.it.ipc.http.share;

import android.util.Log;

public class ResponseHandler extends HttpResponseHandler {


    // 请求缺少必须参数
    private static final String FAILURE_3002 = "3002";

    // 接口未授权
    private static final String FAILURE_2007 = "2007";

    // 没有找到文件
    private static final String FAILURE_3007 = "3007";

    // 上传失败
    private static final String FAILURE_400 = "400";

    // 请求的资源不适用
    private static final String FAILURE_3005 = "3005";

    // 操作票据过期
    private static final String FAILURE_3012 = "3012";
    @Override
    public void onCustomFailure(String msg) {
        if(msg.equals(FAILURE_3002)) {
            onFailure("请求缺少必须参数");
        } else if (msg.equals(FAILURE_2007)) {
            onFailure("接口未授权");
        } else if (msg.equals(FAILURE_3007)) {
            onFailure("没有找到文件");
        } else if (msg.equals(FAILURE_400)) {
            onFailure("上传失败");
        } else if (msg.equals(FAILURE_3005)) {
            onFailure("请求的资源不适用");
        } else if (msg.equals(FAILURE_3012)) {
            onFailure("操作票据过期");
        }

    }

    @Override
    public void onCustomError() {
        onFailure("服务器内部错误");//服务器内部错误
    }

    @Override
    public void onCustomExceptionFinish() {
        onFailure("网络连接超时");
    }

    /**
     * 普通请求失败
     * @param value 失败信息
     */
    public void onFailure(String value) {
        Log.d("onFailure", value);
    }

    /**
     * 令牌相关请求失败
     * @param value 失败信息
     */
    public void onTokenFailure(String value) {
        Log.d("onTokenFailure",value);
    }


}
