package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class DelWBoxRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/Del_WBox";

    private String un;

    private String accessToken;

    private String wBoxID;

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

    public String getwBoxID() {
        return wBoxID;
    }

    public void setwBoxID(String wBoxID) {
        this.wBoxID = wBoxID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        DelWBoxRequestBody body = new DelWBoxRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setwBoxID(wBoxID);
        body.setPassword(password);
        return body;
    }

    private class DelWBoxRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("WBoxID")
        private String wBoxID;

        @SerializedName("Password")
        private String password;

        private DelWBoxRequestBody() {
            setSv(BaseRequestConfig.SV_DEL_WBOXEQ);
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

        public String getwBoxID() {
            return wBoxID;
        }

        public void setwBoxID(String wBoxID) {
            this.wBoxID = wBoxID;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
