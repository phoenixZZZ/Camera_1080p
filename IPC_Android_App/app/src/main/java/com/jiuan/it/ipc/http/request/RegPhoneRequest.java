package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class RegPhoneRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/reg_phone";

    private String phone;

    private String code;

    private String password;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        RegPhoneRequestBody body = new RegPhoneRequestBody();
        body.setPhone(phone);
        body.setCode(code);
        body.setPassword(password);
        return body;
    }

    private class RegPhoneRequestBody extends BaseRequestBody {

        @SerializedName("Phone")
        private String phone;

        @SerializedName("Code")
        private String code;

        @SerializedName("Password")
        private String password;

        private RegPhoneRequestBody() {
            setSv(BaseRequestConfig.SV_REG_PHONE);
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
