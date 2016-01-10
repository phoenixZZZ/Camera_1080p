package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.GlobalBean;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.CodeLoginResponse;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.ui.animation.AnimationConfig;
import com.jiuan.it.ipc.ui.widget.CustomEditGroup;
import com.jiuan.it.ipc.ui.widget.CustomTimerButton;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.jiuan.oa.android.library.util.MobileNumberUtil;
import java.util.HashMap;
import java.util.Map;

public class DynamicLoginActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener
    , CustomToolbar.OnClickCustomToolbarListener, CustomTimerButton.TimerListener{

    private CustomToolbar toolbar =null;

    private CustomEditGroup mUserNameEditText = null;

    private CustomEditGroup mSMSVerificationCodeEditText = null;

    private CustomTimerButton button_timer = null;

    private Button mRegisterSubmitImgView = null;

    private Map clearMap = new HashMap<EditText,ImageView>();

    private boolean getSMSCodeFlg;

    private String second;
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
        setContentView(R.layout.dynamic_login);

        getSMSCodeFlg = false;

        // 标题栏
        this.toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);
        // 用户名
        this.mUserNameEditText = (CustomEditGroup) this.findViewById(R.id.ui_dynamic_login_userName);
        // 验证码
        this.mSMSVerificationCodeEditText = (CustomEditGroup) this.findViewById(R.id.ui_dynamic_login_sms_verification_code);
        // 提交
        this.mRegisterSubmitImgView = (Button) this.findViewById(R.id.ui_dynamic_login_submit);
        // 获得验证码
        this.button_timer = (CustomTimerButton) this.findViewById(R.id.button_timer);

        // 注册事件
        this.mRegisterSubmitImgView.setOnClickListener(this);
        this.toolbar.setOnClickCuteToolbarListener(this);
        this.button_timer.setTimerListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ui_dynamic_login_submit:
                // 提交
                String number = mUserNameEditText.getContent();
                String code = mSMSVerificationCodeEditText.getContent();
                if (checkInput(number, code)) {
                    mRegisterSubmitImgView.setEnabled(false);
                    doLogin(number, code);
                }else {
                    mRegisterSubmitImgView.setEnabled(true);
                }
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        ZnkActivityUtil.setVisibility(v.getId(), hasFocus,clearMap);
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
     * 登录
     */
    private void doLogin(final String number, final String code) {

        Client.requestCodeLogin(this, number, code,
                new ResponseHandler() {
                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        CodeLoginResponse codeLoginResponse = get(value.toString(), CodeLoginResponse.class);
                        // 全局信息
                        GlobalBean globalBean = new GlobalBean();
                        globalBean.setPhoneNum(number);
                        globalBean.setHguid(codeLoginResponse.getId());
                        globalBean.setToken(codeLoginResponse.getToken());
                        Config.setGlobal(DynamicLoginActivity.this, new Gson().toJson(globalBean));
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
     * 前检查
     *
     * @return true:正常 false:异常
     */
    /**
     * 注册前检查
     *
     * @return true:正常 false:异常
     */
    private boolean checkInput(String number, String code) {
        if (!checkNumber(number)) {
            return false;
        }
        if (TextUtils.isEmpty(code)) {
            AnimationConfig.shake(this, mSMSVerificationCodeEditText);
            tipErrorShow(R.string.tip_error_code_empty);
            return false;
        }
        return true;
    }

    /**
     * 获得验证码
     */
    private void doGetVerificationCode(final String userNameStr) {
        // 当未连接网络,或网络连接超时的情况发生时,重置计时器.
        Client.requestGetLoginCode(this, userNameStr,
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
                doGetVerificationCode(code);
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