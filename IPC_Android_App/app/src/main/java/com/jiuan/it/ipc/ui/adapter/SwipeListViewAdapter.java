package com.jiuan.it.ipc.ui.adapter;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.common.listview.SwipeListView;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.model.DeviceListBean;
import com.jiuan.it.ipc.ui.DeleteDeviceConfirmActivity;

public class SwipeListViewAdapter extends BaseAdapter {

    private List<DeviceListBean> datas;
    private Context context;
    private SwipeListView mSwipeListView;
    private LayoutInflater inflater;

    public SwipeListViewAdapter(Context context, SwipeListView mSwipeListView, List<DeviceListBean> datas) {

        this.mSwipeListView = mSwipeListView;
        this.datas = datas;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return datas.size();
    }

    @Override
    public DeviceListBean getItem(int position) {
        // TODO Auto-generated method stub
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final DeviceListBean td = getItem(position);
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = inflater.inflate(R.layout.device_listview, null);

            vh.deviceIcon = (ImageView) convertView.findViewById(R.id.device_icon);
            vh.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            vh.deviceMessage = (TextView) convertView.findViewById(R.id.device_message);
            vh.deviceTime = (TextView) convertView.findViewById(R.id.device_time);
//            vh.shareIcon = (ImageView) convertView.findViewById(R.id.device_share_icon);
            vh.deviceShareCompressIcon = (ImageView) convertView.findViewById(R.id.device_share_compress_icon);
            vh.deviceDeleteBtn = (Button) convertView.findViewById(R.id.device_delete_btn);
            vh.deviceIgnoreBtn = (Button) convertView.findViewById(R.id.device_ignore_btn);
            vh.deviceAgreeBtn = (Button) convertView.findViewById(R.id.device_agree_btn);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.deviceIcon.setBackgroundResource(R.drawable.mysheb_icon_3);
        vh.deviceName.setText(td.getName());

        // 0：否(分享给我的设备) 1：是(自己的设备)
//        if (td.getIsOwner() == 1) {
//            vh.shareIcon.setBackgroundResource(R.drawable.mysheb_icon_5);
//            vh.shareIcon.setVisibility(View.VISIBLE);
            vh.deviceShareCompressIcon.setVisibility(View.GONE);
            vh.deviceIgnoreBtn.setVisibility(View.GONE);
            vh.deviceAgreeBtn.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(context.getResources().getDisplayMetrics().widthPixels / 5, RelativeLayout.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            convertView.findViewById(R.id.back).setVisibility(View.VISIBLE);
            vh.deviceDeleteBtn.setVisibility(View.VISIBLE);
            vh.deviceDeleteBtn.setLayoutParams(layoutParams);

            vh.deviceDeleteBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mSwipeListView.closeAnimate(position);
                    mSwipeListView.dismiss(position);

                    Intent intent = ZnkActivityUtil.getSingletonIntent();
                    intent.setClass(ZnkActivityUtil.getContext(), DeleteDeviceConfirmActivity.class);
                    intent.putExtra("eqID", td.getCode());
                    intent.putExtra("eqType", String.valueOf(td.getEqType()));
                    intent.putExtra("name", ZnkActivityUtil.getClassName());
                    ZnkActivityUtil.getContext().startActivity(intent);

                }
            });

        return convertView;
    }

    class ViewHolder {
        ImageView deviceIcon;
        ImageView deviceShareCompressIcon;
        TextView deviceName;
        TextView deviceMessage;
        TextView deviceTime;
        // ImageView shareIcon;
        Button deviceDeleteBtn;
        Button deviceIgnoreBtn;
        Button deviceAgreeBtn;
    }
}
