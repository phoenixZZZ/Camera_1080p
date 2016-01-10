package com.jiuan.it.ipc.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IPCameraApplication;
import com.jiuan.it.ipc.R;
import com.jiuan.it.ipc.http.share.Client;
import com.jiuan.it.ipc.model.PreviewReceiveModel;
import com.jiuan.it.ipc.model.ReceiveModel;
import com.jiuan.it.ipc.socket.SocketSend;
import com.jiuan.it.ipc.tools.AssetCopyer;
import com.jiuan.it.ipc.tools.Tools;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

public abstract class PreviewActivity extends BaseActivity {

	private String path = "";

	public  SimpleDateFormat fmt;

	private FtpServer mFtpServer;

	private Toast tipError;

	private PreviewReceive mReceiver;

	private ProgressDialog progress;

	private PreviewReceiveModel model ;

	private static final String TAG = "PreviewActivity";

	public   String KF = "headerData";

	public   String KS = "videoHeaders";

	public   String KI = "videoImageHeaders";

	public  static  String ftpPath; //Ftp目录

	public  static String ctrlFileName; //文件名称

	public  static String ctrlName;  //目录名称

	public static IPCameraApplication application;

	public final byte[]  data_video = new byte[1920*1080*3];

	public final byte[]   data_audio = new byte[1920*1080*3];

	public final double[] pts_video = new double[1];

	public final double[] pts_audio = new double[1];

	public final double[] dblVideoLastPts = new double[1];

	public final long[] lVideoLastPts = new long[1];

	public final double[] dblAudioLastPts = new double[1];

	public static int isRecord = 0;

	public static int IsHaveStop = 0;

	public static long TimeStamp_Stop= 0;

	public   String cutFileName = "";

	public final  String ext = ".mp4";

	private LocalBroadcastManager mLocalBroadcastManager;

