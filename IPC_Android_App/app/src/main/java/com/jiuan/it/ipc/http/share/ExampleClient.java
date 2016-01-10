package com.jiuan.it.ipc.http.share;

import android.content.Context;

import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

public enum ExampleClient {

    INSTANCE;

    private static final String CONTENT = "content";
    /**
     * 上传文件
     *
     * @param context             上下文
     * @param file                文件
     * @param uid                 服务调用者
     * @param shareType           分享视频的类别
     * @param uploadToken         上传票据
     * @param desc                分享描述信息
     * @param param               服务器参数 例如 image audio video 等
     * @param responseHandler 响应处理
     * @param listener            网络异常监听
     */
    public void requestUploadFile(Context context, File file, String uid, int shareType, String uploadToken,String desc,
                                  String param, ResponseHandler responseHandler, Client.NetworkUnavailableListener listener) {
        ShareRequest share = new ShareRequest();
        share.setUid(uid);
        share.setShareType(shareType);
        share.setUploadToken(uploadToken);
        share.setDesc(desc);
        RequestParams params = new RequestParams();
        params.put(CONTENT, share.getContent());
        try {
            params.put(param, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Client.INSTANCE.request(context, params, ShareRequest.PATH, responseHandler, listener);
    }

    /**
     * 取消请求
     *
     * @param context 上下文
     */
    public void cancelRequests(Context context) {
        Client.INSTANCE.cancelRequests(context, true);
    }
}
