package com.jiuan.it.ipc.common.udp;

import android.util.Log;

import com.jiuan.it.ipc.IPCameraApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer extends Thread {
	private String TAG = "UDPServer";

	@Override
	public void run() {
		Log.d(TAG, "UDPServer");
		try {
			DatagramSocket socket = new DatagramSocket(
					IPCameraApplication.DEVICE_DEFAULT_PORT);
			byte[] buffer = new byte[64];
			DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length);
			while (true) {
				socket.receive(packet);
				String s = new String(packet.getData(), 0, packet.getLength());
				Log.d(TAG, "接收到的数据" + s);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
