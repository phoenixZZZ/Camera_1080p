package com.jiuan.it.ipc.common.udp;

import android.util.Log;

import com.jiuan.it.ipc.IPCameraApplication;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class TcpServer implements Runnable,TCPClient.SuccessListener {
	private String TAG = "TcpServer";
	private boolean start = true;
	
	public interface TcpFailureListener {
		void tcpConnectFailure(String message);
	}

	private TcpFailureListener mTcpFailureListener;

	public TcpServer(TcpFailureListener listener) {
		mTcpFailureListener = listener;
	}

	public void run() {

		Socket socket = null;
		ServerSocket ss = null;
		DataInputStream dis = null;

		try {
			Log.d(TAG, "ServerSocket Starting");
			ss = new ServerSocket(IPCameraApplication.PHONE_DEFAULT_PORT);
			ss.setSoTimeout(6000);

			while (IPCameraApplication.TCPSERVER_RECIVE) {
				socket = ss.accept();// 此行代码会阻塞，将一直等待别人的连接
				Log.d(TAG, "socket : " + socket.toString());
				if (socket != null) {
					StringBuilder sb = new StringBuilder();
					sb.append(socket.getInetAddress().getHostAddress());
					Log.d(TAG, "connect : " + sb.toString());
					// 读取用户发送的数据
					dis = new DataInputStream(socket.getInputStream());
					byte[] data = new byte[128];
					dis.read(data);
					Log.d(TAG, "receive data : " + Arrays.toString(data));

					IPCameraApplication.setRECIVE_DATA(data);
					IPCameraApplication.UDP_BROADCAST_START = false;
					IPCameraApplication.setDEVICE_IP(sb.toString().trim());
					IPCameraApplication.TCPSERVER_RECIVE = false;
					IPCameraApplication.setTWO_WAY_CERTIFY_START(true);
					
					new Thread(UDPActivity.mTcpClient).start();
				}

				socket.close();
				ss.close();
				dis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
			mTcpFailureListener.tcpConnectFailure(e.toString());
			try {
				if(socket!=null){
					socket.close();
				}
				if(ss!=null){
					ss.close();	
				}
				if(dis!=null){
					dis.close();	
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
	}

	@Override
	public void successListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void failureListener() {
		// TODO Auto-generated method stub
		
	}

}
