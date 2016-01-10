package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class PhoneLoginRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/phone_login";

    private String phone;

    private String password;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        PhoneLoginRequestBody body = new PhoneLoginRequestBody();
        body.setPhone(phone);
        body.setPassword(password);
        return body;
    }

    private class PhoneLoginRequestBody extends BaseRequestBody {

        @SerializedName("Phone")
        private String phone;

        @SerializedName("Password")
        private String password;

        private PhoneLoginRequestBody() {
            setSv(BaseRequestConfig.SV_PHONE_LOGIN);
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