	private NetworkUnavailableListenerImpl mNetworkUnavailableListenerImpl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (IPCameraApplication)getApplication();
		application.setContext(this);
		tipError = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		mNetworkUnavailableListenerImpl = new NetworkUnavailableListenerImpl();
		fmt=new SimpleDateFormat("yyyyMMddHHmmss");
		setContentView(getContentViewId());
		findViews();
		initGetData();
		widgetListener();
		init();

	}
	/** 放置布局文件*/
	protected abstract int getContentViewId();
	/** 控件初始化*/
	protected abstract void findViews();
	/** 初始化信息*/
	protected abstract void init();
	/** 控件监听*/
	protected abstract void widgetListener();
	/** 数据接收*/
	protected void initGetData() {
	};


	protected final Client.NetworkUnavailableListener getNetworkUnavailableListenerImpl() {
		return mNetworkUnavailableListenerImpl;
	}

	private class NetworkUnavailableListenerImpl implements Client.NetworkUnavailableListener {

		@Override
		public void networkUnavailable() {
			tipErrorShow(R.string.tip_network_unavailable);
		}
	}


	/**
	 * FTP开启
	 * @return 返回false连接失败
	 */
	protected boolean startFtpServer() {
		FtpServerFactory serverFactory = new FtpServerFactory();
		AssetCopyer asset =  new AssetCopyer(this);
		try {
			File assets =  asset.copy();
			if(assets==null){
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ListenerFactory factory = new ListenerFactory();
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
		path = getExternalFilesDir(null).getAbsolutePath()+File.separator+"users.properties";
		ftpPath = Tools.createFile("FTP", null, null, null, this);
		userManagerFactory.setFile(new File(path));
		serverFactory.setUserManager(userManagerFactory.createUserManager());
		// set the port of the listener
		factory.setPort(Config.FTP_PORT);
		//user.setAuthorities();
		// replace the default listener
		serverFactory.addListener("default", factory.createListener());
		// start the server
		FtpServer server = serverFactory.createServer();
		this.mFtpServer = server;
		try {
			mFtpServer.start();
		} catch (FtpException e) {
			e.printStackTrace();
			return false;
		}
        return  true;
	}
	/**FTP关闭*/
	protected void stopFtpServer() {
		if(null != mFtpServer) {
			mFtpServer.stop();
			mFtpServer = null;
		}
	}


	/**广播注册接收*/
	protected void registerReceiver() {
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.ACTION_RTSP_RECEIVE);
		filter.addAction(Config.ACTION_RTSP_FAILURE);
        /*网络连接不可用*/
		filter.addAction(Config.ACTION_EXTERNAL_MESSAGE);
		mReceiver = new PreviewReceive();
		//registerReceiver(mReceiver, filter);
		mLocalBroadcastManager.registerReceiver(mReceiver, filter);
	}
	/**广播取消*/
	protected void unregisterReceiver() {
		if(mReceiver != null){
			mLocalBroadcastManager.unregisterReceiver(mReceiver);
		}
	}

	private class PreviewReceive extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Config.ACTION_RTSP_RECEIVE)) {
				String value = intent.getStringExtra(Config.ACTION_EXTERNAL_MESSAGE);
				PreviewReceiveModel model = SocketSend.get(value, PreviewReceiveModel.class);
				if(model!=null){
					if("200".equals(model.getHeader())){
					   //请求返回成功信息
						Message message = new Message();
						message.what = model.getCtrlType();
						message.obj = model;
						preview.sendMessage(message);
						Log.e(TAG, "200"+ model.getCtrlType());
					}  else if(Config.PREVIEWCTRL.equals(model.getHeader())){ //视频文件FTP发送结束通知消息
						model.setMsgType(model.getHeader());
						//设备端发送信息
						Message message = new Message();
						message.obj = model;
						message.what = model.getCtrlType();
						preview.sendMessage(message);
					}else if(Config.NOTICECONTROL.equals(model.getHeader())){ //锁定控制（通知）
						model.setMsgType(model.getHeader());
						//设备端发送信息
						Message message = new Message();
						message.obj = model;
						message.what = model.getCtrlType();
						preview.sendMessage(message);
					}else if(Config.NOTICECLIENTMAX.equals(model.getHeader())){ //客户端数量上限通知
						model.setMsgType(model.getHeader());
						//设备端发送信息
						Message message = new Message();
						message.obj = model;
						message.what = model.getCtrlType();
						preview.sendMessage(message);
					}else if(Config.NOTICERECORDSTOP.equals(model.getHeader())){ //录像停止通知消息
						model.setMsgType(model.getHeader());
						//设备端发送信息
						Message message = new Message();
						message.obj = model;
						message.what = model.getCtrlType();
						preview.sendMessage(message);
					}else {
						tipErrorShow(SocketSend.showError(model.getHeader()));
						Message message = new Message();
						message.what = 9;
						message.obj = new PreviewReceiveModel();
						preview.sendMessage(message);
					}
				} else {
					tipErrorShow("数据格式无法解析");
					Message message = new Message();
					message.what = 9;
					message.obj = new PreviewReceiveModel();
					preview.sendMessage(message);
				}

			}else if (intent.getAction().equals(Config.ACTION_RTSP_FAILURE)) {
				tipErrorShow("Socket失败");
				Log.e(TAG, "Socket失败");
			}
		}
	}

	Handler preview = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//model = msg.getData().getParcelable(KF);
			model = ((PreviewReceiveModel)msg.obj);
			switch (msg.what){//CtrlType
				case 0:
					updateAdapter(model);
					break;
				case 1:
					updateAdapter(model);
					break;
				case 2:
					updateAdapter(model);
					break;
				case 3:
					requestPreview(model);
					break;
				case 4:
					ftpPreview(model);
					break;
				case 5:
					ftpPreviewFail(model);
					break;
				case 6:
					requestVideo(model);
					break;
				case 7:
					ftpVideo(model);
					break;
				case 8:
					ftpVideoFail(model);
					break;
				default:
					errorData();
					break;
			}
			super.handleMessage(msg);
		}
	};

	/**传输数据无法解析*/
	protected void errorData(){

	}
	/**更新列表数据*/
	protected void updateAdapter(PreviewReceiveModel model){

	}
	/**请求预览*/
	protected void requestPreview(PreviewReceiveModel model){

	}
	/**接收FTP预览成功*/
	protected void ftpPreview(PreviewReceiveModel model){

	}
	/**接收FTP预览失败*/
	protected void ftpPreviewFail(PreviewReceiveModel model){

	}
	/**请求录像*/
	protected void requestVideo(PreviewReceiveModel model){

	}
	/**接收FTP视频成功*/
	protected void ftpVideo(PreviewReceiveModel model){

	}
	/**接收FTP视频失败*/
	protected void ftpVideoFail(PreviewReceiveModel model){

	}


}
