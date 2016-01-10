package com.jiuan.it.ipc.common.udp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.jiuan.it.ipc.Config;
import com.jiuan.it.ipc.IPCameraApplication;
import com.jiuan.it.ipc.tools.Tools;
import com.jiuan.it.ipc.ui.BaseActivity;
import com.jiuan.it.ipc.common.util.ZnkActivityUtil;
import com.jiuan.it.ipc.tcp.BaseRequest;
import com.jiuan.it.ipc.tcp.BaseResponse;
import com.jiuan.it.ipc.tcp.TcpResponseHandler;

import java.util.Arrays;
import java.util.Random;

public abstract class UDPActivity extends BaseActivity implements TCPClient.SuccessListener,TcpServer.TcpFailureListener {
	private String TAG = "UDPActivity";

	private final String IC = "COM.JIUAN.DEVICE";

	private final int OPEN_ACTION = 1;
	private final int CLOSE_ACTION = 2;

//	private EditText mEditText;
	private Data mData;
	private String mEqID;
	private String mIC;
	private Dialog connDialog;
	private Dialog disConnDialog;
	public static TCPClient mTcpClient;
	private SendDataAsyncTask task;

	private Handler msgHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initConnDialog();
		initDisConnDialog();

		msgHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.arg1) {
					case 0:
						Toast.makeText(UDPActivity.this, "双向认证失败，请重新认证！", Toast.LENGTH_LONG)
								.show();
						break;
					case 1:
						tcpConnection();
						break;
					case 2:
						disConnection();
						break;
					case 3:
						disConnection();
						break;
					default:
						break;
				}
			}
		};

		mData = new Data();
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	protected void searchDevice(String whiteBoxID) {

		if (!isSuccess() || !Arrays.equals(IPCameraApplication.getID(), whiteBoxID.getBytes())) {
			clearToken();
			IPCameraApplication.setID(whiteBoxID.getBytes());
			mData.setID(whiteBoxID.getBytes());
			mData.setIC(IC.getBytes());
			mData.setOID("AAA1".getBytes());
			mData.setRN("0001".getBytes());
			IPCameraApplication.setR1(Data.intToByteArray(new Random().nextInt(9999)));
			mData.setData1();
			Log.d(TAG, "UDP发送数据" + Arrays.toString(mData.getData1()));
			IPCameraApplication.UDP_BROADCAST_START = true;
			IPCameraApplication.TCPSERVER_RECIVE = true;

			new Thread(new TcpServer(this)).start();
			SystemClock.sleep(30);
			new Thread(new UDPBroadCast(mData.getData1())).start();

			if (mTcpClient == null) {
				mTcpClient = new TCPClient(this);
			}
		}
	}

	protected boolean isSuccess() {

		byte[] empty = new byte[] { 0, 0, 0, 0 };

		if (!Arrays.equals(IPCameraApplication.TOKEN, empty)) {
			return true;
		}

		return false;
	}

	protected boolean isConnection() {

		if (mTcpClient == null || !mTcpClient.checkConnection()) {
			return false;
		}

		return true;
	}

	public void sendData(BaseRequest content, TcpResponseHandler resHandler) {

		// 根据接收到的Token及Socket状态判断是否可以发送
		if (isConnection()) {
			if (isSuccess()) {
				// 判断发送数据是否为空
				if (content != null) {

					task = new SendDataAsyncTask(resHandler);

					task.execute(content);
				} else {
					Toast.makeText(UDPActivity.this, "不能发送空数据",
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(UDPActivity.this, "您还没有通过双向认证！",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

	// 将iSource转为长度为iArrayLen的byte数组，字节数组的低位是整型的低字节位
	public static byte[] toByteArray(int iSource, int iArrayLen) {
		byte[] bLocalArr = new byte[iArrayLen];
		for ( int i = 0; (i < 4) && (i < iArrayLen); i++) {
			bLocalArr[i] = (byte)( iSource>>8*i & 0xFF );

		}
		return bLocalArr;
	}

	protected void tcpConnection() {
		connDialog.show();
	}

	protected void disConnection() {
		//disConnDialog.show();
	}

	private void initConnDialog() {

		final TextView content = new TextView(this);

		content.setPadding(ZnkActivityUtil.dp2Px(this, 10), ZnkActivityUtil.dp2Px(this, 5), 0, 0);

		content.setTextSize(15);

		content.setText("检测到您与设备处于同一网络环境\n是否使用内网控制功能？");

		connDialog = new AlertDialog.Builder(this).setTitle("通知").setView(content)
				.setPositiveButton("使用", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						certifySuccess();
						dialog.dismiss();
					}
				})
				.setNegativeButton("忽略", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						clearToken();
						dialog.dismiss();
					}
				}).create();
	}

	private void initDisConnDialog(){

		final TextView content = new TextView(this);

		content.setPadding(ZnkActivityUtil.dp2Px(this, 10), ZnkActivityUtil.dp2Px(this, 5), 0, 0);

		content.setTextSize(15);

		content.setText("内网控制功能已关闭\n请检查网络或权限设置后重试");

		disConnDialog = new AlertDialog.Builder(this).setTitle("通知").setView(content)
				.setPositiveButton("重试",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								searchDevice(new String(IPCameraApplication.getID()));
								dialog.dismiss();
							}
						})
				.setNegativeButton("知道了", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
	}

	protected void clearToken() {
		IPCameraApplication.setTOKEN(new byte[]{0, 0, 0, 0});
	}

	@Override
	public void successListener() {
		// TODO Auto-generated method stub
		Log.d(TAG, "双向认证成功！");
		// 要想用Toast通知
		Message msg = msgHandler.obtainMessage();
		msg.arg1 = 1;
		msgHandler.sendMessage(msg);

	}

	@Override
	public void tcpConnectFailure(String message) {
		Log.d(TAG, "发送数据时，tcp连接失败！");

		if (isSuccess()) {
			clearToken();
			Message msg = msgHandler.obtainMessage();
			msg.arg1 = 2;
			msgHandler.sendMessage(msg);
		}else {
			Message msg = msgHandler.obtainMessage();
			msg.arg1 = 3;
			msgHandler.sendMessage(msg);
		}

	}

	@Override
	public void failureListener() {
		Log.d(TAG, "双向认证失败！");
		clearToken();

	}

	protected void certifySuccess() {

	}

	protected void certifyFailure() {
		clearToken();
	}

	protected void sendDataSuccess(String result) {
		Log.d(TAG, "发送指令成功");
	}

	protected void sendDataFailure(String result) {
		clearToken();
	}

	class SendDataAsyncTask extends AsyncTask<BaseRequest, Integer, String> {

		private TcpResponseHandler resHandler = null;

		public SendDataAsyncTask(TcpResponseHandler resHandler) {
			this.resHandler = resHandler;
		}

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "开始通过Socket发送数据");
			resHandler.onStart();
		}

		@Override
		protected String doInBackground(BaseRequest... params) {

			byte[] data = params[0].toJson().getBytes();

			byte[] token = IPCameraApplication.getTOKEN();

			byte[] sendData = new byte[data.length + 8];

			byte[] len = new byte[4];

			// 编辑协议头
			System.arraycopy(token, 0, sendData, 0, 4);
			len = Data.intToByteArray(data.length);
			System.arraycopy(len, 0, sendData, 4, 4);

			// 编辑body
			System.arraycopy(data, 0, sendData, 8, data.length);

			mTcpClient.sendDataForResult(sendData);

			if (IPCameraApplication.getRESULT().length > 4) {

				byte[] resLen = new byte[4];

				System.arraycopy(IPCameraApplication.getRESULT(), 4, resLen, 0, 4);

				int intResLen = Integer.parseInt(new String(resLen));

				byte[] resData = new byte[intResLen];

				System.arraycopy(IPCameraApplication.getRESULT(), 8, resData, 0, intResLen);
                //Tools.writeImageToDisk(IPCameraApplication.getRESULT(), "/storage/emulated/0/IPC/FTP/123.txt");
				return new String(resData);

			} else {
				return new String(IPCameraApplication.getRESULT());
			}
		}

		@Override
		protected void onPostExecute(String result) {
			Log.e(TAG, "接收到Socket返回结果" + result);

			if (TextUtils.equals(result, "0000")) {
				sendDataFailure("白盒已断开");
				resHandler.onFailure("0000");
				resHandler.onExceptionFinish();
				return;
			} else if (TextUtils.equals(result, "FFFF")) {
				sendDataFailure("内网不可用");
				resHandler.onFailure("FFFF");
				resHandler.onExceptionFinish();
				return;
			}

			BaseResponse response = resHandler.jsonToObject(result, BaseResponse.class);
			if (response != null) {
				String resultCode = response.getRespCode();
				if (TextUtils.equals(resultCode, "1000")) {
					sendDataSuccess(result);
					resHandler.onSuccess(result);
				} else {
					sendDataFailure(resultCode);
					resHandler.onFailure(resultCode);
				}

			} else {
				sendDataFailure("无法解析白盒返回的Json");
			}

			resHandler.onFinish();
		}
	}


}
