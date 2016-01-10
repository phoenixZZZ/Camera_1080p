package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.TextView;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;

public class NoneSetActivity extends BaseActivity implements View.OnClickListener ,
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    private TextView text_hint =null;

    private TextView setting =null;

    private String scanResult;

    private SpannableString msp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_none_set);

        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        text_hint = (TextView)this.findViewById(R.id.text_01);
        setting= (TextView)this.findViewById(R.id.setting);

        toolbar.setOnClickCuteToolbarListener(this);
        setting.setOnClickListener(this);
        //创建一个 SpannableString对象
        msp = new SpannableString("您可以点击“设置”按钮直接跳转\n至网络连接界面");
        msp.setSpan(new AbsoluteSizeSpan(24), 5, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text_hint.setText(msp.toString());

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClickLeft() {
        ZnkActivityUtil.finishActivity();
    }

    @Override
    public void onClickRight() {


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting:
                //无线和网络设置
                Intent mIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                //ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
                //mIntent.setComponent(comp);
                //mIntent.setAction("android.intent.action.VIEW");
                startActivityForResult(mIntent, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dialog dialog = new AlertDialog.Builder(this).setTitle("").setMessage("是否完成设置？")
                .setPositiveButton("完成",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(NoneSetActivity.this.getApplicationContext(),
                                        RtspActivity.class);
                                intent.putExtra("name", TAG_CLASS_NAME);
                                intent.putExtra("LoginType", 0);
                                startActivity(intent);
                                ZnkActivityUtil.finishActivity();
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