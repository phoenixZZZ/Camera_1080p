package com.jiuan.it.ipc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.GlobalBean;
import com.jiuan.it.ipc.model.LoginInfo;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.DateUtil;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.PhoneLoginResponse;
import com.google.gson.JsonElement;
import com.innovation.android.library.util.InnovationAlgorithm;
import com.jiuan.it.ipc.ui.animation.AnimationConfig;
import com.jiuan.it.ipc.ui.widget.CustomEditGroup;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.jiuan.oa.android.library.util.MobileNumberUtil;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录页面
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener,View.OnFocusChangeListener,
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar  toolbar =null;

    private CustomEditGroup mUserNameEditText = null;

    private CustomEditGroup mPasswordEditText = null;

    private Button mLoginButton = null;

    private Button mRegButton = null;

    private TextView mQuickLoginTextView = null;

    private TextView mForgetPasswordTextView = null;

    private  Map clearMap = new HashMap<EditText,ImageView>();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_login);

        toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);
        mUserNameEditText = (CustomEditGroup)this.findViewById(R.id.userNameEditText);
        mPasswordEditText = (CustomEditGroup)this.findViewById(R.id.passwordEditText);
        mLoginButton = (Button)this.findViewById(R.id.loginButton);
        mRegButton = (Button)this.findViewById(R.id.regButton);
        mQuickLoginTextView = (TextView)this.findViewById(R.id.quick_login);
        mForgetPasswordTextView  = (TextView)this.findViewById(R.id.forget_password);

        mLoginButton.setOnClickListener(this);
        mRegButton.setOnClickListener(this);
        mQuickLoginTextView.setOnClickListener(this);
        mForgetPasswordTextView.setOnClickListener(this);
        toolbar.setOnClickCuteToolbarListener(this);


    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ZnkActivityUtil.setVisibility(v.getId(), hasFocus,clearMap);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginButton:
                String number = mUserNameEditText.getContent();
                String password = mPasswordEditText.getContent();
                if (checkInput(number, password)) {
                    mLoginButton.setEnabled(false);
                    doLogin(number,InnovationAlgorithm.SHA1(Config.SECURITY_CODE, password));
                }else {
                    mLoginButton.setEnabled(true);
                }

                break;
            case R.id.regButton:
                doReg();
                break;
            case R.id.quick_login:
                ZnkActivityUtil.startActivity(DynamicLoginActivity.class);
                break;
            case R.id.forget_password:
                ZnkActivityUtil.startActivity(RetrievePasswordActivity.class);
                break;
        }
    }



    /**
     * 登录处理
     */
    private void doLogin(final String number, final String password ) {
        Client.requestPhoneLogin(this, number, password,
                new ResponseHandler() {

                    @Override
                    public void onInnovationStart() {
                        super.onInnovationStart();
                    }

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        // 调用手机账号登录API
                        PhoneLoginResponse phoneLoginResponse = get(value.toString(), PhoneLoginResponse.class);

                        if (phoneLoginResponse != null) {

                            // 全局信息
                            GlobalBean globalBean = new GlobalBean();
                            globalBean.setPhoneNum(number);
                            globalBean.setHguid(phoneLoginResponse.getId());
                            globalBean.setToken(phoneLoginResponse.getToken());
                            Config.setGlobal(LoginActivity.this,new Gson().toJson(globalBean));
                            // 登录成功，保存用户信息
                            LoginInfo loginInfo = new LoginInfo();
                            loginInfo.setUsername(number);
                            loginInfo.setPassword(password);
                            try {
                                loginInfo.setNowDateTime(DateUtil.getNowDateTime());
                            } catch (ParseException e) {
                                loginInfo.setNowDateTime("2000/01/01 00:00:00");
                            }
                            loginInfo.setLoginFlag(true);
                            Config.setLoginInfo(LoginActivity.this,new Gson().toJson(loginInfo));

                            // 跳转到我的设备页面
                            ZnkActivityUtil.startActivity(DeviceActivity.class);
                            ZnkActivityUtil.finishActivity();
                        }

                    }

                    @Override
                    public void onInnovationFinish() {
                        super.onInnovationFinish();
                        mLoginButton.setEnabled(true);
                    }

                    @Override
                    public void onInnovationExceptionFinish() {
                        super.onInnovationExceptionFinish();
                        mLoginButton.setEnabled(true);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

    /**
     * 用户注册处理
     */
    private void doReg() {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(),
                RegisterActivity.class);
        intent.putExtra("name", TAG_CLASS_NAME);
        startActivity(intent);
    }

    @Override
    public void onClickLeft() {

    }

    @Override
    public void onClickRight() {
        //设置IP、ID
        Config.SOCKET_SERVER = url;
        Config.DEVICE_CODE = null;
        //界面切换
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(),
                NoneSetActivity.class);
        intent.putExtra("name", TAG_CLASS_NAME);
        startActivity(intent);
    }

    private boolean checkInput(String number, String password) {
        if (!MobileNumberUtil.isMobile(number)) {
            AnimationConfig.shake(this, mUserNameEditText);
            tipErrorShow(R.string.tip_number_error);
            return false;
        }
        if (number.length()<11) {
            AnimationConfig.shake(this, mUserNameEditText);
            tipErrorShow(R.string.tip_error_number);
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            AnimationConfig.shake(this, mPasswordEditText);
            tipErrorShow(R.string.tip_error_password_need);
            return false;
        }
        return true;
    }

}