package com.jiuan.it.ipc.http.share;

import com.google.gson.annotations.SerializedName;

public class ShareRequest extends BaseRequest {

    public static final String PATH = BaseRequest.SHARE_PATH + "UploadSingleVideo";

    private String uid; //服务调用者（分享用户的唯一标识）

    private int shareType; //分享视频的类别

    private String uploadToken; //由客户端生成的上传票据，该参数将会在上传成功后返回给客户端，主要用户视频上传操作的校验

    private String desc; //分享描述信息,将会显示在网页上

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getShareType() {
        return shareType;
    }

    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    public String getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(String uploadToken) {
        this.uploadToken = uploadToken;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    protected RequestBody getRequestBody() {
        ShareRequestBody body = new ShareRequestBody();
        body.setUid(uid);
        body.setShareType(shareType);
        body.setUploadToken(uploadToken);
        body.setDesc(desc);
        return body;
    }

    private class ShareRequestBody extends BaseRequestBody {

        @SerializedName("UID")
        private String uid;

        @SerializedName("ShareType")
        private int shareType;

        @SerializedName("UploadToken")
        private String uploadToken;

        @SerializedName("Desc")
        private String desc;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public int getShareType() {
            return shareType;
        }

        public void setShareType(int shareType) {
            this.shareType = shareType;
        }

        public String getUploadToken() {
            return uploadToken;
        }

        public void setUploadToken(String uploadToken) {
            this.uploadToken = uploadToken;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        private ShareRequestBody() {
            setSv(BaseRequestConfig.SV);
        }


    }
}

