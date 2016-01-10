package com.jiuan.it.ipc.http;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.gson.JsonElement;
import com.innovation.android.library.http.InnovationHttpResponseHandler;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.response.RefreshTokenResponse;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.ui.LoginActivity;

public class ResponseHandler extends InnovationHttpResponseHandler {

    // 请求缺少必须参数
    private static final String FAILURE_2001 = "2001";

    // Sc或Sc未授权
    private static final String FAILURE_2002 = "2002";

    // 手机号被占用
    private static final String FAILURE_2003 = "2003";

    // 错误的手机号格式
    private static final String FAILURE_2004 = "2004";

    // 发送短信失败
    private static final String FAILURE_2005 = "2005";

    // 错误的手机注册验证码
    private static final String FAILURE_2006 = "2006";

    // 用户不存在或者密码错误
    private static final String FAILURE_2007 = "2007";

    // 添加的好友不存在
    private static final String FAILURE_2007_1 = "2007.1";

    // 不能添加自己为好友
    private static final String FAILURE_2007_2 = "2007.2";

    // 您已添加此好友
    private static final String FAILURE_2007_3 = "2007.3";

    // 请先添加好友
    private static final String FAILURE_2007_4 = "2007.4";

    // 手机号未被注册
    private static final String FAILURE_2008 = "2008";

    // 错误的重置密码验证码
    private static final String FAILURE_2009 = "2009";

    // 访问令牌错误
    private static final String FAILURE_2010_1 = "2010.1";

    // 访问令牌过期
    private static final String FAILURE_2010_2 = "2010.2";

    // 刷新令牌错误
    private static final String FAILURE_2010_3 = "2010.3";

    // 未注册的手机号或登录动态码错误
    private static final String FAILURE_2011 = "2011";

    // 用户反馈错误(文字内容超长)
    private static final String FAILURE_2012_1 = "2012.1";

    // 用户反馈错误(图片大小超过范围)
    private static final String FAILURE_2012_2 = "2012.2";

    // 用户反馈错误(图片格式不支持)
    private static final String FAILURE_2012_3 = "2012.3";

    // 密码错误(密码长度必需为6-16位非空字符)
    private static final String FAILURE_2020_1 = "2020.1";

    // 密码错误(新旧密码不能相同)
    private static final String FAILURE_2020_2 = "2020.2";

    // 设备关联错误(您已添加过该设备)
    private static final String FAILURE_2030_1 = "2030.1";

    // 设备关联错误(设备已被其他人添加)
    private static final String FAILURE_2030_2 = "2030.2";

    // 设备关联错误(设备不存在)
    private static final String FAILURE_2030_3 = "2030.3";

    // 设备关联错误(您不是设备的创建者)
    private static final String FAILURE_2030_4 = "2030.4";

    // 设备关联错误(不存在与设备的关联关系)
    private static final String FAILURE_2030_5 = "2030.5";

    // 您名下暂无设备信息
    private static final String FAILURE_2030_6 = "2030.6";

    // 分享失败(您已向该好友发出过邀请)
    private static final String FAILURE_2031_1 = "2031.1";

    // 分享失败(没有此分享邀请或已失效)
    private static final String FAILURE_2031_2 = "2031.2";

    // 分享失败(不能向自己发出邀请)
    private static final String FAILURE_2031_3 = "2031.3";

    // 分享失败(发送的“拒绝”或“接受”无效)
    private static final String FAILURE_2031_4 = "2031.4";

    // 该设备圈已存在
    private static final String FAILURE_2040 = "2040";

    // 您不是设备圈的创建者，不能修改此设备圈
    private static final String FAILURE_2040_1 = "2040.1";

    // 该设备圈不存在
    private static final String FAILURE_2040_2 = "2040.2";

    // 您的设备圈下无此设备
    private static final String FAILURE_2040_3 = "2040.3";

    // 您没有权限控制此设备圈
    private static final String FAILURE_2040_4 = "2040.4";

    // 该消息已过期
    private static final String FAILURE_2041 = "2041";

    // 默认权限未初始化
    private static final String FAILURE_2042 = "2042";

    // 您没有修改设备圈的权限
    private static final String FAILURE_2042_1 = "2042.1";

