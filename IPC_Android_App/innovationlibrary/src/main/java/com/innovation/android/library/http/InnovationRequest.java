package com.innovation.android.library.http;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;

public abstract class InnovationRequest {

    private static final String CONTENT = "content";

    private HeadInfo headInfo;

    public void setHeadInfo(HeadInfo headinfo) {
        this.headInfo = headinfo;
    }

    public abstract String getContent();

    public String getPathWithHeadInfo(String path) {
        RequestParams params = new RequestParams();
        params.put(HeadInfo.AGENT, headInfo.getAgent());
        params.put(HeadInfo.AGENTID, headInfo.getAgentID());
        params.put(HeadInfo.HW, headInfo.getHw());
        params.put(HeadInfo.HWVER, headInfo.getHwVer());
        params.put(HeadInfo.FW, headInfo.getFw());
        params.put(HeadInfo.FWVER, headInfo.getFwVer());
        params.put(HeadInfo.OS, headInfo.getOs());
        params.put(HeadInfo.OSVER, headInfo.getOsVer());
        params.put(HeadInfo.APP, headInfo.getApp());
        params.put(HeadInfo.APPVER, headInfo.getAppVer());
        params.put(HeadInfo.APPID, headInfo.getAppID());
        params.put(HeadInfo.LANGUAGE, headInfo.getLanguage());
        params.put(HeadInfo.LOCALE, headInfo.getLocale());
        params.put(HeadInfo.ACCOUNT, headInfo.getAccount());
        params.put(HeadInfo.TOKEN, headInfo.getToken());
        return AsyncHttpClient.getUrlWithQueryString(false, path, params);
    }

    public RequestParams getRequestParams() {
        RequestParams params = new RequestParams();
        params.put(CONTENT, getContent());
        return params;
    }
}
