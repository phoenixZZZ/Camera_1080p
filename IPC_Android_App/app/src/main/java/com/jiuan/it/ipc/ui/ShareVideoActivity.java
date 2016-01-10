package com.jiuan.it.ipc.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;
import android.widget.MediaController;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.share.Client;
import com.jiuan.it.ipc.http.share.ExampleClient;
import com.jiuan.it.ipc.http.share.ResponseHandler;
import com.jiuan.it.ipc.model.ShareModel;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShareVideoActivity extends BaseActivity implements
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    private EditText  edit =null;

    private CheckBox check =null;

    private VideoView videoView =null;

    public  SimpleDateFormat fmt;

    private NetworkUnavailableListenerImpl mNetworkUnavailableListenerImpl;

    private MediaController mController;

    private String path = "";

    /** 微信分享 */
    private IWXAPI wxApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_video_share);

        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        edit = (EditText)this.findViewById(R.id.editText);
        check = (CheckBox)this.findViewById(R.id.checkBox);
        videoView = (VideoView)this.findViewById(R.id.videoView);
        toolbar.setOnClickCuteToolbarListener(this);


        wxApi = WXAPIFactory.createWXAPI(this, Config.AppID);
        wxApi.registerApp(Config.AppID);

        mNetworkUnavailableListenerImpl = new NetworkUnavailableListenerImpl();
        fmt=new SimpleDateFormat("yyyyMMddHHmmss");
        path = getIntent().getStringExtra("PATH");

        File file = new File(path);
        if (file.exists()) {
            mController = new MediaController(this);
            videoView.setVideoPath(file.getAbsolutePath()); //①
            // 设置videoView与mController建立关联
            videoView.setMediaController(mController); //②
            // 设置mController与videoView建立关联
            mController.setMediaPlayer(videoView); //③
            // 让VideoView获取焦点
            videoView.requestFocus();
        }
        if(Config.getLoginInfo(this)==null){
            toolbar.setTextColorRight(Color.GRAY);
        }
    }


    @Override
    public void onClickLeft() {
        ZnkActivityUtil.finishActivity();
    }

    @Override
    public void onClickRight() {
        if(Config.getLoginInfo(this)==null){
            return;
        }
        File share = new File(path);
        String userName = Config.getLoginInfo(ShareVideoActivity.this).getUsername();
        int shareType = 0;
        String uploadToken = fmt.format(new Date());
        String desc= edit.getText().toString();
        String param= "video";
        if(check.isChecked()){
           String  videoPath = Tools.createFile(Config.VIDEO, null, null, null, this);
           videoPath = videoPath+ File.separator + fmt.format(new Date()) + ".mp4";
            try {
                Tools.copyFile(path,videoPath,false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(userName==null){
            tipErrorShow("匿名登录无法分享");
            return;
        }
        ExampleClient.INSTANCE.requestUploadFile(this, share, userName, shareType, uploadToken, desc, param, new ResponseHandler() {
                    @Override
                    public void onCustomStart() {
                        showProgressDialog("提示","视频分享",30*1000);
                        Log.e(TAG_CLASS_NAME, "开始");
                    }

                    @Override
                    public void onCustomSuccess(JsonElement value) {
                        super.onCustomSuccess(value);
                        ShareModel model = get(value.toString(), ShareModel.class);
                        dismissProgressDialog();
                        wechatShare(1,model.getShareUrl());
                        Log.e(TAG_CLASS_NAME, "完成" + model.getShareUrl());
                    }

                    @Override
                    public void onFailure(String value) {
                        super.onFailure(value);
                        dismissProgressDialog();
                        tipErrorShow(value);

                    }

                    @Override
                    public void onCustomExceptionFinish() {
                        super.onCustomExceptionFinish();
                        dismissProgressDialog();
                    }
                },
                getNetworkUnavailableListenerImpl());


    }

    /**
     * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码）
     * @param flag(0:分享到微信好友，1：分享到微信朋友圈)
     */
    private void wechatShare(int flag,String url){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url ;//"这里填写链接url";
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = edit.getText().toString();//"分享视频";
        //msg.description = edit.getText().toString();
        //这里替换一张自己工程里的图片资源
        Bitmap thumb = BitmapFactory.decodeResource(getResources(), R.drawable.icon_logo);
        msg.setThumbImage(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag==0? SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
    }

    protected final Client.NetworkUnavailableListener getNetworkUnavailableListenerImpl() {
        return mNetworkUnavailableListenerImpl;
    }

    private class NetworkUnavailableListenerImpl implements Client.NetworkUnavailableListener {

        @Override
        public void networkUnavailable() {
            tipErrorShow(R.string.tip_network_unavailable);
        }
    }
}