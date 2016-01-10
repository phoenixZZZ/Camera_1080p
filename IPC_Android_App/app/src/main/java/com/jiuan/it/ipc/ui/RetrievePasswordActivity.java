package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.ui.animation.AnimationConfig;
import com.jiuan.it.ipc.ui.widget.CustomEditGroup;
import com.jiuan.it.ipc.ui.widget.CustomTimerButton;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.jiuan.oa.android.library.util.MobileNumberUtil;

public class RetrievePasswordActivity extends BaseActivity implements View.OnClickListener,
        CustomToolbar.OnClickCustomToolbarListener, CustomTimerButton.TimerListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    // 手机号
    private CustomEditGroup mPhoneNo;

    // 获取验证码
    private CustomTimerButton button_timer;

    // 验证码
    private CustomEditGroup mSmsVerificationCode;

    // 下一步按钮
    private Button mNextStep;

    // 获取验证码标志
    private boolean getSMSCodeFlg;

    private String second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        second = getString(R.string.button_timer);
        // 初期加载画面显示
        setContentView(R.layout.layout_retrieve_password);

        // 获取页面控件信息
        toolbar= (CustomToolbar)this.findViewById(R.id.toolbar);// 标题栏
        mPhoneNo = (CustomEditGroup) findViewById(R.id.retrieve_userNameEditText);
        button_timer = (CustomTimerButton) findViewById(R.id.button_timer);
        mSmsVerificationCode = (CustomEditGroup) findViewById(R.id.retrieve_sms_verification_code_EditText);
        mNextStep = (Button) findViewById(R.id.btn_next);

        // 注册点击事件
        this.toolbar.setOnClickCuteToolbarListener(this);
        this.button_timer.setTimerListener(this);
        this.mNextStep.setOnClickListener(this);

        getSMSCodeFlg = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                // 下一步
                String number = mPhoneNo.getContent();
                String code = mSmsVerificationCode.getContent();
                if (checkInput(number, code)) {
                    mNextStep.setEnabled(false);
                    doNextStep(number, code);
                }else {
                    mNextStep.setEnabled(true);
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
     * 获得验证码
     */
    private void requestDynamicLoginGetCode(final String userNameStr) {
        // 当未连接网络,或网络连接超时的情况发生时,重置计时器.
        Client.requestVerifyReset(this, userNameStr,
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


    /**
     * 验证验证码并跳转到下一步
     */
    private void doNextStep(final String phone,final String code) {
        Client.requestVerifyResetCode(this, phone, code,
                new ResponseHandler() {
                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        // 获取返回状态
                        Integer returnValue = get(value.toString(), Integer.class);
                        if (returnValue != 1) {
                            ZnkActivityUtil.showSimpleDialog(
                                    RetrievePasswordActivity.this.getString(R.string.warn_message_109));
                        } else {
                            // 跳转到修改密码UI
                            Intent intent = new Intent(RetrievePasswordActivity.this.getApplicationContext(),
                                    ResetPasswordActivity.class);
                            intent.putExtra("name", TAG_CLASS_NAME);
                            intent.putExtra("phoneNo", phone);
                            intent.putExtra("verificationCode",code);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onInnovationFinish() {
                        super.onInnovationFinish();
                        mNextStep.setEnabled(true);
                    }

                    @Override
                    public void onInnovationExceptionFinish() {
                        super.onInnovationExceptionFinish();
                        mNextStep.setEnabled(true);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

    /**
     * 获取验证码之前的检查
     * @return true:通过 false:不通过
     */
    private boolean checkInput(String number, String code) {
        if (!checkNumber(number)) {
            return false;
        }
        if (TextUtils.isEmpty(code)) {
            AnimationConfig.shake(this, mSmsVerificationCode);
            tipErrorShow(R.string.tip_error_code_empty);
            return false;
        }
        return true;
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
            String code = mPhoneNo.getContent();
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
            AnimationConfig.shake(this, mPhoneNo);
            tipErrorShow(R.string.tip_number_error);
            return false;
        }
        if (number.length()<11) {
            AnimationConfig.shake(this, mPhoneNo);
            tipErrorShow(R.string.tip_error_number);
            return false;
        }
        return true;
    }

    public void stopTime() {
        button_timer.stopThread();
    }
}