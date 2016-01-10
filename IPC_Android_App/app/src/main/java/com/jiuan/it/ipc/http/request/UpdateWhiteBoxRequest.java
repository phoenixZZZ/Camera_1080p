package com.jiuan.it.ipc.http.request;

import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;
import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;

public class UpdateWhiteBoxRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "api/Update_WhiteBox";

    private String un;

    private String accessToken;

    private String wBoxCode;

    private Changes[] changes;

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

    public String getwBoxCode() {
        return wBoxCode;
    }

    public void setwBoxCode(String wBoxCode) {
        this.wBoxCode = wBoxCode;
    }

    public Changes[] getChanges() {
        return changes;
    }

    public void setChanges(Changes[] changes) {
        this.changes = changes;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        UpdateWhiteBoxRequestBody body = new UpdateWhiteBoxRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setwBoxCode(wBoxCode);
        body.setChanges(changes);
        return body;
    }

    private class UpdateWhiteBoxRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("WBoxCode")
        private String wBoxCode;

        @SerializedName("Changes")
        private Changes[] changes;

        private UpdateWhiteBoxRequestBody() {
            setSv(BaseRequestConfig.SV_UPDATE_WHITEBOX);
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

        public String getwBoxCode() {
            return wBoxCode;
        }

        public void setwBoxCode(String wBoxCode) {
            this.wBoxCode = wBoxCode;
        }

        public Changes[] getChanges() {
            return changes;
        }

        public void setChanges(Changes[] changes) {
            this.changes = changes;
        }
    }
}
