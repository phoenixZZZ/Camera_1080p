package com.jiuan.it.ipcamera;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class WifiListActivity extends Activity {
	private static String TAG = "WifiListActivity";
	private WifiManager wifiManager;
	List<ScanResult> list;
	// 得到配置好的网络连接
	List<WifiConfiguration> wifiConfigList;
	private ListView listView;
	private Socket socket;

	private String EncryptionType;
	private String EncryptAlgorithm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifilist);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);//WifiManager:管理wifi 连接，此类里面预先定义了许多常量，可以直接使用，不用再次创建： 
		WifiInfo info = wifiManager.getConnectionInfo();//WifiInfo ： wifi 连通以后，可以通过此类获得一些已经连通的wifi 连接的信息获取当前链接的信息
		wifiConfigList = wifiManager.getConfiguredNetworks();//WifiConfiguration:连通wifi 接入点需要获取到的信息
		
		list = wifiManager.getScanResults();//ScanResult：通过wifi 硬件扫描来获取周边的wifi 热点
		listView = (ListView) findViewById(R.id.listView);
		if (list == null) {
			Toast.makeText(this, "周围没有可连接的网络", Toast.LENGTH_SHORT).show();
		} else {
			listView.setAdapter(new MyAdapter(this, list));
		}
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				//创建Socket
				new Thread(runnable).start();
				if (!TextUtils.isEmpty(list.get(position).SSID)) {  
		               String capabilities = list.get(position).capabilities;  
		               Log.i(TAG,"capabilities=" + capabilities);  
		  
		               if (!TextUtils.isEmpty(capabilities)) {  
		  
		                   if (capabilities.contains("WPA-PSK") || capabilities.contains("wpa-psk")) {  
		                	   EncryptionType = "WPA_PSK";
		                   } else if (capabilities.contains("WPA2-PSK") || capabilities.contains("wpa2-psk")) {  
		                		EncryptionType = "WPA2_PSK"; 
		                   }else if (capabilities.contains("WEP") || capabilities.contains("wep")) {  
		                		EncryptionType = "IEEE8021X"; 
		                   } else {  
		                	   EncryptionType = "NONE";
		                   }  
		                   if (capabilities.contains("CCMP") || capabilities.contains("ccmp")) {  
		                	   EncryptAlgorithm = "CCMP";
		                   } else if (capabilities.contains("TKIP") || capabilities.contains("tkip")) {  
		                	   EncryptAlgorithm = "TKIP"; 
		                   } else {  
		                	   EncryptAlgorithm = "NONE";
		                   }  
		               }  
		           }  
			
				if (true) {
					LayoutInflater inflater = (LayoutInflater) WifiListActivity.this
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View dialogview = inflater.inflate(
							R.layout.layout_preset_dialog, null);
					final EditText nameEditText = (EditText) dialogview
							.findViewById(R.id.name);
					nameEditText.setText(list.get(position).SSID);
					final EditText passwordEditText = (EditText) dialogview
							.findViewById(R.id.password);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							WifiListActivity.this);
					builder.setTitle("连接").setView(dialogview);
					builder.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									Log.d(TAG, "WIFI_RESP,"
											+ EncryptionType+ ","+EncryptAlgorithm+ ","+nameEditText.getText()
											+ ","
											+ passwordEditText
													.getText());
									try {
										if (socket != null
												&& !socket.isClosed()) {
											PrintWriter out = new PrintWriter(
													new BufferedWriter(
															new OutputStreamWriter(
																	socket.getOutputStream())),
													true);
											out.println("WIFI_RESP,"
													+ EncryptionType+ ","+EncryptAlgorithm+ ","+nameEditText.getText()
													+ ","
													+ passwordEditText
															.getText());
											Log.d(TAG, "WIFI_RESP,"
													+ EncryptionType+EncryptAlgorithm+nameEditText.getText()
													+ ","
													+ passwordEditText
															.getText());
										} else {
											Log.e(TAG, "Socket创建连接失败");
										}
									} catch (IOException e) {
										e.printStackTrace();
										Log.e(TAG, "IOException" + e);
									}
									WifiConfiguration config = new WifiConfiguration();
									config.SSID = "\"" + nameEditText.getText() + "\"";
									config.preSharedKey = "\""+passwordEditText.getText()+"\""; //指定密码
									config.hiddenSSID = true;
									int netID = wifiManager.addNetwork(config);
									wifiManager.enableNetwork(netID, true);
									
								}
							});
					builder.show();
				}

			}
		});

	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "Runnable");
			try {
				socket = new Socket("192.168.100.100", 8234);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String rec = br.readLine();
				Log.d(TAG, "socket" + rec);
				// if(rec == "WIFI_REQ\r\n"){
				// isSocket = true;
				// }

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "UnknownHostException" + e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "IOException" + e);
			}
		}
	};

	public class MyAdapter extends BaseAdapter {

		LayoutInflater inflater;
		List<ScanResult> list;

		public MyAdapter(Context context, List<ScanResult> list) {
			// TODO Auto-generated constructor stub
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = null;
			view = inflater.inflate(R.layout.wifilist_item, null);
			ScanResult scanResult = list.get(position);
			TextView textView = (TextView) view.findViewById(R.id.textView);
			textView.setText(scanResult.SSID);
			ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
			// 根据wifi信号强度设置wifi图标
			if (Math.abs(scanResult.level) > 100) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.installation_progress_wifi0));
			} else if (Math.abs(scanResult.level) > 80) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.installation_progress_wifi1));
			} else if (Math.abs(scanResult.level) > 70) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.installation_progress_wifi1));
			} else if (Math.abs(scanResult.level) > 60) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.installation_progress_wifi2));
			} else if (Math.abs(scanResult.level) > 50) {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.installation_progress_wifi3));
			} else {
				imageView.setImageDrawable(getResources().getDrawable(
						R.drawable.installation_progress_wifi4));
			}
			return view;
		}

	}

}
