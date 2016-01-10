package com.jiuan.it.ipc.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import com.google.gson.JsonElement;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.tools.BitmapUtils;
import com.jiuan.it.ipc.tools.HttpUtils;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class CheckImageActivity extends BaseActivity implements
        CustomToolbar.OnClickCustomToolbarListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    private Gallery mGallery;

    private String typeCode;

    private List<String> mBean = new ArrayList<String>();

    private String[] imageUrl = new String[8];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_image);

        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        mGallery  = (Gallery) findViewById(R.id.mGallery);

        toolbar.setOnClickCuteToolbarListener(this);

        imageUrl = getIntent().getStringArrayExtra("FileList");
        typeCode = getIntent().getStringExtra("typeCode");
        mGallery.setAdapter(new ImageAdapter(CheckImageActivity.this, imageUrl));
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
        downloadImage(Config.DEVICE_CODE, typeCode);
    }

    private class ImageAdapter extends BaseAdapter {

        private  String[] IMAGE_URLS = null  ;

        private LayoutInflater inflater;

        private DisplayImageOptions options;

        ImageAdapter(Context context,String[] imageIDResults) {
            inflater = LayoutInflater.from(context);
            IMAGE_URLS = imageIDResults;
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(20))
                    .build();
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = (ImageView) convertView;
            if (imageView == null) {
                imageView = (ImageView) inflater.inflate(R.layout.item_gallery_image, parent, false);
            }
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(ImageLoaderConfiguration.createDefault(CheckImageActivity.this));
            imageLoader.displayImage(IMAGE_URLS[position%IMAGE_URLS.length], imageView, options);
            return imageView;
        }
    }

    /**
     * 下载全景图片
     */
    private void downloadImage(final String deviceID,final String typeCode) {
        // 调用用户绑定白盒子API
        Client.requestMergedPic(this, Config.getGlobal(this).getHguid(),  Config.getGlobal(this).getToken().getAccessToken(), deviceID, typeCode,
                new ResponseHandler() {

                    @Override
                    public void onInnovationStart() {
                        super.onInnovationStart();
                        showProgressDialog("提示","下载中",30*1000);
                    }

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        String response = get(value.toString(), String.class);
                        DownloadImageTask task = new DownloadImageTask();
                        task.execute(response);
                    }

                    @Override
                    public void onZNKFailure(String value) {
                        ZnkActivityUtil.showSimpleDialog("提示", value);
                        dismissProgressDialog();
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        showTokenFailure(value);
                        dismissProgressDialog();
                    }

                }, new ZNKNetWorkUnavialableListener());
    }

    class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
            Bitmap mDownloadImage = HttpUtils.getNetWorkBitmap(params[0]);
            return mDownloadImage;
        }

        // 下载完成回调
        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            dismissProgressDialog();
            BitmapUtils myBitmap = new BitmapUtils();
            String imagePath = Tools.createFile(Config.PANORAMA, null, null, null, CheckImageActivity.this);
            imagePath = imagePath+ File.separator +fmt.format(new Date()) + ".jpg";
            try {
                myBitmap.saveBitmap(result,imagePath);
                tipErrorShow("图片下载成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 更新进度回调
        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }
}
