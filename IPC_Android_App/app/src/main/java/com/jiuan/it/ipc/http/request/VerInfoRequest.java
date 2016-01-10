package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class VerInfoRequest extends BaseRequest {

    //public static final String PATH = BaseRequest.PATH + "api/ver_info";
    public static final String PATH = "http://andonacademy.chinacloudapp.cn:8015/api/ver_info";
    private String un;

    private String accessToken;

    private String cateSN;

    private String hVer;

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

    public String gethVer() {
        return hVer;
    }

    public void sethVer(String hVer) {
        this.hVer = hVer;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        VerInfoRequestBody body = new VerInfoRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setCateSN(cateSN);
        body.sethVer(hVer);
        return body;
    }

    private class VerInfoRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("CateSN")
        private String cateSN;

        @SerializedName("Ver")
        private String hVer;

        private VerInfoRequestBody() {
            setSv(BaseRequestConfig.SV_VER_INFO);
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

        public String gethVer() {
            return hVer;
        }

        public void sethVer(String hVer) {
            this.hVer = hVer;
        }
    }
}
