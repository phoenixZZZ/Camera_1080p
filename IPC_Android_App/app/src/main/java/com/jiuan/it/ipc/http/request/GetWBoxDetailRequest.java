package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class GetWBoxDetailRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/Get_WBoxDetail";

    private String un;

    private String accessToken;

    private String wbCode;

    public String getUn() {
        return un;
    }

    public void setUn(String un) {
        this.un = un;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getWbCode() {
        return wbCode;
    }

    public void setWbCode(String wbCode) {
        this.wbCode = wbCode;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        BindWBoxUserRequestBody body = new BindWBoxUserRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setWbCode(wbCode);
        return body;
    }

    private class BindWBoxUserRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("WBCode")
        private String wbCode;

        private BindWBoxUserRequestBody() {
            setSv(BaseRequestConfig.SV_GET_WBOXDETAIL);
        }

        public String getUn() {
            return un;
        }

        public void setUn(String un) {
            this.un = un;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getWbCode() {
            return wbCode;
        }

        public void setWbCode(String wbCode) {
            this.wbCode = wbCode;
        }
    }
}
