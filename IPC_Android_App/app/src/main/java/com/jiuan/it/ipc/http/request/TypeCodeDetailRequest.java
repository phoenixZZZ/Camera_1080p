package com.jiuan.it.ipc.http.request;

import com.google.gson.annotations.SerializedName;
import com.innovation.android.library.http.InnovationRequestBody;
import com.jiuan.it.ipc.http.BaseRequest;
import com.jiuan.it.ipc.http.BaseRequestBody;
import com.jiuan.it.ipc.http.BaseRequestConfig;

public class TypeCodeDetailRequest extends BaseRequest {

    public static final String PATH = BaseRequest.PATH + "camera/TypeCode_Detail";

    private String un;

    private String accessToken;

    private String deviceID;

    private String typeCode;

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

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    @Override
    protected InnovationRequestBody getInnovationRequestBody() {
        TypeCodeDetailRequestBody body = new TypeCodeDetailRequestBody();
        body.setUn(un);
        body.setAccessToken(accessToken);
        body.setDeviceID(deviceID);
        body.setTypeCode(typeCode);
        return body;
    }

    private class TypeCodeDetailRequestBody extends BaseRequestBody {

        @SerializedName("UN")
        private String un;

        @SerializedName("AccessToken")
        private String accessToken;

        @SerializedName("DeviceID")
        private String deviceID;

        @SerializedName("TypeCode")
        private String typeCode;

        private TypeCodeDetailRequestBody() {
            setSv(BaseRequestConfig.SV_TYPE_CODE_DETAIL);
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

        public String getTypeCode() {
            return typeCode;
        }

        public void setTypeCode(String typeCode) {
            this.typeCode = typeCode;
        }
    }
}
