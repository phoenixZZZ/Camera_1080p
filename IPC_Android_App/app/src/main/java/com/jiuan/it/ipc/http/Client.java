package com.jiuan.it.ipc.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.innovation.android.library.http.HeadInfo;
import com.innovation.android.library.http.InnovationClient;
import com.innovation.android.library.http.InnovationHttpResponseHandler;
import com.jiuan.it.ipc.http.request.BindWBoxUserRequest;
import com.jiuan.it.ipc.http.request.Changes;
import com.jiuan.it.ipc.http.request.CodeLoginRequest;
import com.jiuan.it.ipc.http.request.DelEqRequest;
import com.jiuan.it.ipc.http.request.DelWBoxRequest;
import com.jiuan.it.ipc.http.request.GetLoginCodeRequest;
import com.jiuan.it.ipc.http.request.GetWBoxDetailRequest;
import com.jiuan.it.ipc.http.request.LastVerRequest;
import com.jiuan.it.ipc.http.request.MergedPicRequest;
import com.jiuan.it.ipc.http.request.MyBoxEquipRequest;
import com.jiuan.it.ipc.http.request.PhoneLoginRequest;
import com.jiuan.it.ipc.http.request.RefreshTokenRequest;
import com.jiuan.it.ipc.http.request.RegPhoneRequest;
import com.jiuan.it.ipc.http.request.ResetCodeRequest;
import com.jiuan.it.ipc.http.request.TypeCodeDetailRequest;
import com.jiuan.it.ipc.http.request.TypeCodesRequest;
import com.jiuan.it.ipc.http.request.UpdateStateRequest;
import com.jiuan.it.ipc.http.request.UpdateWhiteBoxRequest;
import com.jiuan.it.ipc.http.request.VerInfoRequest;
import com.jiuan.it.ipc.http.request.VerifyPhoneCodeRequest;
import com.jiuan.it.ipc.http.request.VerifyPhoneRequest;
import com.jiuan.it.ipc.http.request.VerifyResetCodeRequest;
import com.jiuan.it.ipc.http.request.VerifyResetRequest;
import com.jiuan.oa.android.library.util.Installation;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.client.params.ClientPNames;

public enum Client {

    INSTANCE;
    /*根据通讯协议设定的常量*/

    /**
     * 空的时间戳
     */
    public static final long EMPTY_TIME = -2208988800l;

    private static final String OS = "Android";

    private static final String APP = "IntelliHouse";

    private AsyncHttpClient client = new AsyncHttpClient();

    public interface NetworkUnavailableListener {
        void networkUnavailable();
    }

    /**
     * 1.申请智能控注册验证码
     *
     * @param phone 手机号
     */
    public static RequestHandle requestVerifyPhone(Context context, String phone, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        VerifyPhoneRequest helper = new VerifyPhoneRequest();
        helper.setPhone(phone);
        return request(context, helper, VerifyPhoneRequest.PATH, responseHandler, listener);
    }

    /**
     * 2.验证手机验证码
     *
     * @param phone 手机号
     * @param code  验证码
     */
    public static RequestHandle requestVerifyPhoneCode(Context context, String phone, String code, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        VerifyPhoneCodeRequest helper = new VerifyPhoneCodeRequest();
        helper.setPhone(phone);
        helper.setCode(code);
        return request(context, helper, VerifyPhoneCodeRequest.PATH, responseHandler, listener);
    }

    /**
     * 3.手机账号注册
     *
     * @param phone    手机号
     * @param code     验证码
     * @param password 密码
     */
    public static RequestHandle requestRegPhone(Context context, String phone, String code, String password, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        RegPhoneRequest helper = new RegPhoneRequest();
        helper.setPhone(phone);
        helper.setCode(code);
        helper.setPassword(password);
        return request(context, helper, RegPhoneRequest.PATH, responseHandler, listener);
    }

    /**
     * 4.手机账号登录
     *
     * @param phone    手机号
     * @param password 密码
     */
    public static RequestHandle requestPhoneLogin(Context context, String phone, String password, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        PhoneLoginRequest helper = new PhoneLoginRequest();
        helper.setPhone(phone);
        helper.setPassword(password);
        return request(context, helper, PhoneLoginRequest.PATH, responseHandler, listener);
    }

