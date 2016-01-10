package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class GetOrderTokenRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/Get_OrderToken";

    private String un;

    private String accessToken;

    private String eqID;

    private String wBoxID;

    private String circleID;


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

    public String getCircleID() {

        return circleID;
    }

    public void setCircleID(String circleID) {
        this.circleID = circleID;
    }

    public String getEqID() {
        return eqID;
    }

    public void setEqID(String eqID) {
        this.eqID = eqID;
    }

    public String getwBoxID() {
        return wBoxID;
    }

    public void setwBoxID(String wBoxID) {
        this.wBoxID = wBoxID;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        GetOrderTokenRequestBody body = new GetOrderTokenRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setEqID(eqID);
        body.setwBoxID(wBoxID);
        body.setCircleID(circleID);
        return body;
    }

    private class GetOrderTokenRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("EqID")
        private String eqID;

        @SerializedName("WBoxID")
        private String wBoxID;

        @SerializedName("CircleID")
        private String circleID;

        private GetOrderTokenRequestBody() {
            setSv(BaseRequestConfig.SV_GET_ORDERTOKEN);
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

        public String getCircleID() {
            return circleID;
        }

        public void setCircleID(String circleID) {
            this.circleID = circleID;
        }

        public String getEqID() {
            return eqID;
        }

        public void setEqID(String eqID) {
            this.eqID = eqID;
        }

        public String getwBoxID() {
            return wBoxID;
        }

        public void setwBoxID(String wBoxID) {
            this.wBoxID = wBoxID;
        }
    }
}
