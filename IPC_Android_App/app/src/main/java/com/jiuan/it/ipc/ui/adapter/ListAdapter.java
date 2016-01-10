package com.jiuan.it.ipc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.DeviceListBean;
import com.jiuan.it.ipc.tools.StepComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements View.OnClickListener{

    private List<String> mMessages;

    private Context mContext;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int multiple; //时间戳的倍数(秒与毫秒)
    public ListAdapter(Context context,int multiple) {
        mMessages = new ArrayList<>();
        setHasStableIds(true);
        this.mContext = context;
        this.multiple = multiple;
    }

    public void setList(List<String> messages) {
        mMessages = messages;
        //对集合对象进行排序
        StepComparator comparator=new StepComparator();
        Collections.sort(mMessages, comparator);
        notifyDataSetChanged();
    }
    @Override
    public long getItemId(int position) {
        return Long.valueOf(mMessages.get(position));
    }
    public List<String> getList()  {
        return mMessages ;
    }
    public void addList(String str) {
        if(str!=null){
            mMessages.add(str);
        }
        //对集合对象进行排序
        StepComparator comparator=new StepComparator();
        Collections.sort(mMessages, comparator);
    }
    public void clear() {
        mMessages.clear();
        notifyDataSetChanged();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.item_device;
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        String message = mMessages.get(position);
        viewHolder.setUsername(message);

    }

    @Override
    public int getItemCount() {
        if (mMessages != null) {
            return mMessages.size();
        }
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mUsernameView;

        public ViewHolder(View itemView) {
            super(itemView);
            mUsernameView = (TextView) itemView.findViewById(R.id.uid);
        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            long time = Long.valueOf(username);
            Date date = new Date();
            date.setTime(time*multiple);
            mUsernameView.setText(fmt.format(date));
            mUsernameView.setTag(username);
        }
    }



    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            TextView name = (TextView)v.findViewById(R.id.uid);
            mOnItemClickListener.onItemClick(v,name.getText().toString(),(String)name.getTag());
        }
    }

    public  interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, String id, String name);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}