    /**
     * 5.申请重置密码验证码
     *
     * @param phone    手机号
     */
    public static RequestHandle requestVerifyReset(Context context, String phone, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        VerifyResetRequest helper = new VerifyResetRequest();
        helper.setPhone(phone);
        return request(context, helper, VerifyResetRequest.PATH, responseHandler, listener);
    }

    /**
     * 6.验证重置密码验证码
     *
     * @param phone    手机号
     * @param code     手机验证码
     */
    public static RequestHandle requestVerifyResetCode(Context context, String phone, String code, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        VerifyResetCodeRequest helper = new VerifyResetCodeRequest();
        helper.setPhone(phone);
        helper.setCode(code);
        return request(context, helper, VerifyResetCodeRequest.PATH, responseHandler, listener);
    }

    /**
     * 7.重置密码
     *
     * @param phone    手机号
     * @param code     手机验证码
     * @param password 密码
     */
    public static RequestHandle requestResetCode(Context context, String phone, String code, String password, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        ResetCodeRequest helper = new ResetCodeRequest();
        helper.setPhone(phone);
        helper.setCode(code);
        helper.setPassword(password);
        return request(context, helper, ResetCodeRequest.PATH, responseHandler, listener);
    }

    /**
     * 8.访问令牌刷新
     *
     * @param un           HGUID
     * @param refreshToken 刷新令牌
     */
    public static RequestHandle requestRefreshToken(Context context, String un, String refreshToken, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        RefreshTokenRequest helper = new RefreshTokenRequest();
        helper.setUn(un);
        helper.setRefreshToken(refreshToken);
        return request(context, helper, RefreshTokenRequest.PATH, responseHandler, listener);
    }

    /**
     * 9.申请手机动态登录码
     *
     * @param phone    手机号
     */
    public static RequestHandle requestGetLoginCode(Context context, String phone, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        GetLoginCodeRequest helper = new GetLoginCodeRequest();
        helper.setPhone(phone);
        return request(context, helper, GetLoginCodeRequest.PATH, responseHandler, listener);
    }

    /**
     * 10.手机动态码登录
     *
     * @param phone    手机号
     * @param code     登录动态码
     */
    public static RequestHandle requestCodeLogin(Context context, String phone, String code, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        CodeLoginRequest helper = new CodeLoginRequest();
        helper.setPhone(phone);
        helper.setCode(code);
        return request(context, helper, CodeLoginRequest.PATH, responseHandler, listener);
    }

    /**
     * 16.删除设备
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param eqID         设备ID
     * @param password     密码(如果仅为此设备的被分享者则此字段不需要输入)
     */
    public static RequestHandle requestDelEq(Context context, String un, String accessToken, String eqID, String password, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        DelEqRequest helper = new DelEqRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setEqID(eqID);
        helper.setPassword(password);
        return request(context, helper, DelEqRequest.PATH, responseHandler, listener);
    }


    /**
     * 51.用户绑定白盒子
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param wbCode       白盒唯一16位标示ID
     */
    public static RequestHandle requestBindWBoxUser(Context context, String un, String accessToken, String wbCode, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        BindWBoxUserRequest helper = new BindWBoxUserRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setWbCode(wbCode);
        return request(context, helper, BindWBoxUserRequest.PATH, responseHandler, listener);
    }

    /**
     * 52.获取我的设备列表（设备和白盒）
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     */
    public static RequestHandle requestMyBoxEquip(Context context, String un, String accessToken, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        MyBoxEquipRequest helper = new MyBoxEquipRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        return request(context, helper, MyBoxEquipRequest.PATH, responseHandler, listener);
    }

    /**
     * 53.获取白盒详细信息
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param wbCode       白盒唯一16位标示ID
     */
    public static RequestHandle requestGetWBoxDetail(Context context, String un, String accessToken, String wbCode, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        GetWBoxDetailRequest helper = new GetWBoxDetailRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setWbCode(wbCode);
        return request(context, helper, GetWBoxDetailRequest.PATH, responseHandler, listener);
    }

