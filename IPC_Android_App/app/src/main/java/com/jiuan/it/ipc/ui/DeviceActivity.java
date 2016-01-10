package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.GlobalBean;
import com.jiuan.it.ipc.tools.WifiAdmin;
import com.jiuan.it.ipc.ui.adapter.ConnectAdapter;
import com.jiuan.it.ipc.model.DeviceListBean;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.common.db.DBUpdateUtil;
import com.jiuan.it.ipc.common.db.ZnkDBHelper;
import com.jiuan.it.ipc.common.dialog.TitlePopup;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.BeanUtil;
import com.jiuan.it.ipc.common.util.DateUtil;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.EqInfo;
import com.jiuan.it.ipc.http.response.MyBoxEquipResponse;
import com.jiuan.it.ipc.http.response.WBoxInfo;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.ui.view.ActivityCache;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeviceActivity extends BaseActivity implements View.OnClickListener,
        CustomToolbar.OnClickCustomToolbarListener, SwipeRefreshLayout.OnRefreshListener,
        ConnectAdapter.ConnectViewClickListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private final String WHITE_BOX = "白盒";

    private CustomToolbar toolbar =null;

    private TextView clickData =null;

    // 数据库工具类
    private ZnkDBHelper dbHelper;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LinearLayoutManager layoutManager;

    private ConnectAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private List<DeviceListBean> myDeviceListBeans;

    private ArrayList<WBoxInfo> mWBoxInfos;

    private EqInfo[] mEqInfos;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click_data:
                //设置IP、ID
                Config.SOCKET_SERVER = url;
                Config.DEVICE_CODE = null;
                //界面切换
                Intent intent = new Intent(this.getApplicationContext(),
                        NoneSetActivity.class);
                intent.putExtra("name", TAG_CLASS_NAME);
                intent.putExtra("LoginType", 0);
                startActivity(intent);
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 父类onCreate
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.device);
        // 初始化控件
        initControl();
        dbHelper = GlobalBeanManager.getDbHelper(this);
        // 初始化
        myDeviceListBeans = new ArrayList<DeviceListBean>();
        mSwipeRefreshLayout.setRefreshing(false);
        // 调用WebService 取得数据
        getWebServiceData();
    }

    private void initControl() {
        toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);
        clickData= (TextView)this.findViewById(R.id.click_data);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ConnectAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mSwipeRefreshLayout.setEnabled(layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
        toolbar.setOnClickCuteToolbarListener(this);
        clickData.setOnClickListener(this);
    }

    private void getWebServiceData() {
        getUserDeviceList();
    }

    private void getUserDeviceList() {
        if(Config.getGlobal(this)==null){
             stopSyncUI();
             return;
        }
        Client.requestMyBoxEquip(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(),
                new ResponseHandler() {
                    @Override
                    public void onZNKFailure(String value) {
                        stopSyncUI();
                        ZnkActivityUtil.showSimpleDialog("提示", value);
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        stopSyncUI();
                        showTokenFailure(value);
                    }

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        stopSyncUI();
                        // 调用获取用户所有设备信息API
                        MyBoxEquipResponse myBoxEquipResponses = get(value.toString(), MyBoxEquipResponse.class);

                        if (myBoxEquipResponses != null) {

                            List<DeviceListBean> myDeviceList = new ArrayList<DeviceListBean>();

                            DeviceListBean myDeviceBean = null;

                            mEqInfos = myBoxEquipResponses.getEqInfos();

                            if (mEqInfos != null && mEqInfos.length > 0) {

                                for (EqInfo eqInfo : mEqInfos) {

                                    myDeviceBean = new DeviceListBean();

                                    myDeviceBean.setCode(eqInfo.getEquipCode());
                                    myDeviceBean.setName(eqInfo.getName());
                                    myDeviceBean.setEqType(1);
                                    myDeviceBean.setWhiteBoxId(eqInfo.getwBoxID());

                                    myDeviceList.add(myDeviceBean);
                                }

                            }

                            mWBoxInfos = new ArrayList<WBoxInfo>();

                            if (myBoxEquipResponses.getwBoxInfos() != null && myBoxEquipResponses.getwBoxInfos().length > 0) {

                                mWBoxInfos.addAll(Arrays.asList(myBoxEquipResponses.getwBoxInfos()));

                                for (WBoxInfo wBoxInfo : mWBoxInfos) {
                                    myDeviceBean = new DeviceListBean();
                                    myDeviceBean.setCode(wBoxInfo.getwBoxCode());
                                    myDeviceBean.setFInfo(wBoxInfo.getfInfo());
                                    if (TextUtils.isEmpty(wBoxInfo.getName())) {
                                        myDeviceBean.setName(TextUtils.concat(WHITE_BOX,
                                                wBoxInfo.getwBoxCode().substring(wBoxInfo.getwBoxCode().length() - 4)).toString());
                                    } else {
                                        myDeviceBean.setName(wBoxInfo.getName());
                                    }

                                    myDeviceBean.setEqType(2);
                                    myDeviceBean.setLanIP(wBoxInfo.getLanIP());
                                    myDeviceList.add(myDeviceBean);
                                }
                            }
                            mAdapter.setList(myDeviceList);
                            myDeviceListBeans.addAll(myDeviceList);
                        }

                    }
                }, new ZNKNetWorkUnavialableListener());
    }

    @Override
    public void onClickLeft() {
        //注销用户
        exitApp(0, "是否注销用户？");
    }

    @Override
    public void onClickRight() {
        // 跳转到关联白盒子UI
        WifiAdmin wifiAdmin = new WifiAdmin(DeviceActivity.this); ////配置连接白盒
        if(wifiAdmin.checkState()== WifiManager.WIFI_STATE_DISABLED) {
            ZnkActivityUtil.showSimpleDialog("提示","WIFI关闭，无法配置！");
            return;
        }else {
            ZnkActivityUtil.startActivity(ScanWhiteBoxActivity.class);
        }


    }

    @Override
    public void onItemClick(View view, int position, DeviceListBean model) {
        //设置IP、ID
        //DeviceListBean model =myDeviceListBeans.get(position);
        Config.SOCKET_SERVER =  model.getLanIP();
        Config.DEVICE_CODE = model.getCode();
        Config.FINFO = model.getFInfo();

        //界面切换
        Intent intent = new Intent(getApplicationContext(),
                CameraMainActivity.class);
        intent.putExtra("name", TAG_CLASS_NAME);
        intent.putExtra("LoginType", 1);
        startActivity(intent);
    }


    @Override
    public void onDeleteBtnClick(View view, int position) {
        DeviceListBean model =myDeviceListBeans.get(position);
        Intent intent = ZnkActivityUtil.getSingletonIntent();
        intent.setClass(ZnkActivityUtil.getContext(), DeleteDeviceConfirmActivity.class);
        intent.putExtra("eqID", model.getCode());
        intent.putExtra("eqType", String.valueOf(model.getEqType()));
        intent.putExtra("name", ZnkActivityUtil.getClassName());
        ZnkActivityUtil.getContext().startActivity(intent);
    }

    @Override
    public void onRefresh() {
        startSyncUI();
    }

    /*开启下拉刷新*/
    private void startSyncUI() {
        getUserDeviceList();
    }

    /*停止下拉刷新*/
    private void stopSyncUI() {
        mSwipeRefreshLayout.setRefreshing(false);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp(1,"确定要退出吗？");
            return true;
        }

        return false;
    }

    private  void  exitApp(final int mark,String message){
        Dialog dialog = new AlertDialog.Builder(this).setTitle("提示").setMessage(message)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (mark == 0) { //注销用户
                                    Config.cleanLogin(DeviceActivity.this);
                                    Intent intent = new Intent(DeviceActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    ZnkActivityUtil.finishActivity();
                                } else if (mark == 1){
                                    // 结束所有activity
                                    ActivityCache.getScreenManager().popAllActivity();
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(0);
                                }

                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        dialog.show();
    }
}