    // 数据初始化错误
    private static final String FAILURE_2042_2 = "2042.2";

    // 白盒子未初始化
    private static final String FAILURE_2043 = "2043";

    // 白盒子已绑定
    private static final String FAILURE_2043_1 = "2043.1";

    // 消息发送失败
    private static final String FAILURE_2044 = "2044";

    // 未找到该类型文件
    private static final String FAILURE_2050= "2050";

    // 服务器内部错误
    private static final String FAILURE_5000 = "2050";


    @Override
    public void onInnovationFailure(String msg) {
//        overBlockDiaog();
        if(msg.equals(FAILURE_2001)) {
            onZNKFailure("请求缺少必须参数");
        } else if (msg.equals(FAILURE_2002)) {
            onZNKFailure("Sc或Sc未授权");
        } else if (msg.equals(FAILURE_2003)) {
            onZNKFailure("手机号被占用");
        } else if (msg.equals(FAILURE_2004)) {
            onZNKFailure("错误的手机号格式");
        } else if (msg.equals(FAILURE_2005)) {
            onZNKFailure("发送短信失败");
        } else if (msg.equals(FAILURE_2006)) {
            // 错误的手机注册验证码
            onZNKFailure("您输入的手机注册验证码有误");
        } else if (msg.equals(FAILURE_2007)) {
            onZNKFailure("用户不存在或者密码错误");
        } else if (msg.equals(FAILURE_2007_1)) {
            onZNKFailure("添加的好友不存在");
        } else if (msg.equals(FAILURE_2007_2)) {
            onZNKFailure("不能添加自己为好友");
        } else if (msg.equals(FAILURE_2007_3)) {
            onZNKFailure("您已添加此好友");
        } else if (msg.equals(FAILURE_2007_4)) {
            onZNKFailure("请先添加好友");
        } else if (msg.equals(FAILURE_2008)) {
            onZNKFailure("手机号未被注册");
        } else if (msg.equals(FAILURE_2009)) {
            // 错误的重置密码验证码
            onZNKFailure("您输入的重置密码验证码有误");
        } else if (msg.equals(FAILURE_2010_1)) {
            onZNKTokenFailure("访问令牌错误");
        } else if (msg.equals(FAILURE_2010_2)) {
            onZNKTokenFailure("访问令牌过期");
        } else if (msg.equals(FAILURE_2010_3)) {
            onZNKTokenFailure("刷新令牌错误");
        } else if (msg.equals(FAILURE_2011)) {
            onZNKFailure("未注册的手机号或登录动态码错误");
        } else if (msg.equals(FAILURE_2012_1)) {
            onZNKFailure("文字内容超长");
        } else if (msg.equals(FAILURE_2012_2)) {
            onZNKFailure("图片大小超过范围");
        } else if (msg.equals(FAILURE_2012_3)) {
            onZNKFailure("图片格式不支持");
        } else if (msg.equals(FAILURE_2020_1)) {
            onZNKFailure("密码长度必需为6-16位非空字符");
        } else if (msg.equals(FAILURE_2020_2)) {
            onZNKFailure("新旧密码不能相同");
        } else if (msg.equals(FAILURE_2030_1)) {
            onZNKFailure("您已添加过该设备");
        } else if (msg.equals(FAILURE_2030_2)) {
            onZNKFailure("设备已被其他人添加");
        } else if (msg.equals(FAILURE_2030_3)) {
            onZNKFailure("设备不存在");
        } else if (msg.equals(FAILURE_2030_4)) {
            onZNKFailure("您不是设备的创建者");
        } else if (msg.equals(FAILURE_2030_5)) {
            onZNKFailure("不存在与设备的关联关系");
        } else if (msg.equals(FAILURE_2030_6)) {
            onZNKFailure("您名下暂无设备信息");
        } else if (msg.equals(FAILURE_2031_1)) {
            onZNKFailure("您已向该好友发出过邀请");
        } else if (msg.equals(FAILURE_2031_2)) {
            onZNKFailure("没有此分享邀请或已失效");
        } else if (msg.equals(FAILURE_2031_3)) {
            onZNKFailure("不能向自己发出邀请");
        } else if (msg.equals(FAILURE_2031_4)) {
            onZNKFailure("发送的“拒绝”或“接受”无效");
        } else if (msg.equals(FAILURE_2040)) {
            onZNKFailure("该设备圈已存在");
        } else if (msg.equals(FAILURE_2040_1)) {
            onZNKFailure("您不是设备圈的创建者，不能修改此设备圈");
        } else if (msg.equals(FAILURE_2040_2)) {
            onZNKFailure("该设备圈不存在");
        } else if (msg.equals(FAILURE_2040_3)) {
            onZNKFailure("您的设备圈下无此设备");
        } else if (msg.equals(FAILURE_2040_4)) {
            onZNKFailure("您没有权限控制此设备圈");
        } else if (msg.equals(FAILURE_2041)) {
            onZNKFailure("该消息已过期");
        } else if (msg.equals(FAILURE_2042)) {
            onZNKFailure("默认权限未初始化");
        } else if (msg.equals(FAILURE_2042_1)) {
            onZNKFailure("您没有修改设备圈的权限");
        } else if (msg.equals(FAILURE_2042_2)) {
            onZNKFailure("数据初始化错误");
        } else if (msg.equals(FAILURE_2043)) {
            onZNKFailure("白盒不存在");
        } else if (msg.equals(FAILURE_2043_1)) {
            onZNKFailure("白盒子已绑定");
        } else if (msg.equals(FAILURE_2044)) {
            onZNKFailure("消息发送失败");
        } else if (msg.equals(FAILURE_2050)) {
            onZNKFailure("未找到该类型文件");
        }else if (msg.equals(FAILURE_5000)) {
            onZNKFailure("服务器内部错误");
        }
    }

