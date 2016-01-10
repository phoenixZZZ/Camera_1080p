package com.jiuan.it.ipc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listener.ZNKNetWorkUnavialableListener;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.http.Client;
import com.jiuan.it.ipc.http.ResponseHandler;
import com.jiuan.it.ipc.http.response.TypeCodeDetail;
import com.jiuan.it.ipc.model.GlobalBeanManager;
import com.jiuan.it.ipc.tools.StepComparator;
import com.jiuan.it.ipc.ui.adapter.ListAdapter;
import com.jiuan.it.ipc.ui.widget.CustomToolbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ImageListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        CustomToolbar.OnClickCustomToolbarListener,ListAdapter.OnRecyclerViewItemClickListener{

    private final String TAG_CLASS_NAME = this.getClass().getSimpleName();

    private CustomToolbar toolbar =null;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LinearLayoutManager layoutManager;

    private ListAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private List<String> mBean = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初期加载画面显示
        setContentView(R.layout.layout_list);

        toolbar = (CustomToolbar)this.findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ListAdapter(this,1);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);

        toolbar.setOnClickCuteToolbarListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mAdapter.setList(mBean);
        mAdapter.setOnItemClickListener(this);
        getImageList();
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
    public void onItemClick(View view, String id, String name) {
        getImageDetail(Config.DEVICE_CODE, name);
    }
    /**
     * Image详细
     */
    private void getImageDetail(final String deviceID,final String typeCode) {
        // 调用用户绑定白盒子API
        Client.requestTypeCodeDetail(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), deviceID, typeCode,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        java.lang.reflect.Type listType = new TypeToken<LinkedList<TypeCodeDetail>>() {
                        }.getType();
                        Gson gson = new Gson();
                        LinkedList<TypeCodeDetail> list = gson.fromJson(value.toString(), listType);
                        String[] imageUrl = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            imageUrl[i] = list.get(i).getFilePath();
                        }
                        if(imageUrl.length==8){
                            Intent intent = new Intent(getApplicationContext(),
                                    CheckImageActivity.class);
                            intent.putExtra("FileList",imageUrl);
                            intent.putExtra("typeCode",typeCode);
                            startActivity(intent);
                        }else {
                            ZnkActivityUtil.showSimpleDialog("图片数目不对");
                        }

                    }

                    @Override
                    public void onZNKFailure(String value) {
                        ZnkActivityUtil.showSimpleDialog("提示", value);
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        showTokenFailure(value);
                    }

                }, new ZNKNetWorkUnavialableListener());
    }
    /**
     * Image列表
     */
    private void getImageList() {
        // 调用用户绑定白盒子API
        Client.requestTypeCodes(this, Config.getGlobal(this).getHguid(), Config.getGlobal(this).getToken().getAccessToken(), Config.DEVICE_CODE,
                new ResponseHandler() {

                    @Override
                    public void onInnovationSuccess(JsonElement value) {
                        super.onInnovationSuccess(value);
                        // 获取相应结果
                        String[] array = get(value.toString(), String[].class);
                        mBean = new ArrayList<String>();
                        for (int i = 0; i < array.length; i++) {
                            mBean.add(array[i]);
                        }
                       // mAdapter.clear();
                        mAdapter.setList(mBean);
                        stopSyncUI();
                    }

                    @Override
                    public void onZNKFailure(String value) {
                        ZnkActivityUtil.showSimpleDialog("提示", value);
                        stopSyncUI();
                    }

                    @Override
                    public void onZNKTokenFailure(String value) {
                        showTokenFailure(value);
                        stopSyncUI();
                    }

                }, new ZNKNetWorkUnavialableListener());
    }


    @Override
    public void onRefresh() {
        getImageList();
    }

    /**开始下拉刷新*/
    private void startSyncUI() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    /**停止下拉刷新*/
    private void stopSyncUI() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

}