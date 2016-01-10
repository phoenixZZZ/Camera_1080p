package com.jiuan.it.ipc.http.share;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.HttpEntity;
import org.apache.http.client.params.ClientPNames;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public enum Client {

    INSTANCE;

    public interface NetworkUnavailableListener {
        void networkUnavailable();
    }

    private AsyncHttpClient client = new AsyncHttpClient();

    /**
     * 下载文件
     */
    public RequestHandle downloadFile(Context context, String url, FileAsyncHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        if (!isNetworkConnect(context)) {
            if (listener != null) {
                listener.networkUnavailable();
            }
            return null;
        }
        // 支持重定向
        client.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        return client.get(context, url, responseHandler);
    }

    /**
     * 取消请求
     */
    public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
        client.cancelRequests(context, mayInterruptIfRunning);
    }

    public RequestHandle request(Context context, Request helper, String path, HttpResponseHandler responseHandler, NetworkUnavailableListener listener) {

        if (!isNetworkConnect(context)) {
            if (listener != null) {
                listener.networkUnavailable();
            }
            return null;
        }
        return post(context, path, helper.getRequestParams(), responseHandler);
    }

    public RequestHandle request(Context context, RequestParams params, String path, HttpResponseHandler responseHandler, NetworkUnavailableListener listener) {

        if (!isNetworkConnect(context)) {
            if (listener != null) {
                listener.networkUnavailable();
            }
            return null;
        }
        return post(context, path, params, responseHandler);
    }

    private boolean isNetworkConnect(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTING
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTING) {
            return true;
        } else if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    private RequestHandle post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        return client.post(context, url, paramsToEntity(params, responseHandler), null, responseHandler);
    }

    private RequestHandle post(Context context, String url, HttpEntity entity, AsyncHttpResponseHandler responseHandler) {
        return client.post(context, url, entity, null, responseHandler);
    }

    private void setSSLSocketFactory() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(sf);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns HttpEntity containing data from RequestParams included with request declaration.
     * Allows also passing progress from upload via provided ResponseHandler
     *
     * @param params          additional request params
     * @param responseHandler ResponseHandlerInterface or its subclass to be notified on progress
     */
    private HttpEntity paramsToEntity(RequestParams params, ResponseHandlerInterface responseHandler) {
        HttpEntity entity = null;

        try {
            if (params != null) {
                entity = params.getEntity(responseHandler);
            }
        } catch (IOException e) {
            if (responseHandler != null) {
                responseHandler.sendFailureMessage(0, null, null, e);
            } else {
                e.printStackTrace();
            }
        }

        return entity;
    }
}
