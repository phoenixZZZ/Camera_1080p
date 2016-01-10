package com.jiuan.it.ipc.http;

import com.innovation.android.library.http.InnovationRequestBody;

public abstract class BaseRequestBody extends InnovationRequestBody {

    public BaseRequestBody() {
        setSc(BaseRequestConfig.SC);
    }

}
