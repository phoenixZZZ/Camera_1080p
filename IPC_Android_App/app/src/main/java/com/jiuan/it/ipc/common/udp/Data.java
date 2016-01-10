package com.jiuan.it.ipc.common.udp;

import com.jiuan.it.ipc.IPCameraApplication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

/** 
 * @author chao 
 * @date 2015-6-19下午1:51:26
 * @Title: Data.java 
 * @Package com.jiuan.android.udptest  
 */
public class Data {
	
	private byte[] Data = getByteArray(124);
    private byte[] Data1 = getByteArray(128);
	private byte[] ID;//设备ID
	private byte[] IC;//标识码
	private byte[] OID;//指令顺序ID
	private byte[] RN;//请求码
	private byte[] AAC;//UDP请求接收确认码
	private byte[] R1 = IPCameraApplication.getByteArray(16);//随机数R1
	private byte[] R2 = IPCameraApplication.getByteArray(16);//随机数R2
	private byte[] ER1 = IPCameraApplication.getByteArray(16);//对随机数R1加密后的值
	private byte[] ER2 = IPCameraApplication.getByteArray(16);//对随机数R2加密后的值
	private int SUC;//验证成功标志
	private byte[] CRC = new byte[4];//校验码
	public byte[] getData() {
		return Data;
	}
	public void setData() {
		System.arraycopy(ID, 0, Data, 0, ID.length);
		System.arraycopy(IC, 0, Data, 16, IC.length);
		System.arraycopy(OID, 0, Data, 32, OID.length);
		System.arraycopy(RN, 0, Data, 36, RN.length);
//		System.arraycopy(AAC, 0, Data, 40, AAC.length);
		System.arraycopy(R1, 0, Data, 40, R1.length);
		System.arraycopy(ER1, 0, Data, 56, ER1.length);
		System.arraycopy(R2, 0, Data, 72, R2.length);
		System.arraycopy(ER2, 0, Data, 88, ER2.length);
		System.arraycopy(intToByteArray(SUC), 0, Data, 120, intToByteArray(SUC).length);
	}
	public byte[] getData1() {
		return Data1;
	}
	public void setData1() {
		setData();
		System.arraycopy(Data, 0, Data1, 0, Data.length);
		setCRC(getData());
		System.arraycopy(getCRC(), 0, Data1, 124, CRC.length);
	}

	public byte[] getID() {
		return ID;
	}
	public void setID(byte[] iD) {
		ID = iD;
	}
	public byte[] getIC() {
		return IC;
	}
	public void setIC(byte[] iC) {
		IC = iC;
	}
	public byte[] getOID() {
		return OID;
	}
	public void setOID(byte[] oID) {
		OID = oID;
	}
	public byte[] getRN() {
		return RN;
	}
	public void setRN(byte[] rN) {
		RN = rN;
	}
	public byte[] getAAC() {
		return AAC;
	}
	public void setAAC(byte[] aAC) {
		AAC = aAC;
	}
	public int getSUC() {
		return SUC;
	}
	public void setSUC(int sUC) {
		SUC = sUC;
	}
	
	public byte[] getR1() {
		return R1;
	}
	public void setR1(byte[] r1) {
		R1 = r1;
	}
	public byte[] getR2() {
		return R2;
	}
	public void setR2(byte[] r2) {
		R2 = r2;
	}
	public byte[] getER1() {
		return ER1;
	}
	public void setER1(byte[] eR1) {
		ER1 = eR1;
	}
	public byte[] getER2() {
		return ER2;
	}
	public void setER2(byte[] eR2) {
		ER2 = eR2;
	}
	public byte[] getCRC() {
		return CRC;
	}
	public void setCRC(byte[] c) {
		CRC32 crc32 = new CRC32();
		crc32.update(c);
		long crc = crc32.getValue();
		CRC = longToByteArray(crc);
	}

	private byte[] getByteArray(int num) {
		if (num < 0) return null;
		byte[] result = new byte[num];
		for (int i = 0; i < num; i++) {
			result[i] = 48;
		}

		return result;
	}

	public static byte[] intToByteArray(int i) {   
        byte[] result = new byte[] { 48, 48, 48, 48 };
//        //由高位到低位
//        result[0] = (byte)((i >> 24) & 0xFF);
//        result[1] = (byte)((i >> 16) & 0xFF);
//        result[2] = (byte)((i >> 8) & 0xFF);
//        result[3] = (byte)(i & 0xFF);

		byte[] value = String.valueOf(i).getBytes();

		if (value.length > 4) {
			System.arraycopy(value, value.length - 4, result, 0, 4);
		} else {
			System.arraycopy(value, 0, result, 4 - value.length, value.length);
		}

        return result;
      }

	public static byte[] longToByteArray(long i) {
		byte[] result = new byte[] { 48, 48, 48, 48 };
//        //由高位到低位
//        result[0] = (byte)((i >> 24) & 0xFF);
//        result[1] = (byte)((i >> 16) & 0xFF);
//        result[2] = (byte)((i >> 8) & 0xFF);
//        result[3] = (byte)(i & 0xFF);

		byte[] value = String.valueOf(i).getBytes();

		if (value.length > 4) {
			System.arraycopy(value, value.length - 4, result, 0, 4);
		} else {
			System.arraycopy(value, 0, result, 4 - value.length, value.length);
		}

		return result;
	}

//	public static byte[] InttoByteArray(int i) {
//		byte[] result = new byte[] { 48, 48, 48, 48 };
//        b[0] = (byte) (n & 0xff);
//        b[1] = (byte) (n >> 8 & 0xff);
//        b[2] = (byte) (n >> 16 & 0xff);
//        b[3] = (byte) (n >> 24 & 0xff);

//		byte[] value = String.valueOf(i).getBytes();
//
//		if (value.length <= 4) {
//			System.arraycopy(value, 0, result, 0, value.length);
//		}
//
//		return result;
//    }
//	public static byte[] intToByteArray ( int integer) {
//		ByteArrayOutputStream boutput = new ByteArrayOutputStream();
//		DataOutputStream doutput = new DataOutputStream(boutput);
//		try {
//			doutput.writeInt(integer);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		byte[] buf = boutput.toByteArray();
//
//		return (buf);
//		}
	public static int byteArrayToInt(byte[] b) {
        ByteArrayInputStream bintput = new ByteArrayInputStream(b);
        DataInputStream dintput = new DataInputStream(bintput);
        int i = 0;
		try {
			i = dintput.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return i;
 }
	public static byte[] toCRC(byte[] data){
		CRC32 crc32 = new CRC32();
		crc32.update(data);
		long crc = crc32.getValue();
		byte[] tCRC = longToByteArray(crc);
		return tCRC;
		
	}
	
}
