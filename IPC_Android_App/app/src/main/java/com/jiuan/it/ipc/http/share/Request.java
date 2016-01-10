package com.jiuan.it.ipc.http.share;

import com.loopj.android.http.RequestParams;

public abstract class Request {

    private static final String CONTENT = "content";

    public abstract String getContent();

    public RequestParams getRequestParams() {
        RequestParams params = new RequestParams();
        params.put(CONTENT, getContent());
        return params;
    }

}
