package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class GetLoginCodeRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/get_login_code";

    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        GetLoginCodeRequestBody body = new GetLoginCodeRequestBody();
        body.setPhone(phone);
        return body;
    }

    private class GetLoginCodeRequestBody extends BaseRequestBody {

        @SerializedName("Phone")
        private String phone;

        private GetLoginCodeRequestBody() {
            setSv(BaseRequestConfig.SV_GET_LOGIN_CODE);
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

    }
}
