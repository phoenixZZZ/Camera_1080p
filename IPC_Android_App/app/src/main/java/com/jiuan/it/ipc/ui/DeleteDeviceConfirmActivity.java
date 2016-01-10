package com.jiuan.it.ipc.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.textwatcher.TextWatcherFactory;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.google.gson.JsonElement;
import com.innovation.android.library.util.InnovationAlgorithm;

public class DeleteDeviceConfirmActivity extends BaseActivity implements View.OnClickListener {

    // 密码
    private EditText mPassWord;
    // 确定
    private Button mConfirmBtn;
    // 取消
    private Button mCancelBtn;
    // 清空密码
    private ImageView mPassWordClear;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.delete_device_confirm);

        mPassWord = (EditText) findViewById(R.id.ui_delete_device_confirm_password);
        mPassWordClear = (ImageView) findViewById(R.id.ui_delete_device_confirm_password_clear);

        mCancelBtn = (Button) findViewById(R.id.ui_delete_device_confirm_cancel);
        mConfirmBtn = (Button) findViewById(R.id.ui_delete_device_confirm_confirm);

        // 注册点击事件
        mCancelBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);

        // 清空图片显示事件
        this.mPassWord.addTextChangedListener(TextWatcherFactory.getTextWatcher(mPassWord,
                this.mPassWordClear, TextWatcherFactory.TextWatcherTyp.ShowClearImgTextWatcher));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ui_delete_device_confirm_cancel:
                // 取消
                ZnkActivityUtil.finishActivity();
                break;
            case R.id.ui_delete_device_confirm_confirm:
                // 确认
                doConfirm();
                break;
        }
    }

    /**
     * 确认
     */
    private void doConfirm() {
        mConfirmBtn.setEnabled(false);
        String eqID = getIntent().getStringExtra("eqID");
        String eqType = getIntent().getStringExtra("eqType");
        final String password = InnovationAlgorithm.SHA1(Config.SECURITY_CODE, mPassWord.getText().toString());

        if (TextUtils.equals(eqType, "1")) {
            // 调用API16.删除设备
            Client.requestDelEq(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), eqID, password,
                    new ResponseHandler() {
                        @Override
                        public void onInnovationSuccess(JsonElement value) {

                            Integer code = get(value.toString(), Integer.class);
                            mConfirmBtn.setEnabled(true);
                            if (code != null && code == 1) {
                                ZnkActivityUtil.showSimpleDialogAndFinish("提示", "设备删除成功!");
                            } else {
                                ZnkActivityUtil.showSimpleDialogAndFinish("提示", "设备删除失败!");
                            }

                        }

                        @Override
                        public void onZNKFailure(String value) {
                            mConfirmBtn.setEnabled(true);
                            ZnkActivityUtil.showSimpleDialog("提示", value);
                        }

                        @Override
                        public void onZNKTokenFailure(String value) {
                            mConfirmBtn.setEnabled(true);
                            showTokenFailure(value);
                        }
                    },
                    new ZNKNetWorkUnavialableListener()
            );

        } else if (TextUtils.equals(eqType, "2")) {
            // 调用API58.删除白盒设备
            Client.requestDelWBox(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), eqID, password,
                    new ResponseHandler() {
                        @Override
                        public void onInnovationSuccess(JsonElement value) {
                            Integer code = get(value.toString(), Integer.class);
                            mConfirmBtn.setEnabled(true);
                            if (code != null && code == 1) {
                                ZnkActivityUtil.showSimpleDialogAndFinish("提示", "白盒删除成功!");
                            } else {
                                ZnkActivityUtil.showSimpleDialogAndFinish("提示", "白盒删除失败!");
                            }
                        }

                        @Override
                        public void onZNKFailure(String value) {
                            mConfirmBtn.setEnabled(true);
                            ZnkActivityUtil.showSimpleDialog("提示", value);
                        }

                        @Override
                        public void onZNKTokenFailure(String value) {
                            mConfirmBtn.setEnabled(true);
                            showTokenFailure(value);
                        }
                    },
                    new ZNKNetWorkUnavialableListener()
            );
        }


    }

}