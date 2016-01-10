package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.request.Changes;
import com.jiuan.it.ipc.http.response.EquipSet;
import com.jiuan.it.ipc.http.response.GetWBoxDetailResponse;
import com.jiuan.it.ipc.http.response.UpdateWhiteBoxResponse;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Arrays;

public class WhiteBoxDetailActivity extends BaseActivity implements View.OnClickListener {

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private final String WHITE_BOX_ID = "ID: ";

    // 返回
    private ImageView mReturn;
    // 白盒ID
    private TextView mWhiteBoxId;
    // 昵称
    private TextView mNickName;
    // 昵称布局
    private RelativeLayout mNickNameLayout;
    // 位置
    private TextView mLocation;
    // 位置布局
    private RelativeLayout mLocationLayout;
    // 接入设备
    private TextView mConnectedDevice;
    // 接入设备布局
    private RelativeLayout mConnectedDeviceLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.white_box_detail);

        initView();

        Bundle bundle = getIntent().getExtras();

        String whiteBoxId = bundle.getString("whiteBoxId");
        // 设置白盒ID
        mWhiteBoxId.setText(WHITE_BOX_ID + whiteBoxId);

        getWhiteBoxDetail(whiteBoxId);

    }

    private void initView() {

        mReturn = (ImageView) findViewById(R.id.ui_1_1_1_1_return);
        mWhiteBoxId = (TextView) findViewById(R.id.ui_1_1_1_1_white_box_id);
        mNickName = (TextView) findViewById(R.id.ui_1_1_1_1_nickname);
        mNickNameLayout = (RelativeLayout) findViewById(R.id.ui_1_1_1_1_line_4);
        mLocation = (TextView) findViewById(R.id.ui_1_1_1_1_location);
        mLocationLayout = (RelativeLayout) findViewById(R.id.ui_1_1_1_1_line_6);
        mConnectedDevice = (TextView) findViewById(R.id.ui_1_1_1_1_connected_device);
        mConnectedDeviceLayout = (RelativeLayout) findViewById(R.id.ui_1_1_1_1_line_8);

        // 注册单击事件
        mReturn.setOnClickListener(this);
        mNickNameLayout.setOnClickListener(this);
        mLocationLayout.setOnClickListener(this);
    }

    /**
     * 获取白盒详细信息
     */
    private void getWhiteBoxDetail(String whiteBoxId) {
        // 调用API53.获取白盒详细信息

        Client.requestGetWBoxDetail(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), whiteBoxId,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        GetWBoxDetailResponse response = get(value.toString(), GetWBoxDetailResponse.class);

                        if (response != null) {
                            mNickName.setText(response.getWbName());
                            mLocation.setText(response.getLocation());

                            final ArrayList<EquipSet> equipList = new ArrayList<EquipSet>();

                            equipList.addAll(Arrays.asList(response.getEquipSet()));

                            Integer onlineDeviceCount = 0;

                            if (equipList != null && equipList.size() > 0) {
                                for (EquipSet equip : equipList) {
                                    if (equip.getIsOnline() == 1) {
                                        onlineDeviceCount++;
                                    }
                                }

                                mConnectedDevice.setText(onlineDeviceCount + "/" + response.getEquipSet().length);

                            } else {
                                mConnectedDevice.setText("0/0");
                            }

                        }

                    }

                }, new ZNKNetWorkUnavialableListener());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ui_1_1_1_1_return:
                ZnkActivityUtil.finishActivity();
                break;
            case R.id.ui_1_1_1_1_line_4:
                // 设置白盒昵称
                setWhiteBoxNickName();
                break;
            case R.id.ui_1_1_1_1_line_6:
                // 设置白盒位置
                setWhiteBoxLocation();
                break;
        }
    }

    /**
     * 设置白盒昵称
     */
    private void setWhiteBoxNickName() {
        final EditText nickName = new EditText(this);

        nickName.setText(mNickName.getText().toString());

        Dialog dialog = new AlertDialog.Builder(this).setTitle("修改白盒昵称").setView(nickName)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                updateWhiteBox(new Changes("Name", nickName.getText().toString()));
                                mNickName.setText(nickName.getText().toString());
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        dialog.show();
    }

    /**
     * 设置白盒位置
     */
    private void setWhiteBoxLocation() {
        final EditText location = new EditText(this);

        location.setText(mLocation.getText().toString());

        Dialog dialog = new AlertDialog.Builder(this).setTitle("修改白盒位置").setView(location)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                updateWhiteBox(new Changes("Location", location.getText().toString()));
                                mLocation.setText(location.getText().toString());
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();

        dialog.show();
    }

    /**
     * 更新白盒信息
     * @param change 修改属性
     */
    private void updateWhiteBox(Changes change) {

        String whiteBoxId = getIntent().getExtras().getString("whiteBoxId");

        // 调用API55修改白盒信息
        Client.requestUpdateWhiteBox(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), whiteBoxId, new Changes[] { change },
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);

                        UpdateWhiteBoxResponse response = get(value.toString(), UpdateWhiteBoxResponse.class);

                        if (response != null) {
                            if (response.getIsSuccess() == 1) {
                                // 显示成功消息
                                //Toast.makeText(WhiteBoxDetailActivity.this, "修改白盒信息成功", Toast.LENGTH_SHORT).show();
                            } else {
                                ZnkActivityUtil.showSimpleDialog(response.getErrors());
                            }
                        }
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

}