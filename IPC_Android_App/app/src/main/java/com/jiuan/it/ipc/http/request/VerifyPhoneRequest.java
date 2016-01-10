package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class VerifyPhoneRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/verify_phone";

    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        VerifyPhoneRequestBody body = new VerifyPhoneRequestBody();
        body.setPhone(phone);
        return body;
    }

    private class VerifyPhoneRequestBody extends BaseRequestBody {

        @SerializedName("Phone")
        private String phone;

        private VerifyPhoneRequestBody() {
            setSv(BaseRequestConfig.SV_VERIFY_PHONE);
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
