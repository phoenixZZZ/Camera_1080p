package com.jiuan.it.ipc.common.listener;


import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;

public class ZNKNetWorkUnavialableListener implements Client.NetworkUnavailableListener {
    @Override
    public void networkUnavailable() {
        // 提示网络连接失败
        ZnkActivityUtil.showSimpleDialog(ZnkActivityUtil.getContext().getString(R.string.warn_message_105));
    }
}