    /**
     * 55.修改白盒信息
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param wBoxCode     白盒ID
     * @param changes      修改属性列表
     */
    public static RequestHandle requestUpdateWhiteBox(Context context, String un, String accessToken, String wBoxCode, Changes[] changes, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        UpdateWhiteBoxRequest helper = new UpdateWhiteBoxRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setwBoxCode(wBoxCode);
        helper.setChanges(changes);
        return request(context, helper, UpdateWhiteBoxRequest.PATH, responseHandler, listener);
    }

    /**
     * 58.删除白盒设备
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param wBoxID       白盒子唯一16位ID
     * @param password    用户登录密码
     */
    public static RequestHandle requestDelWBox(Context context, String un, String accessToken, String wBoxID, String password, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        DelWBoxRequest helper = new DelWBoxRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setwBoxID(wBoxID);
        helper.setPassword(password);
        return request(context, helper, DelWBoxRequest.PATH, responseHandler, listener);
    }
    /**
     * 全景拍照获取图片的上传状态
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param deviceID     IPC设备的ID
     * @param typeCode     文件目录名称
     */
    public static RequestHandle requestUploadState(Context context, String un, String accessToken, String deviceID, String typeCode, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        UpdateStateRequest helper = new UpdateStateRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setDeviceID(deviceID);
        helper.setTypeCode(typeCode);
        return request(context, helper, UpdateStateRequest.PATH, responseHandler, listener);
    }

    /**
     * 获取IPC设备文件目录
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param deviceID     IPC设备的ID
     */
    public static RequestHandle requestTypeCodes(Context context, String un, String accessToken, String deviceID, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        TypeCodesRequest helper = new TypeCodesRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setDeviceID(deviceID);
        return request(context, helper, TypeCodesRequest.PATH, responseHandler, listener);
    }

    /**
     * 获取目录详细信息
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param deviceID     IPC设备的ID
     * @param typeCode     文件目录名称
     */
    public static RequestHandle requestTypeCodeDetail(Context context, String un, String accessToken, String deviceID, String typeCode, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        TypeCodeDetailRequest helper = new TypeCodeDetailRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setDeviceID(deviceID);
        helper.setTypeCode(typeCode);
        return request(context, helper, TypeCodeDetailRequest.PATH, responseHandler, listener);
    }

    /**
     * 获取目录详细信息
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param deviceID     IPC设备的ID
     * @param typeCode     文件目录名称
     */
    public static RequestHandle requestMergedPic(Context context, String un, String accessToken, String deviceID, String typeCode, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        MergedPicRequest helper = new MergedPicRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setDeviceID(deviceID);
        helper.setTypeCode(typeCode);
        return request(context, helper, MergedPicRequest.PATH, responseHandler, listener);
    }

    /**
     * 检查升级
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param cateSN     设备类型唯一编号
     * @param ver     设备固件版本号
     */
    public static RequestHandle requestLastVer(Context context, String un, String accessToken, String cateSN, String ver, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        LastVerRequest helper = new LastVerRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setCateSN(cateSN);
        helper.setVer(ver);
        return request(context, helper, LastVerRequest.PATH, responseHandler, listener);
    }

    /**
     * 获取指定版本的固件信息
     *
     * @param un           HGUID
     * @param accessToken  访问令牌
     * @param cateSN     设备类型唯一编号
     * @param hVer     指定设备硬件版本号
     */
    public static RequestHandle requestVerInfo(Context context, String un, String accessToken, String cateSN, String hVer, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {
        VerInfoRequest helper = new VerInfoRequest();
        helper.setUn(un);
        helper.setAccessToken(accessToken);
        helper.setCateSN(cateSN);
        helper.sethVer(hVer);
        return request(context, helper, VerInfoRequest.PATH, responseHandler, listener);
    }

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
    private static RequestHandle request(Context context, BaseRequest helper, String path, InnovationHttpResponseHandler responseHandler, NetworkUnavailableListener listener) {

        if (!isNetworkConnect(context)) {
            if (listener != null) {
                listener.networkUnavailable();
            }
            return null;
        }

        HeadInfo.Builder builder = new HeadInfo.Builder(context).os(OS).app(APP).appID(Installation.id(context));
        helper.setHeadInfo(builder.build());
        InnovationClient client = InnovationClient.getInstance();
        client.setSSLSocketFactory();
        return client.post(context, helper.getPathWithHeadInfo(path), helper.getRequestParams(), responseHandler);
    }

    public static boolean isNetworkConnect(Context context) {
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
}
