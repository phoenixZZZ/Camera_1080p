package com.jiuan.it.ipc.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.ResetCodeResponse;
import com.google.gson.JsonElement;
import com.innovation.android.library.util.InnovationAlgorithm;
import com.jiuan.it.ipc.ui.widget.CustomEditGroup;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;

public class ResetPasswordActivity extends BaseActivity implements View.OnClickListener,
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    // 密码
    private CustomEditGroup mPasswordEditText;

    // 重复密码
    private CustomEditGroup mPasswordAgainEditText;

    // 提交修改密码
    private Button mChangePassword;

    // 手机号
    private String mPhoneNo;

    // 验证码
    private String mSmsVerificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_reset_password);

        mPhoneNo = getIntent().getStringExtra("phoneNo");
        mSmsVerificationCode = getIntent().getStringExtra("verificationCode");

        // 获取页面控件信息
        toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);
        mPasswordEditText = (CustomEditGroup) findViewById(R.id.reset_password_EditText);
        mPasswordAgainEditText = (CustomEditGroup) findViewById(R.id.again_password_EditText);
        mChangePassword = (Button) findViewById(R.id.change_password);

        // 注册点击事件
        mChangePassword.setOnClickListener(this);
        toolbar.setOnClickCuteToolbarListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password:
                // 修改密码
                String password = mPasswordEditText.getContent();
                String changPassword = mPasswordAgainEditText.getContent();
                if(doChangePasswordBeforeCheck(password,changPassword)){
                    mChangePassword.setEnabled(false);
                    doChangePassword(InnovationAlgorithm.SHA1(Config.SECURITY_CODE, changPassword));
                }else {
                    mChangePassword.setEnabled(true);
                }
                break;
        }
    }

    /**
     * 修改密码
     */
    private void doChangePassword(final String tagPassword ) {
            Client.requestResetCode(this, mPhoneNo, mSmsVerificationCode, tagPassword,
                    new ResponseHandler() {
                        @Override
                        public void onInnovationSuccess(JsonElement value) {
                            // 获取返回值
                            ResetCodeResponse resetCodeResponse = get(value.toString(), ResetCodeResponse.class);
                            if (resetCodeResponse != null) {
                                // 全局信息清空
                                Config.cleanGlobal(ResetPasswordActivity.this);
                                // 修改密码成功，清空用户登录信息
                                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                                // 将登录状态清空
                                editor.putBoolean("loginFlag", false);
                                editor.commit();
                                // 跳转到登录页面
                                ZnkActivityUtil.startActivity(LoginActivity.class);
                                ZnkActivityUtil.finishActivity();
                            }
                        }

                        @Override
                        public void onInnovationFinish() {
                            super.onInnovationFinish();
                            mChangePassword.setEnabled(true);
                        }

                        @Override
                        public void onInnovationExceptionFinish() {
                            super.onInnovationExceptionFinish();
                            mChangePassword.setEnabled(true);
                        }

                    }, new ZNKNetWorkUnavialableListener());

    }

    /**
     * 获取验证码之前的检查
     * @return true:通过 false:不通过
     */
    private boolean doChangePasswordBeforeCheck(String password,String changePassword) {

        // 密码是否为空
        if (TextUtils.isEmpty(password)) {
            ZnkActivityUtil.showSimpleDialog(this.getString(R.string.warn_message_103));
            return false;
        }
        // 密码是否长度为6-16不包含空格
        if (password.length() < 6 ||password.length() > 16 ||password.contains(" ")) {
            ZnkActivityUtil.showSimpleDialog(this.getString(R.string.warn_message_106));
            return false;
        }
        // 重复密码是否为空
        if (TextUtils.isEmpty(changePassword)) {
            ZnkActivityUtil.showSimpleDialog(this.getString(R.string.warn_message_110));
            return false;
        }

        // 密码与重复密码是否一致
        if (!TextUtils.equals(password,
                changePassword)) {
            ZnkActivityUtil.showSimpleDialog(this.getString(R.string.warn_message_111));
            return false;
        }

        return true;
    }

    @Override
    public void onClickLeft() {
        // 返回
        ZnkActivityUtil.finishActivity();
    }

    @Override
    public void onClickRight() {

    }
}