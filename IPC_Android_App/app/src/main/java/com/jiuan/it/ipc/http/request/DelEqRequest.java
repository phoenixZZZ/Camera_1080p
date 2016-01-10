package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class DelEqRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/del_eq";

    private String un;

    private String accessToken;

    private String eqID;

    private String password;

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

    public String getEqID() {
        return eqID;
    }

    public void setEqID(String eqID) {
        this.eqID = eqID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        DelEqRequestBody body = new DelEqRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setEqID(eqID);
        body.setPassword(password);
        return body;
    }

    private class DelEqRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("EqID")
        private String eqID;

        @SerializedName("Password")
        private String password;

        private DelEqRequestBody() {
            setSv(BaseRequestConfig.SV_DEL_EQ);
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

        public String getEqID() {
            return eqID;
        }

        public void setEqID(String eqID) {
            this.eqID = eqID;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
