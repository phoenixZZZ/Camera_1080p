package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class CodeLoginRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/code_login";

    private String phone;

    private String code;

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

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        CodeLoginRequestBody body = new CodeLoginRequestBody();
        body.setPhone(phone);
        body.setCode(code);
        return body;
    }

    private class CodeLoginRequestBody extends BaseRequestBody {

        @SerializedName("Phone")
        private String phone;

        @SerializedName("Code")
        private String code;

        private CodeLoginRequestBody() {
            setSv(BaseRequestConfig.SV_CODE_LOGIN);
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
    }
}
