package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class RefreshTokenRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/refresh_token";

    private String un;

    private String refreshToken;

    public String getUn() {
        return un;
    }

    public void setUn(String un) {
        this.un = un;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        RefreshTokenRequestBody body = new RefreshTokenRequestBody();
        body.setUn(un);
        body.setRefreshToken(refreshToken);
        return body;
    }

    private class RefreshTokenRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("RefreshToken")
        private String refreshToken;

        private RefreshTokenRequestBody() {
            setSv(BaseRequestConfig.SV_REFRESH_TOKEN);
        }

        public String getUn() {
            return un;
        }

        public void setUn(String un) {
            this.un = un;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
