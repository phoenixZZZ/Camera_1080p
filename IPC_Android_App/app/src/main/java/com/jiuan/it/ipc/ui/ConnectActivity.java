package com.jiuan.it.ipc.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.DeviceListBean;
import com.jiuan.it.ipc.ui.adapter.ConnectAdapter;
import com.jiuan.it.ipc.model.LoginInfo;
import com.jiuan.it.ipc.tools.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import glnk.client.GlnkClient;
import glnk.rt.MyRuntime;

public class ConnectActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener,
        ConnectAdapter.ConnectViewClickListener{

    //连接类型
    public static final int GOOLINK = 1;

    public static final int TUTK = 2;

    public static final int RTSP = 3;

    private long exitTime = 0;// 第一次按退出键时间

    private static final int TIME = 2000;

    private List<LoginInfo> connect;

    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private GridLayoutManager layoutManager;

    private ConnectAdapter mAdapter;

    public static GlnkClient gClient = null;

    private static String TAG = "ConnectActivity";

    public SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        layoutManager = new GridLayoutManager(this,2);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ConnectAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mSwipeRefreshLayout.setEnabled(layoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSyncUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            onStartActivity(this, AddDeviceActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 切换界面
     */
    protected void onStartActivity(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        startSyncUI();
        stopSyncUI();
    }

    @Override
    public void onItemClick(View view, int position, DeviceListBean model) {

    }

    @Override
    public void onDeleteBtnClick(View view, int position) {

    }

    /*开启下拉刷新*/
    private void startSyncUI() {
       // connect = Config.getDevice(this);
        if(connect!=null){
            //mAdapter.setList(connect);
            mAdapter.notifyDataSetChanged();
        }
    }

    /*停止下拉刷新*/
    private void stopSyncUI() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > TIME) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
