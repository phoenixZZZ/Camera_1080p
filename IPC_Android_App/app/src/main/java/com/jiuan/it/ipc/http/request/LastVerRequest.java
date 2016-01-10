package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class LastVerRequest extends BaseRequest {

    //public static final String PATH = BaseRequest.PATH + "api/last_ver";
    public static final String PATH = "http://andonacademy.chinacloudapp.cn:8015/api/last_ver";

    private String un;

    private String accessToken;

    private String cateSN;

    private String ver;

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

    public String getCateSN() {
        return cateSN;
    }

    public void setCateSN(String cateSN) {
        this.cateSN = cateSN;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        LastVerRequestBody body = new LastVerRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setCateSN(cateSN);
        body.setVer(ver);
        return body;
    }

    private class LastVerRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("CateSN")
        private String cateSN;

        @SerializedName("Ver")
        private String ver;

        private LastVerRequestBody() {
            setSv(BaseRequestConfig.SV_LAST_VER);
            setSc("001cfe2fe7044aa691d4e6eff9bfb56c");
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

        public String getCateSN() {
            return cateSN;
        }

        public void setCateSN(String cateSN) {
            this.cateSN = cateSN;
        }

        public String getVer() {
            return ver;
        }

        public void setVer(String ver) {
            this.ver = ver;
        }
    }
}
