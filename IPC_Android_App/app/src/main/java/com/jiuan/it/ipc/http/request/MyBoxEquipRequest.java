package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class MyBoxEquipRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/My_BoxEquip";

    private String un;

    private String accessToken;

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

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        MyBoxEquipRequestBody body = new MyBoxEquipRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        return body;
    }

    private class MyBoxEquipRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        private MyBoxEquipRequestBody() {
            setSv(BaseRequestConfig.SV_MY_BOXEQUIP);
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

    }
}
