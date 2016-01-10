package com.jiuan.it.ipc.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.model.DeviceListBean;
import com.jiuan.it.ipc.tools.AppUtils;
import com.jiuan.it.ipc.ui.widget.SlidingButtonView;

import java.util.ArrayList;
import java.util.List;


public class ConnectAdapter extends RecyclerView.Adapter<ConnectAdapter.ViewHolder>
		implements SlidingButtonView.IonSlidingButtonListener{

	private List<DeviceListBean> connectList;

	private Context mContext;

	private ConnectViewClickListener mConnectViewClickListener;

	private SlidingButtonView mMenu = null;

	public ConnectAdapter(Context context) {
		this.mContext = context;
		mConnectViewClickListener = (ConnectViewClickListener) context;
		connectList = new ArrayList<>();
		setHasStableIds(true);
	}

	public List<DeviceListBean> getList()  {
		return connectList ;
	}

	public void setList(List<DeviceListBean> list) {
		connectList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return -1;
	}

	@Override
	public int getItemCount() {
		if (connectList != null) {
			return connectList.size();
		}
		return 1;
	}

	@Override
	public long getItemId(int position) {
		return position+1;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delete, parent, false);
		ViewHolder vh = new ViewHolder(view);
		return vh;
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		 final DeviceListBean user = connectList.get(position);
		 holder.text.setText(user.getName());
		//设置内容布局的宽为屏幕宽度
		holder.layout_content.getLayoutParams().width = AppUtils.getScreenWidth(mContext);
		holder.layout_content.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//判断是否有删除菜单打开
				if (menuIsOpen()) {
					closeMenu();//关闭菜单
				} else {
					int n = holder.getLayoutPosition();
					mConnectViewClickListener.onItemClick(v, n,user);
				}

			}
		});
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int n = holder.getLayoutPosition();
				closeMenu();//关闭菜单
				mConnectViewClickListener.onDeleteBtnClick(v, n);
			}
		});

	}

	/**
	 * 删除菜单打开信息接收
	 */
	@Override
	public void onMenuIsOpen(View view) {
		//closeMenu();
		mMenu = (SlidingButtonView) view;
	}

	/**
	 * 滑动或者点击了Item监听
	 * @param slidingButtonView
	 */
	@Override
	public void onDownOrMove(SlidingButtonView slidingButtonView) {
		if(menuIsOpen()){
			if(mMenu != slidingButtonView){
				closeMenu();
			}
		}
	}


	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView text;
		ViewGroup layout_content;
		TextView btn_Delete;
		RelativeLayout delete;
		public ViewHolder(View itemView) {
			super(itemView);
			text = (TextView) itemView.findViewById(R.id.care_name);
			btn_Delete = (TextView) itemView.findViewById(R.id.btn_Delete);
			layout_content = (ViewGroup) itemView.findViewById(R.id.layout_content);
			delete = (RelativeLayout) itemView.findViewById(R.id.delete);
			((SlidingButtonView) itemView).setSlidingButtonListener(ConnectAdapter.this);
		}
	}

	public interface ConnectViewClickListener {
		void onItemClick(View view,int position, DeviceListBean model);
		void onDeleteBtnClick(View view,int position);
	}

	/**
	 * 关闭菜单
	 */
	public void closeMenu() {
		if(mMenu!=null){
			mMenu.closeMenu();
			mMenu = null;
		}


	}
	/**
	 * 判断是否有菜单打开
	 */
	public Boolean menuIsOpen() {
		if(mMenu != null){
			return true;
		}
		return false;
	}

}