    @Override
    public void onStart() {
//        startBlockDiaog();
        super.onStart();
    }
    /**
     * 正常结束会调用此方法
     */
    public void onInnovationFinish() {
        super.onInnovationFinish();
        ZnkActivityUtil.overBlockDialog();
    }

    @Override
    public void onInnovationError() {
//        overBlockDiaog();
        onZNKFailure("服务器内部错误");
    }

    @Override
    public void onInnovationExceptionFinish() {
//        overBlockDiaog();
        onZNKFailure("网络连接超时");
    }

    /**
     * 普通请求失败
     * @param value 失败信息
     */
    public void onZNKFailure(String value) {
        Log.d("onZNKFailure", value);
        ZnkActivityUtil.overBlockDialog();
        ZnkActivityUtil.showSimpleDialog(value);
    }

    /**
     * 令牌相关请求失败
     * @param msg 失败信息
     */
    public void onZNKTokenFailure(String msg) {
        Log.d("onZNKTokenFailure", msg);

        if (TextUtils.equals(msg, "访问令牌错误")
                && System.currentTimeMillis() < Config.getGlobal(ZnkActivityUtil.getContext()).getToken().getAccessExpire()) {

            Dialog dialog = new AlertDialog.Builder(ZnkActivityUtil.getContext()).setTitle("提示")
                    .setMessage("您的账号在其他设备上登录了，请您重新登录").setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent =  new Intent(ZnkActivityUtil.getContext(),LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    ZnkActivityUtil.getContext().startActivity(intent);
                                    ZnkActivityUtil.finishActivity();
                                    dialog.dismiss();
                                }
                            }).create();
            // 显示
            dialog.show();

        } else {
            // 调用刷新令牌API
            Client.requestRefreshToken(ZnkActivityUtil.getContext(), Config.getGlobal(ZnkActivityUtil.getContext()).getHguid(), Config.getGlobal(ZnkActivityUtil.getContext()).getToken().getRefreshToken(),
                    new ResponseHandler() {
                        @Override
                        public void onInnovationSuccess(JsonElement value) {
                            super.onInnovationSuccess(value);
                            // 调用获取用户所有设备信息API
                            RefreshTokenResponse refreshTokenResponse = get(value.toString(), RefreshTokenResponse.class);

                            if (refreshTokenResponse != null && refreshTokenResponse.getToken() != null) {
                                // 更新Token
                                Config.getGlobal(ZnkActivityUtil.getContext()).setToken(refreshTokenResponse.getToken());

                            } else {
                                Toast.makeText(ZnkActivityUtil.getContext(), "更新令牌失败", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, new ZNKNetWorkUnavialableListener());

        }

    }
}
