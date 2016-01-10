package com.jiuan.it.ipcamera;


import java.lang.reflect.Field;
import java.util.ArrayList;

import glnk.client.GlnkClient;
import glnk.rt.MyRuntime;

import com.jiuan.it.ipc.utils.MyApplication;
import com.jiuan.it.ipc.utils.TUTKClient;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.AVFrame;
import com.tutk.IOTC.AVIOCTRLDEFs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MainActivity extends Activity {

	private static String TAG = "MainActivity";
	private static final String[] MODE = {"GOOLINK","RTSP","TUTK"};

	private Spinner ModeSpinner;
	private AutoCompleteTextView UidEditText;
	private ArrayAdapter<String> mAdapter;
	private Button ConnectButton;
	private EditText Password;
	private EditText UserName;

	private ArrayAdapter<String> modeAdapter;

	private String MyAccount;
	String mUserName ;
	String mPassword ;

	private Intent mIntent;

	
	private Boolean isGoolink = true;
	private Boolean isRTSP = false;
	private Boolean isTUTK = false;
	private Boolean isConnect = false;
	private Boolean isSetAudioFormat = false;
	private int audioFormat = 0;
	
	private int AudioCode;

	private long exitTime = 0;// 第一次按退出键时间

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// 解决实体键存在而不显示Actionbar菜单的问题
		try {
			ViewConfiguration mconfig = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(mconfig, false);
			}
		} catch (Exception ex) {
		}

		
		if (!MyRuntime.supported()) {
			Toast.makeText(this, "Goolink暂不支持的手机", Toast.LENGTH_SHORT).show();
			return;
		}

		Password = (EditText) findViewById(R.id.password);
		UserName = (EditText) findViewById(R.id.name);
		Password.setText("admin");
		UserName.setText("admin");
		ModeSpinner = (Spinner) findViewById(R.id.ModeSpinner);
		UidEditText = (AutoCompleteTextView) findViewById(R.id.UIDEditText);
		mAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line);
		UidEditText.setAdapter(mAdapter);
		ConnectButton = (Button) findViewById(R.id.connect);
		// 将可选内容与ArrayAdapter连接起来
		modeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, MODE);
		// 设置下拉列表的风格
		modeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ModeSpinner.setAdapter(modeAdapter);
		ModeSpinner
				.setOnItemSelectedListener(new ModeSpinnerSelectedListener());
		mIntent = new Intent();
		ConnectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "ConnectButton.setOnClickListener");
				if (UserName.length() < 1 && Password.length() < 1) {
					Toast.makeText(MainActivity.this, "请正确填写用户名和密码",
							Toast.LENGTH_SHORT).show();
				} else {
					//Goolink
					if(isGoolink){
						getLoginInfo();
						Log.d(TAG, "GOOLINK " + MyAccount);
						mIntent.setClass(MainActivity.this,
								GoolinkActivity.class);
						// TODO Goolink网络连接
						GlnkClient gClient = GlnkClient.getInstance();
						
                       /* clientName - 客户端名称
						firstStartTime - 客户端首次启用时间
						clientUDID - 客户端的UDID，ios可以随便指定值
						version - 客户端版本号
						type - 客户端类型*/
						int r = gClient.init(getApplication(), "IPC_Android", "20150114",
								"1234567890", 1, 1);
						Log.d(TAG,"Client.init返回值"+r);
						gClient.setStatusAutoUpdate(true);//设备状态自动更新
						int re = gClient.start();
						if(re==0){
							gClient.addGID(MyAccount);
							saveAccount(MyApplication.Goolink_ACCOUNT, MyAccount);
							startActivity(mIntent);
						}else{
							Log.d(TAG, "预连接返回值"+re);
							Toast.makeText(getApplicationContext(), "预连接失败", Toast.LENGTH_SHORT).show();
						}
					}
					//TUTK
					if (isTUTK) {
						getLoginInfo();
						Log.d(TAG, "TUTK " + MyAccount);
						mIntent.setClass(MainActivity.this, TUTKActivity.class);
						isConnect = TUTKClient.start(MyAccount, mUserName,
								mPassword);
						// 发送音频类型控制命令
						if (isConnect && isSetAudioFormat) {
							int res = AVAPIs
									.avSendIOCtrl(
											TUTKClient.AV_CID,
											AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETAUDIOFORMAT_REQ,
											AVIOCTRLDEFs.SMsgAVIoctrlSetAudioFormatReq
													.parseContent(
															TUTKClient.AV_CID,
															(byte) AudioCode),
											8);
							Log.d(TAG, "音频格式设置" + res);
							if (res < 0) {
								Log.e(TAG, "音频格式设置失败" + res);
								Toast.makeText(getApplicationContext(), "音频格式设置失败",
										Toast.LENGTH_SHORT).show();
							}
							isSetAudioFormat = false;
						}
						if (isConnect ) {
							if(TUTKClient.startIpcamStream()){
								saveAccount(MyApplication.TUTK_ACCOUNT, MyAccount);
								startActivity(mIntent);	
							}
						} else {
							TUTKClient.stop();
							Toast.makeText(MainActivity.this,
									"网络连接或数据请求失败，请重新连接!", Toast.LENGTH_SHORT)
									.show();
						}
					}
					//RTSP
					if(isRTSP){
						
						getLoginInfo();
						Log.d(TAG, "RTSP " + MyAccount);
						mIntent.setClass(MainActivity.this, RTSPActivity.class);
						saveAccount(MyApplication.RTSP_ACCOUNT, MyAccount);
						startActivity(mIntent);	
					}
					
				}
				
			}
		});

	}

	void getLoginInfo(){
		mUserName = UserName.getText().toString();
		mPassword = Password.getText().toString();
		MyAccount = UidEditText.getText().toString();
		mIntent.putExtra("UserName", mUserName);
		mIntent.putExtra("Password", mPassword);
		mIntent.putExtra("UID", MyAccount);
		mIntent.putExtra("audioFormat", audioFormat);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1) {
			UidEditText.setText(data.getStringExtra("UID"));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.connect:
			Intent intent = new Intent();
			intent.setClass(this, WifiListActivity.class);
			startActivity(intent);
			break;
		case R.id.scan:
			Intent mIntent = new Intent();
//			mIntent.setClass(this, ScanForUIDActivity.class);
//			startActivityForResult(mIntent, 1);
			break;
		case R.id.update:
			// updateVersion();
			break;
		case R.id.setVideoFormat:
			isSetAudioFormat = true;
			android.app.AlertDialog.Builder mBuilder = new AlertDialog.Builder(
					this)
					.setTitle("请选择要设置音频格式：")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setSingleChoiceItems(new String[] { "G711a", "AAC","PCM" }, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										AudioCode = AVFrame.MEDIA_CODEC_AUDIO_G711A;
										Log.d(TAG, "您的选择是" + which+AudioCode);
										audioFormat = 0;
										dialog.dismiss();
										break;
									case 1:
										AudioCode = AVFrame.MEDIA_CODEC_AUDIO_AAC;
										Log.d(TAG, "您的选择是" + which+AudioCode);
										audioFormat = 1;
										dialog.dismiss();
										break;
									case 2:
										AudioCode = AVFrame.MEDIA_CODEC_AUDIO_PCM;
										Log.d(TAG, "您的选择是" + which+AudioCode);
										audioFormat = 2;
										dialog.dismiss();
										break;
									default:
										AudioCode = AVFrame.MEDIA_CODEC_AUDIO_G711A;
										Log.d(TAG, "您的选择是默认值" + which+AudioCode);
										audioFormat = 0;
										dialog.dismiss();
										break;
									}
								}
							});
			mBuilder.show();
			break;
		}
		return true;
	}

	// 使用数组形式操作
	class ModeSpinnerSelectedListener implements OnItemSelectedListener {
		public void onNothingSelected(AdapterView<?> arg0) {
			Toast.makeText(MainActivity.this, "请选择连接方式", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onItemSelected");
			switch (position) {
			case 0:
				UidEditText.setText("");
				isGoolink = true;
				isRTSP = false;
				isTUTK = false;
				Log.d(TAG, "isGoolink"+isGoolink+";isRTSP"+isRTSP+";isTUTK"+isTUTK);
				setAutoCompleteData(MyApplication.Goolink_ACCOUNT);
				break;
			case 1:
				UidEditText.setText("");
				isGoolink = false;
				isRTSP = true;
				isTUTK = false;
				UidEditText.setText("rtsp://10.0.0.5/cam1/h264");
				Log.d(TAG, "isGoolink"+isGoolink+";isRTSP"+isRTSP+";isTUTK"+isTUTK);
				setAutoCompleteData(MyApplication.RTSP_ACCOUNT);
				break;
			case 2:
				UidEditText.setText("");
				isGoolink = false;
				isRTSP = false;
				isTUTK = true;
				Log.d(TAG, "isGoolink"+isGoolink+";isRTSP"+isRTSP+";isTUTK"+isTUTK);
				setAutoCompleteData(MyApplication.TUTK_ACCOUNT);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(), "再按一次退出程序",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				MyApplication.videoDataToPlay.clear();
				finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 保存帐号
	 */
	private void saveAccount(String name, String account) {
		SharedPreferences preferences = getSharedPreferences(name,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		int num = preferences.getInt(MyApplication.ACCOUNT_NUM_KEY, 0);
		ArrayList<String> accountArray = new ArrayList<String>();
		if (num != 0) {
			for (int i = 0; i < num; i++) {
				accountArray.add(
						i,
						preferences.getString(MyApplication.ACCOUNT_NAME_KEY
								+ (i + 1), null));
			}
		}
		for (int i = 0; i < num; i++) {
			if (accountArray.get(i).equals(account)) {
				return;
			}
		}
		editor.putInt(MyApplication.ACCOUNT_NUM_KEY, num + 1);
		editor.putString(MyApplication.ACCOUNT_NAME_KEY + (num + 1), account);
		editor.apply();
	}

	/**
	 * 设置自动填写的数据.
	 */
	private void setAutoCompleteData(String modeName) {
		SharedPreferences preferences = getSharedPreferences(modeName,
				Context.MODE_PRIVATE);
		int num = preferences.getInt(MyApplication.ACCOUNT_NUM_KEY, 0);
		if (num != 0) {
			for (int i = 0; i < num; i++) {
				mAdapter.add(preferences.getString(
						MyApplication.ACCOUNT_NAME_KEY + (i + 1), null));
			}
		}
	}

}