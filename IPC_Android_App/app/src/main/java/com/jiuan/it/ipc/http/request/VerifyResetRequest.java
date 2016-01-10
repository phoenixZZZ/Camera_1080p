package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class VerifyResetRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/verify_reset";

    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        VerifyResetRequestBody body = new VerifyResetRequestBody();
        body.setPhone(phone);
        return body;
    }

    private class VerifyResetRequestBody extends BaseRequestBody {

        @SerializedName("Phone")
        private String phone;

        private VerifyResetRequestBody() {
            setSv(BaseRequestConfig.SV_VERIFY_RESET);
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

    }
}
