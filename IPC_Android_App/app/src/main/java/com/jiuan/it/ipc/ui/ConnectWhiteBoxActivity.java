package com.jiuan.it.ipc.ui;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.IPCameraApplication;
import com.jiuan.it.ipc.common.textwatcher.TextWatcherFactory;
import com.jiuan.it.ipc.common.udp.UDPActivity;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.tcp.TcpRequestClient;
import com.jiuan.it.ipc.tcp.TcpResponseHandler;
import com.jiuan.it.ipc.tcp.response.GetWifiListResponse;
import com.jiuan.it.ipc.tcp.response.RespContentForGetWifi;
import com.jiuan.it.ipc.tcp.response.SetWifiListResponse;
import com.jiuan.it.ipc.tools.WifiAdmin;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectWhiteBoxActivity extends UDPActivity implements View.OnClickListener,
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    // WIFI下拉列表
    private Spinner mWifiSpinner;

    // WIFI密码
    private EditText mWifiPassword;

    // 连接
    private Button mConnect;

    // 刷新
    private ImageView refresh;

    // 加载效果
    //private ProgressBar mLoadingBar;

    private List<String> spinnerItems = null;

    private Map<String, RespContentForGetWifi> ssidMap = null;

    private ArrayAdapter<String> adapter = null;

    // WIFI密码清空图片
    private ImageView mPasswordClearImageView;

    // WIFI管理器
    private WifiManager mWifiManager;

    // 白盒ID
    private String mWhiteBoxId;

    private int mNetworkId;

    private CustomToolbar toolbar =null;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 1:
                    dismissProgressDialog();
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    dismissProgressDialog();
                    setWifiInfoSuccess();
                    break;
                case 3:
                    showErrorMsg(msg.obj);
                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //mLoadingBar.setVisibility(View.GONE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.connect_white_box);

        toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);
        mWifiSpinner = (Spinner) findViewById(R.id.ui_1_1_1_search_text);
        mWifiPassword = (EditText) findViewById(R.id.ui_1_1_1_wifi_password);
        mConnect = (Button) findViewById(R.id.ui_1_1_1_connect);
        mPasswordClearImageView = (ImageView) findViewById(R.id.ui_connect_device_password_Clear_ImageView);
        //mLoadingBar = (ProgressBar) findViewById(R.id.ui_1_1_1_loading);
        refresh = (ImageView) findViewById(R.id.ui_search_ImageView);


        mConnect.setOnClickListener(this);
        refresh.setOnClickListener(this);
        toolbar.setOnClickCuteToolbarListener(this);

        // 清空图片显示事件
        this.mWifiPassword.addTextChangedListener(TextWatcherFactory.getTextWatcher(mWifiPassword,
                this.mPasswordClearImageView, TextWatcherFactory.TextWatcherTyp.ShowClearImgTextWatcher));

        // 获取白盒ID
        mWhiteBoxId = getIntent().getExtras().getString("whiteBoxId");

        mNetworkId = getIntent().getExtras().getInt("networkId");


        // 初始化wifi列表
        initWifiSpinner();

        // 调用父类使用UDP查找同一子网下设备的方法
        searchDevice(mWhiteBoxId);

    }

    @Override
    protected void disConnection() {
        super.disConnection();
        tipErrorShow("TCP连接失败");
        dismissProgressDialog();
    }

    @Override
    protected void tcpConnection() {
        tipErrorShow("已连接到白盒");
        ssidMap = new HashMap<String, RespContentForGetWifi>();
        requestGetWifiList();
    }

    private void initWifiSpinner() {

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        spinnerItems = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinnerItems);

        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        mWifiSpinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ui_1_1_1_connect:
                showProgressDialog("提示","设备WIFI连接",10*1000);
                doConnect();
                break;
            case R.id.ui_search_ImageView:
                showProgressDialog("提示","WIFI列表加载",10*1000);

                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (!TextUtils.equals(wifiInfo.getSSID().replace("\"", ""), mWhiteBoxId)) {
                    tipErrorShow("请将wifi切换为" + mWhiteBoxId);
                } else {
                    if (isSuccess() && Arrays.equals(IPCameraApplication.getID(), mWhiteBoxId.getBytes())) {
                        // 使用已建立的TCP连接获取wifi列表
                        tcpConnection();
                    } else {
                        // 调用父类使用UDP查找同一子网下设备的方法
                        searchDevice(mWhiteBoxId);
                    }
                }

                break;

        }

    }

    /**
     * 跳转到连接页面
     */
    private void doConnect() {

        // 通过TCP将SSID和密码发送给白盒
        requestSetWifiInfo();

    }

    /**
     * 向白盒发送请求获取wifi列表
     */
    private void requestGetWifiList() {

        // 发送请求
        sendData(TcpRequestClient.getWifiListRequest(mWhiteBoxId), new TcpResponseHandler() {
            @Override
            public void onSuccess(String strJson) {
                super.onSuccess(strJson);
                GetWifiListResponse response = jsonToObject(strJson, GetWifiListResponse.class);
                if (response != null && response.getWifiCommand() != null) {
                    for (int i = 0; i < response.getWifiCommand().getRespContent().length; i++) {
                        spinnerItems.add(response.getWifiCommand().getRespContent()[i].getSsid());
                        ssidMap.put(response.getWifiCommand().getRespContent()[i].getSsid(),
                                response.getWifiCommand().getRespContent()[i]);
                    }
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                }

            }
        });
    }

    /**
     * 向白盒发送请求设置wifi信息
     */
    private void requestSetWifiInfo() {

        if(mWifiSpinner.getSelectedItem()==null||TextUtils.isEmpty(mWifiPassword.getText())){
            dismissProgressDialog();
            return;
        }
        RespContentForGetWifi wifiInfo = ssidMap.get(mWifiSpinner.getSelectedItem().toString());

        // 兼容白盒的wifi加密规则
        if (TextUtils.equals(wifiInfo.getSecurityMode(), "WPA1PSK")) {
            wifiInfo.setSecurityMode("WPAPSK");
        }

        sendData(TcpRequestClient.setWifiInfoRequest(mWhiteBoxId, wifiInfo, mWifiPassword.getText().toString()),
                new TcpResponseHandler() {
                    @Override
                    public void onSuccess(String strJson) {
                        super.onSuccess(strJson);
                        SetWifiListResponse response = jsonToObject(strJson, SetWifiListResponse.class);

                        if (response != null
                                && response.getWifiCommand() != null
                                && response.getWifiCommand().getRespContent() != null
                                && TextUtils.equals(response.getWifiCommand().getRespContent().getRespOrder(), "OK")) {

                            Message msg = handler.obtainMessage();
                            msg.arg1 = 2;
                            handler.sendMessage(msg);
                        }

                    }
                });
    }

    @Override
    protected void sendDataFailure(String result) {
        if (TextUtils.equals(result, "内网不可用")) {
            disConnection();
        }
    }

    @Override
    public void onClickLeft() {
        showProgressDialog("提示","WIFI切换中",30*1000);
        setWifiInfoSuccess();
    }

    @Override
    public void onClickRight() {

    }

    private void setWifiInfoSuccess() {
        WifiAdmin wifiAdmin = new WifiAdmin(ConnectWhiteBoxActivity.this);
        WifiConfiguration tempConfig = wifiAdmin.IsExsits(mWhiteBoxId);
        if(tempConfig != null) {
            //删除白盒网络
            mWifiManager.removeNetwork(tempConfig.networkId);
            // mWifiManager.disableNetwork(tempConfig.networkId);
            // 连接配置好的指定ID的网络
            mWifiManager.enableNetwork(mNetworkId, true);
        }
        /** 返回设备界面*/
        start.postDelayed(runnable, sleep);

    }
    /** 返回设备界面*/
    Handler start=new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //返回设备界面
            //ZnkActivityUtil.startActivity(DeviceActivity.class);
            dismissProgressDialog();
            Intent intent = new Intent(ConnectWhiteBoxActivity.this, DeviceActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            ZnkActivityUtil.finishActivity();
        }
    };



    private void showErrorMsg(Object obj) {
        Toast.makeText(this, obj.toString(), Toast.LENGTH_SHORT).show();
        mConnect.setEnabled(true);
    }


}