package com.jiuan.it.ipc.tcp;

import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.tcp.request.GetWifiListRequest;
import com.jiuan.it.ipc.tcp.request.ReqContentForGetWifi;
import com.jiuan.it.ipc.tcp.request.ReqContentForSetWifi;
import com.jiuan.it.ipc.tcp.request.SetWifiInfoRequest;
import com.jiuan.it.ipc.tcp.request.WifiCommandForGetWifi;
import com.jiuan.it.ipc.tcp.request.WifiCommandForSetWifi;
import com.jiuan.it.ipc.tcp.response.RespContentForGetWifi;

/**
 * Created by Administrator on 2015/11/27.
 */
public class TcpRequestClient {

    /**
     * 1.向白盒请求获取wifi列表
     *
     * @param whiteBoxId 白盒ID
     */
    public static BaseRequest getWifiListRequest(String whiteBoxId) {

        // 组织请求体
        GetWifiListRequest request = new GetWifiListRequest();

        request.setWbID(whiteBoxId);
        request.setPhoneID(ZnkActivityUtil.getDeviceID());

        WifiCommandForGetWifi wifiCommandRequset = new WifiCommandForGetWifi();

        wifiCommandRequset.setOrderID("AAA7");
        wifiCommandRequset.setRn("REQWifi-1");

        ReqContentForGetWifi reqContent = new ReqContentForGetWifi();

        reqContent.setOrder("REQ\r\n");

        wifiCommandRequset.setReqContentForGetWifi(reqContent);

        request.setWifiCommand(wifiCommandRequset);

        return request;
    }

    /**
     * 2.向白盒请求设置wifi信息
     *
     * @param whiteBoxId 白盒ID
     */
    public static BaseRequest setWifiInfoRequest(String whiteBoxId, RespContentForGetWifi wifiInfo, String password) {

        // 组织请求体
        SetWifiInfoRequest request = new SetWifiInfoRequest();

        request.setWbID(whiteBoxId);
        request.setPhoneID(ZnkActivityUtil.getDeviceID());

        WifiCommandForSetWifi wifiCommandRequset = new WifiCommandForSetWifi();

        wifiCommandRequset.setOrderID("AAA9");
        wifiCommandRequset.setRn("REQWifi-2");

        ReqContentForSetWifi reqContent = new ReqContentForSetWifi();

        reqContent.setSecurityMode(wifiInfo.getSecurityMode());
        reqContent.setChannel(wifiInfo.getChannel());
        reqContent.setEncryptionType(wifiInfo.getEncryptionType());
        reqContent.setSsid(wifiInfo.getSsid());
        reqContent.setPassPhrase(password);

        wifiCommandRequset.setReqContentForSetWifi(reqContent);

        request.setWifiCommand(wifiCommandRequset);

        return request;
    }
}
