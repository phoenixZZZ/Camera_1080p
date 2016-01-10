package com.jiuan.it.ipc.common.udp;

import android.util.Log;
import com.jiuan.it.ipc.IPCameraApplication;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class TCPClient implements Runnable {
	private String TAG = "TCPClient";
	Socket socket = null;
	private Boolean star = true;

	public interface SuccessListener {
		void successListener();
		void failureListener();
	}

	private SuccessListener mSuccessListener;

	public TCPClient(SuccessListener listener) {
		mSuccessListener = listener;
	}

	@Override
	public void run() {
		Log.d(TAG, "TCPClient");
		star = true;

		try {

			while (star) {
				String deviceIP = IPCameraApplication.getDEVICE_IP();
				if (deviceIP != null && (socket == null || socket.isClosed())) {
					// 时间戳
					IPCameraApplication.setT2(System.currentTimeMillis());
					socket = new Socket(deviceIP,
							IPCameraApplication.DEVICE_DEFAULT_PORT);
				}

				if (IPCameraApplication.isTWO_WAY_CERTIFY_START()) {
					// 发送数据
					twoWayCertify();

					if (IPCameraApplication.isTWO_WAY_CERTIFY_START()) {
						// 接收数据
						recive();
					}
				}
				
			}

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

	public void recive() {
		// 接收数据
		DataInputStream dis;
		try {
			if (socket != null && !socket.isClosed()) {
				dis = new DataInputStream(socket.getInputStream());
				byte[] mData = new byte[128];
				dis.read(mData);
				Log.d(TAG, "接收到服务端发送的数据是：" + Arrays.toString(mData));
				IPCameraApplication.setRECIVE_DATA(mData);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "IOException_recive" + e);
			twoWayCertifyFailuer();
			
		}

	}

	public void reciveData() {
		// 接收数据
		DataInputStream dis;

		try {
			if (socket != null && !socket.isClosed()) {
				dis = new DataInputStream(socket.getInputStream());
				byte[] bufferData = new byte[8192];
				dis.read(bufferData);

				byte[] resultSUC = new byte[4];
				System.arraycopy(bufferData, 0, resultSUC, 0, 4);
				if (Arrays.equals(resultSUC, IPCameraApplication.SUC)) {
					byte[] resultLen = new byte[4];
					System.arraycopy(bufferData, 4, resultLen, 0, 4);
					byte[] empty = new byte[4];

					// 判断接收到的数据是否为空(白盒断开的情况)
					if (Arrays.equals(resultLen, empty)) {
						IPCameraApplication.setRESULT(new byte[]{48, 48, 48, 48});
					} else {
						int bodyLen = Integer.parseInt(new String(resultLen));
						byte[] resultData = new byte[bodyLen + 8];
						System.arraycopy(bufferData, 0, resultData, 0, bodyLen + 8);

						Log.d(TAG, "接收到服务端发送的数据是：" + Arrays.toString(resultData));
						IPCameraApplication.setRESULT(resultData);
					}
				} else {
					IPCameraApplication.setRESULT(resultSUC);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "IOException_recive" + e);
		}

	}

	public void twoWayCertify() {
		if (socket != null) {
			byte[] data_128 = IPCameraApplication.getRECIVE_DATA();
			byte[] oid = new byte[4];// 接收到的OID
			System.arraycopy(data_128, 32, oid, 0, 4);
			byte[] ER1 = new byte[16];
			byte[] R2 = new byte[16];
			byte[] RN = new byte[4];// 接收到的RN
			System.arraycopy(data_128, 36, RN, 0, 4);
			Log.d(TAG, "接收到的RN" + Arrays.toString(RN));
			// 接收到的ID和IC
			byte[] ID = new byte[16];
			byte[] IC = new byte[16];
			System.arraycopy(data_128, 0, ID, 0, 16);
			System.arraycopy(data_128, 16, IC, 0, 16);
			// 待发送的RN
			byte[] RN1 = Data.intToByteArray(1);
			byte[] RN3 = Data.intToByteArray(2);
			byte[] RN5 = Data.intToByteArray(3);
			byte[] OID = new byte[4];// 待发送的OID
			byte[] ACC = new byte[4];// 接收到的ACC
			byte[] SUC = new byte[4];// 接收到的SUC
			// CRC校验
			byte[] data_124 = new byte[124];
			System.arraycopy(data_128, 0, data_124, 0, 124);
			byte[] data_crc = new byte[4];
			System.arraycopy(data_128, 124, data_crc, 0, 4);
			CRC32 crc32 = new CRC32();
			crc32.update(data_124);
			long crc = crc32.getValue();
			byte[] CRC = Data.longToByteArray(crc);

			Log.d(TAG, "data_crc" + Arrays.toString(data_crc));
			Log.d(TAG, "CRC" + Arrays.toString(CRC));
			// 首先验证ID、IC、CRC
			if (Arrays.equals(data_crc, CRC)
					&& Arrays.equals(ID, IPCameraApplication.getID())
					&& Arrays.equals(IC, IPCameraApplication.IC)) {

				Log.d(TAG, "oid" + Data.byteArrayToInt(oid));
				switch (Data.byteArrayToInt(oid)) {
				/*
				 * AAA1:1094795569 AAA2:1094795570 AAA3:1094795571
				 * AAA4:1094795572 AAA5:1094795573 AAA6:1094795574
				 */
				case 1094795570:
					// 验证RN
					if (Arrays.equals(RN, RN1)) {
						Log.d(TAG, "1094795570");
						System.arraycopy(data_128, 40, ACC, 0, 4);
						if (Arrays.equals(ACC, IPCameraApplication.ACC)) {
							RN = Data.intToByteArray(2);
							System.arraycopy(RN, 0, data_124, 36, 4);
							System.arraycopy(IPCameraApplication.getR1(), 0,
									data_124, 40, 16);
							Log.d(TAG, "data_124" + Arrays.toString(data_124));
							OID = "AAA3".getBytes();
							System.arraycopy(OID, 0, data_124, 32, 4);
							System.arraycopy(data_124, 0, data_128, 0, 124);
							System.arraycopy(Data.toCRC(data_124), 0, data_128,
									124, 4);
							Log.d(TAG, "data_128:" + Arrays.toString(data_128));

							sendData(data_128);
						} else {
							Log.e(TAG, "ACC不正确");
							twoWayCertifyFailuer();
							break;
						}

					} else {
						Log.e(TAG, "回复的RN1出错");
						twoWayCertifyFailuer();
						break;
					}

					break;
				case 1094795572:
					System.arraycopy(data_128, 56, ER1, 0, 16);
					System.arraycopy(data_128, 72, R2, 0, 16);
					// 验证RN和ER1
					if (Arrays.equals(IPCameraApplication.getR1(), XXTea.decrypt(ER1,
							XXTea.encrypt(IPCameraApplication.ID, XXTea.encrypt(
									IPCameraApplication.IC, IPCameraApplication.K1))))
							&& Arrays.equals(RN, RN3)) {
						RN = Data.intToByteArray(3);
						byte[] ER2 = XXTea.encrypt(R2, XXTea.encrypt(
								IPCameraApplication.ID, XXTea.encrypt(
										IPCameraApplication.IC, IPCameraApplication.K1)));

						OID = "AAA5".getBytes();
						System.arraycopy(OID, 0, data_124, 32, 4);
						System.arraycopy(RN, 0, data_124, 36, 4);
						System.arraycopy(IPCameraApplication.getByteArray(48), 0, data_124, 40, 48);
						System.arraycopy(ER2, 0, data_124, 88, 16);
						System.arraycopy(IPCameraApplication.SUC, 0, data_124, 120, 4);
						System.arraycopy(data_124, 0, data_128, 0, 124);
						System.arraycopy(Data.toCRC(data_124), 0, data_128,
								124, 4);

						sendData(data_128);
					} else {
						Log.e(TAG, "回复的RN3出错");
						twoWayCertifyFailuer();
						break;
					}

					break;
				case 1094795574:
					// 验证RN5
					if (Arrays.equals(RN, RN5)) {
						System.arraycopy(data_128, 120, SUC, 0, 4);
						IPCameraApplication.setTOKEN(SUC);
						IPCameraApplication.setTWO_WAY_CERTIFY_START(false);// 双向认证关闭
						long t = System.currentTimeMillis();
						Log.d(TAG, "T1" + IPCameraApplication.getT1());
						Log.d(TAG, "T2" + IPCameraApplication.getT2());
						Log.d(TAG, "整个流程所用毫秒数为：" + (t - IPCameraApplication.getT1())
								+ " 双向认证所用毫秒数为：" + (t - IPCameraApplication.getT2()));
						Log.d(TAG, "11验证成功");
//						try {
//							socket.close();
//							Log.d(TAG, "关闭Socket");
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						mSuccessListener.successListener();

					} else {
						Log.e(TAG, "回复的RN5出错");
						twoWayCertifyFailuer();
						break;
					}
					break;
				default:
					Log.e(TAG, "双向验证时的OID出错");
					twoWayCertifyFailuer();
					break;
				}

			} else {
				Log.e(TAG, "验证失败");
				twoWayCertifyFailuer();
				
			}

		}
	}

	public void sendData(byte[] data) {
		OutputStream os = null;

		try {
			os = socket.getOutputStream();
			os.write(data);
			os.flush();
			Log.d(TAG, "发送数据data:" + Arrays.toString(data));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e(TAG, "IOException_send" + e1);
		}

	}

	public void sendDataForResult(byte[] data) {
		OutputStream os = null;
		BufferedReader recv = null;

		try {
//			if (socket.isClosed()) {
//			}
			os = socket.getOutputStream();
			os.write(data);
			os.flush();
			Log.d(TAG, "发送数据data:" + Arrays.toString(data));

			reciveData();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e(TAG, "IOException_send" + e1);
			IPCameraApplication.setRESULT(new byte[]{70, 70, 70, 70});
			twoWayCertifyFailuer();
		}

	}

	public boolean checkConnection() {
		if (socket == null || socket.isClosed()) {
			return false;
		}

		return socket.isConnected();
	}

	public void twoWayCertifyFailuer(){
		star = false;
		mSuccessListener.failureListener();

		closeSocket();
	}

	public void closeSocket() {
		try {
			if(socket!=null){
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
