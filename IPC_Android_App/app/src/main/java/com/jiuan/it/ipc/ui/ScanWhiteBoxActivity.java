package com.jiuan.it.ipc.ui;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.common.zxing.activity.CaptureActivity;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.tools.WifiAdmin;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;

public class ScanWhiteBoxActivity extends CaptureActivity implements View.OnClickListener ,
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    private TextView deviceId =null;

    private int networkId;

    private String whiteBoxId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_scan_white_box);

        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        deviceId = (TextView)this.findViewById(R.id.device_id);
        toolbar.setOnClickCuteToolbarListener(this);
        deviceId.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected int getViewFinderViewId() {
        return R.id.viewfinder_view;
    }

    @Override
    protected int getSurfaceViewId() {
        return R.id.preview_view;
    }

    @Override
    protected void scanFinish(String scanResult) {
        whiteBoxId = scanResult;
        bindWhiteBox();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.device_id:
                Intent intent = new Intent(getApplicationContext(),
                        ScanWhiteActivity.class);
                startActivity(intent);
                ZnkActivityUtil.finishActivity();
                break;
        }
    }

    @Override
    public void onClickLeft() {
        ZnkActivityUtil.startActivity(DeviceActivity.class);
        ZnkActivityUtil.finishActivity();
    }

    @Override
    public void onClickRight() {

    }

    private void bindWhiteBox() {
        // 调用用户绑定白盒子API
        Client.requestBindWBoxUser(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), whiteBoxId,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        Integer response = get(value.toString(), Integer.class);
                        if (response == 1) {
                            tipErrorShow("绑定白盒子成功！");
                            //配置连接白盒
                            WifiAdmin wifiAdmin = new WifiAdmin(ScanWhiteBoxActivity.this);
                            if(wifiAdmin.checkState()== WifiManager.WIFI_STATE_DISABLED) {
                                ZnkActivityUtil.showSimpleDialogAndFinish("提示","WIFI关闭，无法配置！");
                                return;
                            }
                            networkId = wifiAdmin.getNetworkId();
                            wifiAdmin.openWifi();
                            wifiAdmin.addNetwork(wifiAdmin.CreateWifiInfo(whiteBoxId, Config.white_Password, Config.white_Type));
                            //白盒配置Wifi界面
                            start.postDelayed(runnable, 0);

                        } else {
                            ZnkActivityUtil.showSimpleDialog("绑定白盒子失败！");
                        }
                        ZnkActivityUtil.finishActivity();
                    }

                    @Override
                    public void onZNKFailure(String value) {

                        ZnkActivityUtil.showSimpleDialog("提示", value);
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {

                        showTokenFailure(value);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

    /** 白盒配置Wifi界面*/
    Handler start=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(ScanWhiteBoxActivity.this,
                    ConnectWhiteBoxActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", TAG_CLASS_NAME);
            bundle.putString("whiteBoxId", whiteBoxId);
            bundle.putInt("networkId", networkId);

            intent.putExtras(bundle);
            startActivity(intent);
        }
    };
}