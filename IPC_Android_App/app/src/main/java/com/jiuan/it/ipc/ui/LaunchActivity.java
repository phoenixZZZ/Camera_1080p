package com.jiuan.it.ipc.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.DateUtil;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.PhoneLoginResponse;
import com.jiuan.it.ipc.model.AppVersion;
import com.jiuan.it.ipc.model.GlobalBean;
import com.jiuan.it.ipc.model.LoginInfo;
import com.jiuan.it.ipc.tools.AppUtils;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import org.apache.http.Header;
import java.io.File;
import java.text.ParseException;
import im.fir.sdk.FIR;
import im.fir.sdk.VersionCheckCallback;

public class LaunchActivity extends BaseActivity {

    private ProgressDialog progressDialog;

    private AppVersion appVersion;

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setupLayout();
        checkUpdate();

    }
    private void setupLayout() {
        setContentView(R.layout.layout_launch);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void doLogin()  {

        //检查本地是否已经登录，如果登录取出登录名和密码，发送登录请求，
       // SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        LoginInfo login = Config.getLoginInfo(LaunchActivity.this);
        if (login != null) {
            final String userNameStr = login.getUsername();
            final String password = login.getPassword();
            final String nowDateTime = login.getNowDateTime();
            boolean loginFlag = login.isLoginFlag();

            // 上次通过登录或注册登录
            if (!TextUtils.isEmpty(userNameStr)
                    && !TextUtils.isEmpty(password)
                    && !TextUtils.isEmpty(nowDateTime)
                    && loginFlag) {
                try {
                    if (nowDateTime.compareTo(DateUtil.getNow7DateTimeBefore()) > 0) {

                        Client.requestPhoneLogin(LaunchActivity.this, userNameStr, password,
                                new ResponseHandler() {
                                    @Override
                                    public void onInnovationSuccess(JsonElement value) {

                                        // 调用手机账号登录API
                                        PhoneLoginResponse phoneLoginResponse = get(value.toString(), PhoneLoginResponse.class);

                                        if (phoneLoginResponse != null) {

                                            // 全局信息
                                            GlobalBean globalBean = new GlobalBean();
                                            globalBean.setPhoneNum(userNameStr);
                                            globalBean.setHguid(phoneLoginResponse.getId());
                                            globalBean.setToken(phoneLoginResponse.getToken());
                                            Config.setGlobal(LaunchActivity.this, new Gson().toJson(globalBean));
                                            // 我的设备画面
                                            ZnkActivityUtil.startActivity(DeviceActivity.class);
                                            ZnkActivityUtil.finishActivity();
                                        }
                                    }

                                    @Override
                                    public void onZNKFailure(String value) {
                                        // 登录画面
                                        ZnkActivityUtil.startActivity(DeviceActivity.class);
                                        ZnkActivityUtil.finishActivity();
                                    }

                                    @Override
                                    public void onZNKTokenFailure(String value) {
                                        // 登录画面
                                        ZnkActivityUtil.startActivity(DeviceActivity.class);
                                        ZnkActivityUtil.finishActivity();
                                    }

                                }, new ZNKNetWorkUnavialableListener());

                    } else {
                        // 登录画面
                        ZnkActivityUtil.startActivity(DeviceActivity.class);
                        ZnkActivityUtil.finishActivity();
                    }

                } catch (ParseException e) {
                    // 登录画面
                    ZnkActivityUtil.startActivity(DeviceActivity.class);
                    ZnkActivityUtil.finishActivity();
                }

            } else {
                // 登录画面
                ZnkActivityUtil.startActivity(LoginActivity.class);
                ZnkActivityUtil.finishActivity();
            }
        } else {
            // 登录画面
            ZnkActivityUtil.startActivity(LoginActivity.class);
            ZnkActivityUtil.finishActivity();
        }

    }


    private void checkUpdate() {
        FIR.checkForUpdateInFIR(Config.FIR_TOKEN, new VersionCheckCallback() {
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                appVersion = get(s.toString(), AppVersion.class);
                if (TextUtils.equals(String.valueOf(AppUtils.getVersionCode(LaunchActivity.this))
                        ,appVersion.getVersion())) {
                    // todo 未检测到升级后的处理
                    doLogin();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openDialog(appVersion);
                        }
                    }, 0);

                }
            }

            @Override
            public void onFail(Exception e) {
                super.onFail(e);
                doLogin();
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });

    }

    private void openDialog(final AppVersion appVersion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
        builder.setTitle("下载最新版本" + appVersion.getName());
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // todo 不升级
                doLogin();
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                download(appVersion.getInstallUrl(), appVersion.getName());
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void download(String url, String name) {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
        // todo 改成自己的下载
        Client.INSTANCE.downloadFile(this, url, new FileAsyncHttpResponseHandler(file) {

            @Override
            public void onStart() {
                progressDialog.show();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                int progress = (int) (bytesWritten * 1.0f / totalSize * 100);
                progressDialog.setProgress(progress);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                progressDialog.dismiss();
                install(file);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Toast.makeText(LaunchActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }

        }, new Client.NetworkUnavailableListener() {
            @Override
            public void networkUnavailable() {
                Toast.makeText(LaunchActivity.this, "网络连接不可用", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void install(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    /**
     * 把Json格式的字符串转换成实体类型的方法.
     * 注意:实体类中的变量需要用@SerializedName注释
     */
    public final <T> T get(String value, Class<T> classOfT) {
        try {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            return gson.fromJson(value, classOfT);
        } catch (JsonSyntaxException exception) {
            Log.d("JsonSyntaxException", exception.getMessage());
        }
        return null;
    }
}