package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class TypeCodesRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "camera/TypeCodes";

    private String un;

    private String accessToken;

    private String deviceID;

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

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }


    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        TypeCodesRequestBody body = new TypeCodesRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setDeviceID(deviceID);
        return body;
    }

    private class TypeCodesRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("DeviceID")
        private String deviceID;

        private TypeCodesRequestBody() {
            setSv(BaseRequestConfig.SV_TYPE_CODE);
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

        public String getDeviceID() {
            return deviceID;
        }

        public void setDeviceID(String deviceID) {
            this.deviceID = deviceID;
        }

    }
}
