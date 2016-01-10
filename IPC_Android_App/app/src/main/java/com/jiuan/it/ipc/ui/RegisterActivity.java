package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.innovation.android.library.util.InnovationAlgorithm;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.GlobalBean;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.DateUtil;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.RegPhoneResponse;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.ui.animation.AnimationConfig;
import com.jiuan.it.ipc.ui.widget.CustomEditGroup;
import com.jiuan.it.ipc.ui.widget.CustomTimerButton;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.jiuan.oa.android.library.util.MobileNumberUtil;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener,
        CustomToolbar.OnClickCustomToolbarListener, CustomTimerButton.TimerListener{

    private CustomToolbar toolbar =null;

    private CustomEditGroup mUserNameEditText = null;

    private CustomEditGroup mSMSVerificationCodeEditText = null;

    private CustomEditGroup mPasswordEditText = null;

    private Button mRegisterSubmitImgView = null;

    private CustomTimerButton button_timer = null;

    private TextView mAgreementTextView = null;

    private String second;

    private Map clearMap = new HashMap<EditText,ImageView>();

    private boolean getSMSCodeFlg;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 父类onCreate
        super.onCreate(savedInstanceState);

        second = getString(R.string.button_timer);
        // 初期加载画面显示
        setContentView(R.layout.layout_regist);

        // 标题栏
        this.toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);
        // 用户名
        this.mUserNameEditText = (CustomEditGroup) this.findViewById(R.id.register_userName);
        // 验证码
        this.mSMSVerificationCodeEditText = (CustomEditGroup) this.findViewById(R.id.register_sms_verification_code);
        // 密码
        this.mPasswordEditText = (CustomEditGroup) this.findViewById(R.id.register_password_EditText);
        // 提交
        this.mRegisterSubmitImgView = (Button) this.findViewById(R.id.register_submit);
        // 获得验证码
        this.button_timer = (CustomTimerButton) this.findViewById(R.id.button_timer);
        // 使用协议
        this.mAgreementTextView = (TextView) this.findViewById(R.id.register_agreement);

        // 注册事件
        this.mRegisterSubmitImgView.setOnClickListener(this);
        this.mAgreementTextView.setOnClickListener(this);
        this.toolbar.setOnClickCuteToolbarListener(this);
        this.button_timer.setTimerListener(this);

        getSMSCodeFlg = false;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_agreement:
                break;
            case R.id.register_submit:
                // 提交
                String number = mUserNameEditText.getContent();
                String code = mSMSVerificationCodeEditText.getContent();
                String password = mPasswordEditText.getContent();
                if (checkInput(number, code,password)) {
                    mRegisterSubmitImgView.setEnabled(false);
                    doRegister(number, code, InnovationAlgorithm.SHA1(Config.SECURITY_CODE, password));
                }else {
                    mRegisterSubmitImgView.setEnabled(true);
                }
                break;
        }
    }

    private void doReturn() {
        if (getSMSCodeFlg) {
            Dialog dialog = new AlertDialog.Builder(this).setTitle("提示").setMessage("验证码已发送，是否放弃当前操作？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ZnkActivityUtil.finishActivity();
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();

            dialog.show();
        } else {
            ZnkActivityUtil.finishActivity();
        }
    }

    /**
     * 注册
     */
    private void doRegister(final String userNameStr,final String verificationCodeStr, final String tagPassword) {
        Client.requestRegPhone(this, userNameStr, verificationCodeStr, tagPassword,
                new ResponseHandler() {
                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        RegPhoneResponse regPhoneResponse = get(value.toString(), RegPhoneResponse.class);
                        // 全局信息
                        GlobalBean globalBean = new GlobalBean();
                        globalBean.setPhoneNum(userNameStr);
                        globalBean.setHguid(regPhoneResponse.getId());
                        globalBean.setToken(regPhoneResponse.getToken());
                        Config.setGlobal(RegisterActivity.this, new Gson().toJson(globalBean));
                        // 登录成功，保存用户信息
                        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.putString("username", userNameStr);
                        editor.putString("password", tagPassword);
                        try {
                            editor.putString("nowDateTime", DateUtil.getNowDateTime());
                        } catch (ParseException e) {
                            editor.putString("nowDateTime", "2000/01/01 00:00:00");
                        }
                        editor.putBoolean("loginFlag", true);
                        editor.commit();

                        ZnkActivityUtil.startActivity(DeviceActivity.class);
                        ZnkActivityUtil.finishActivity();
                    }

                    @Override
                    public void onInnovationFinish() {
                        super.onInnovationFinish();
                        mRegisterSubmitImgView.setEnabled(true);
                    }

                    @Override
                    public void onInnovationExceptionFinish() {
                        super.onInnovationExceptionFinish();
                        mRegisterSubmitImgView.setEnabled(true);
                    }

                }, new ZNKNetWorkUnavialableListener());

    }

    /**
     * 注册前检查
     *
     * @return true:正常 false:异常
     */
    private boolean checkInput(String number, String code,String password ) {

        if (!checkNumber(number)) {
            return false;
        }
        if (TextUtils.isEmpty(code)) {
            AnimationConfig.shake(this, mSMSVerificationCodeEditText);
            tipErrorShow(R.string.tip_error_code_empty);
            return false;
        }
        // 密码是否为空
        if (TextUtils.isEmpty(password)) {
            AnimationConfig.shake(this, mPasswordEditText);
            tipErrorShow(R.string.tip_error_password_need);
            return false;
        }
        // 密码是否长度为6-16不包含空格
        if (password.length() < 6 ||password.length() > 16 ||password.contains(" ")) {
            AnimationConfig.shake(this, mPasswordEditText);
            tipErrorShow(R.string.tip_error_code_empty);
            ZnkActivityUtil.showSimpleDialog(this.getString(R.string.tip_error_password));
            return false;
        }

        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ZnkActivityUtil.setVisibility(v.getId(), hasFocus, clearMap);
    }

    /**
     * 获得验证码
     */
    private void requestDynamicLoginGetCode(final String userNameStr) {
        // 当未连接网络,或网络连接超时的情况发生时,重置计时器.
        Client.requestVerifyPhone(this, userNameStr,
                new ResponseHandler() {
                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        stopTime();
                        getSMSCodeFlg = true;
                    }

                    @Override
                    public void onZNKFailure(String value) {
                        super.onZNKFailure(value);
                        stopTime();
                        getSMSCodeFlg = true;
                    }

                }, new ZNKNetWorkUnavialableListener());

    }

    @Override
    public void onClickLeft() {
        // 返回
        doReturn();
    }

    @Override
    public void onClickRight() {

    }

    @Override
    public void timerStart(View v, int startCount) {
        if (v.getId() == R.id.button_timer) {
            button_timer.setText(startCount + second);
            String code = mUserNameEditText.getContent();
            if (checkNumber(code)) {
                requestDynamicLoginGetCode(code);
            } else {
                stopTime();
            }
        }
    }

    @Override
    public void timerCount(View v, int currentCount) {
        if (v.getId() == R.id.button_timer) {
            button_timer.setText(currentCount + second);
        }
    }

    @Override
    public void timerEnd(View v) {
        if (v.getId() == R.id.button_timer) {
            button_timer.setText("获取验证码");
        }
    }

    private boolean checkNumber(String number) {
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
        return true;
    }

    public void stopTime() {
        button_timer.stopThread();
    }
}
