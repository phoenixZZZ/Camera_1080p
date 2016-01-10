package com.jiuan.it.ipc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.tools.AppUtils;
import com.jiuan.it.ipc.tools.DensityUtils;
import com.jiuan.it.ipc.tools.DigitalTrans;
import com.jiuan.it.ipc.tools.BitmapUtils;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.ui.adapter.ListAdapter;
import com.jiuan.it.ipc.model.PreviewHeader;
import com.jiuan.it.ipc.model.PreviewReceiveModel;
import com.jiuan.it.ipc.model.VideoHeader;
import com.jiuan.it.ipc.model.VideoImage;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewListActivity extends PreviewActivity implements SwipeRefreshLayout.OnRefreshListener,
        CustomToolbar.OnClickCustomToolbarListener,ListAdapter.OnRecyclerViewItemClickListener{

    private ListAdapter mAdapter;

    private List<String> data ;

    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LinearLayoutManager layoutManager;

    private static final String TAG = "ListActivity";

    private ArrayList<PreviewHeader> headerData;

    private ArrayList<VideoHeader>  videoHeaders;

    private ArrayList<VideoImage>  VideoImageHeaders;

    private BitmapUtils myBitmap = new BitmapUtils();

    private boolean flag = false;

    private CustomToolbar toolbar =null;

    private int lastVisiblePosition;

    private String base ="000000000000";

    private  int itemHeight , listHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_list;
    }

    @Override
    protected void findViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ListAdapter(this,1000);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        TypedValue typed_value = new TypedValue();
        getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
        mAdapter.setOnItemClickListener(this);
        toolbar.setOnClickCuteToolbarListener(this);
    }

    @Override
    protected void init() {
        registerReceiver();
        //mServiceIntent = new Intent(this, BackService.class);
        startFtpServer();
        send.ctrlBase = base;
        send.ctrlName = null;
        send.ctrlNameStart = null;
        send.ctrlNameEnd = null;
        send.ctrlCount = getCount();
        send.sendData(Config.PREVIEWCTRL, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flag = false;
        unregisterReceiver();
        stopFtpServer();

    }

    @Override
    protected void widgetListener() {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && lastVisiblePosition + 1 == mAdapter.getItemCount()) {
                    if(!mSwipeRefreshLayout.isRefreshing()){
                        mSwipeRefreshLayout.setRefreshing(true);
                        send.ctrlBase = mAdapter.getList().get(lastVisiblePosition);
                        send.ctrlName = null;
                        send.ctrlNameStart = null;
                        send.ctrlNameEnd = null;
                        send.ctrlCount = getCount();
                        send.sendData(Config.PREVIEWCTRL, 1);
                    }
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                mSwipeRefreshLayout.setEnabled(layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
    }

    @Override
    protected void initGetData() {
        super.initGetData();
    }


    @Override
    public void onRefresh() {
        send.ctrlBase = base;
        send.ctrlName = null;
        send.ctrlNameStart = null;
        send.ctrlNameEnd = null;
        send.sendData(Config.PREVIEWCTRL, 0);
    }

    /**开始下拉刷新*/
    private void startSyncUI() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    /**停止下拉刷新*/
    private void stopSyncUI() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void updateAdapter(PreviewReceiveModel model) {
        super.updateAdapter(model);
        data=new ArrayList<String>();
        if(model.getReqType()>0){
            switch (model.getCtrlType()){
                case 0:
                    for (int i = 0; i < model.getReqType(); i++) {
                        data.add(model.getFileName()[i]);
                    }
                    mAdapter.clear();
                    mAdapter.setList(data);
                    break;
                case 1:
                    for (int i = 0; i < model.getReqType(); i++) {
                        mAdapter.addList(model.getFileName()[i]);
                    }
                    break;
            }
        }
        mAdapter.notifyDataSetChanged();
        stopSyncUI();
    }

    @Override
    protected void errorData() {
        super.errorData();
        if(mSwipeRefreshLayout.isRefreshing()){
            stopSyncUI();
        }
        dismissProgressDialog();
    }

    @Override
    protected void requestPreview(PreviewReceiveModel model) {
        super.requestPreview(model);
        ctrlName = model.getCtrlName();
        ctrlFileName = model.getCtrlFileName();
    }

    @Override
    protected void ftpPreview(PreviewReceiveModel model) {
        super.ftpPreview(model);
        headerData = new ArrayList<PreviewHeader>();
        videoHeaders = new ArrayList<VideoHeader>();
        VideoImageHeaders = new ArrayList<VideoImage>();
        //读取dat文件
        byte[] fileData = Tools.ReadSettings(PreviewListActivity.this, ftpPath+File.separator+ctrlName+File.separator+ctrlFileName);
        if(fileData.length>0) {
            String startNum = DigitalTrans.byte2hex(DigitalTrans.subBytes(fileData, 0, 8));
            flag =false;
            int init = 0;
            if("00000000FFFFFFFF".equals(startNum)){//有效文件开始
                flag = true;
                init = 8;
            }
            while (flag) {
                String endNum = "";
                if(init<fileData.length){
                     endNum = DigitalTrans.byte2hex(DigitalTrans.subBytes(fileData, init, 8));
                }else {
                    flag = false;
                    dismissProgressDialog();
                    ZnkActivityUtil.showSimpleDialog("文件无法打开");
                    break;
                }
                if("00000000FEFEFEFE".equals(endNum)){//有效文件结束
                    flag = false;
                    String CurrentFileName = "";
                    int iCurrentImages = 0;
                    for(int i = 0;i < headerData.size();i++){
                        if (CurrentFileName == "") {
                            CurrentFileName = headerData.get(i).getNamerec();
                            iCurrentImages++;
                        }
                        else if (CurrentFileName.equals(headerData.get(i).getNamerec()) != true)
                        {
                            Parcel parcel = Parcel.obtain();
                            VideoImage video = VideoImage.CREATOR.createFromParcel(parcel);
                            video.setStrVideoFileName(CurrentFileName);
                            video.setiImageCount(iCurrentImages);
                            VideoImageHeaders.add(video);

                            CurrentFileName = headerData.get(i).getNamerec();
                            iCurrentImages = 1;
                        }
                        else if (CurrentFileName.equals(headerData.get(i).getNamerec()) == true)
                        {
                            iCurrentImages++;
                        }
                    }
                    Parcel parcel = Parcel.obtain();
                    VideoImage video = VideoImage.CREATOR.createFromParcel(parcel);
                    video.setStrVideoFileName(CurrentFileName);
                    video.setiImageCount(iCurrentImages);
                    VideoImageHeaders.add(video);
                    // Add By Hjr 20151207 End


                    if(headerData.size()>0){
                        Intent intent = new Intent(PreviewListActivity.this, VideoCutActivity.class);
                        intent.putParcelableArrayListExtra(KF, headerData);
                        intent.putParcelableArrayListExtra(KS, videoHeaders);
                        intent.putParcelableArrayListExtra(KI, VideoImageHeaders);
                        startActivity(intent);
                    }else {
                         ZnkActivityUtil.showSimpleDialog("视频文件无图像");
                    }

                    dismissProgressDialog();
                    Log.e(TAG,"有效文件结束");
                    break;
                } else if("00000000FDFDFDFD".equals(endNum)){ //录像时长区
                    int num = DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 8, 4));
                    long overall = DigitalTrans.toInt(DigitalTrans.subBytes(fileData, init + 12, 8));
                    init = init + 20;
                    for (int i = 0; i < num; i++) {
                        Parcel parcel = Parcel.obtain();
                        VideoHeader video = VideoHeader.CREATOR.createFromParcel(parcel);
                        video.setVideoSum(num);
                        video.setOverall(overall);
                        video.setSeqNumFile(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init, 4)));
                        video.setNameFile(DigitalTrans.bytetoString(DigitalTrans.subBytes(fileData, init + 4, 32)));
                        video.setTimeFile(DigitalTrans.toInt(DigitalTrans.subBytes(fileData, init + 36, 8)));
                        videoHeaders.add(video);
                        init = init+44;
                    }
                    continue;
                }
                Parcel parcel = Parcel.obtain();
                PreviewHeader bean = PreviewHeader.CREATOR.createFromParcel(parcel);
                bean.setHeader(DigitalTrans.byte2hex(DigitalTrans.subBytes(fileData, init, 4)));
                bean.setPktSize(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 4, 4)));
                bean.setNamepre(DigitalTrans.bytetoString(DigitalTrans.subBytes(fileData, init + 8, 32)));
                bean.setNamerec(DigitalTrans.bytetoString(DigitalTrans.subBytes(fileData, init + 40, 32)));
                bean.setSeqNumpre(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 72, 4)));
                bean.setSeqNumrec(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 76, 4)));
                bean.setDataTimeSingle(DigitalTrans.toInt(DigitalTrans.subBytes(fileData, init + 80, 8)));
                bean.setDataTimeWhole(DigitalTrans.toInt(DigitalTrans.subBytes(fileData, init + 88, 8)));
                bean.setTimeBaseNum(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 96, 4)));
                bean.setTimeBaseDen(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 100, 4)));
                bean.setChecksum(DigitalTrans.unsigned4BytesToInt(DigitalTrans.subBytes(fileData, 104, 4), 0));
                bean.setBytesSent(DigitalTrans.byte2int(DigitalTrans.subBytes(fileData, init + 108, 4)));
                try {
                    //保存文件
                    String image = Tools.createFile("Cache",ctrlName,null,null,PreviewListActivity.this)+File.separator+String.valueOf(bean.getSeqNumrec())+".jpg";
                    myBitmap.saveBitmap(Tools.Bytes2Bimap(DigitalTrans.subBytes(fileData, init + 112, bean.getBytesSent())), image);
                    bean.setImage(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                headerData.add(bean);
                init = init+bean.getPktSize();
            }
         }else {
            dismissProgressDialog();
        }
    }

    @Override
    protected void ftpPreviewFail(PreviewReceiveModel model) {
        super.ftpPreviewFail(model);
        dismissProgressDialog();
        tipErrorShow("设备端发送预览文件失败");
    }

    //protected Intent mServiceIntent;

    @Override
    public void onItemClick(View view, String id, String name) {
        Tools.createFile("FTP", name, null, null, this);
        send.ctrlBase = null;
        send.ctrlName = name;
        send.ctrlNameStart = null;
        send.ctrlNameEnd = null;
        send.ctrlCount = getCount();
        send.sendData(Config.PREVIEWCTRL, 3); //信息请求
        showProgressDialog("提示", "视频信息加载",30*1000);
    }

    @Override
    public void onClickLeft() {
        ZnkActivityUtil.finishActivity();
    }

    @Override
    public void onClickRight() {

    }
    /**计算传输条数*/
    private int getCount(){
        itemHeight = (int) getResources().getDimension(R.dimen.layout_height);
        listHeight = AppUtils.getScreenHeight(this)-(int) getResources().getDimension(R.dimen.toolbar_height);
        return listHeight/itemHeight;
    }
}
