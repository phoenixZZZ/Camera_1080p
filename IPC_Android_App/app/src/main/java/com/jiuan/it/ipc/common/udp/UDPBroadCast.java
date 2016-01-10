package com.jiuan.it.ipc.common.udp;

import android.util.Log;

import com.jiuan.it.ipc.IPCameraApplication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPBroadCast implements Runnable {
	private byte[] buffer = new byte[IPCameraApplication.MAX_DATA_PACKET_LENGTH];
	private String TAG = "UDPBroadCast";
	private byte[] data;
	private DatagramSocket udpSocket;

	public UDPBroadCast(byte[] data) {
		this.data = data;
	}

	public void run() {
		DatagramPacket dataPacket = null;
		//时间戳
		IPCameraApplication.setT1(System.currentTimeMillis());

		try {
			udpSocket = new DatagramSocket(IPCameraApplication.PHONE_DEFAULT_PORT);
			dataPacket = new DatagramPacket(buffer,
					IPCameraApplication.MAX_DATA_PACKET_LENGTH);
			dataPacket.setData(data);
			dataPacket.setLength(data.length);
			dataPacket.setPort(IPCameraApplication.DEVICE_DEFAULT_PORT);
			InetAddress broadcastAddr;
			broadcastAddr = InetAddress.getByName("255.255.255.255");
			dataPacket.setAddress(broadcastAddr);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
//		if(MyApplication.UDP_BROADCAST_START) {
//			while(true){//能确保在30秒内接收成功就不用循环发送
				try {
					
					udpSocket.send(dataPacket);
					Log.d(TAG, "成功发送一次UDP广播");
//					sleep(3000);
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}

//			}
//		}else{
				if(udpSocket!=null){
					udpSocket.close();
				}
			
//		}
        
	}

}
