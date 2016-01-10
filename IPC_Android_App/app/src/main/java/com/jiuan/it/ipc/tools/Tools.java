package com.jiuan.it.ipc.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by admin on 2015/12/11.
 */
public class Tools {

    private static final  String TAG = "Tools";
    /**
     * 创建目录
     *
     * @param folderName    文件夹
     * @param deviceName    设备名称
     * @param subFolderName 子文件（按照时间命名）
     * @param fileName      文件
     */
    public static String createFile(String folderName, String deviceName, String subFolderName, String fileName, Context context) {
        StringBuffer PATH = new StringBuffer();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//优先保存到SD卡中
            PATH.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        } else {
            //如果SD卡不存在，就保存到本应用的目录下
            PATH.append(context.getFilesDir().getAbsolutePath());
        }
        PATH.append(File.separator + "IPC");
        if (folderName != null) {
            PATH.append(File.separator + folderName);
        }
        if (deviceName != null) {
            PATH.append(File.separator + deviceName);
        }
        if (subFolderName != null) {
            PATH.append(File.separator + subFolderName);
        }
        if (subFolderName != null) {
            PATH.append(File.separator + fileName);
        }
        File file = new File(PATH.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return PATH.toString();
    }


    public static Bitmap Bytes2Bimap(byte[] b) {
        Bitmap btm = null;
        try {
            btm = BitmapFactory.decodeByteArray(b, 0, b.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return btm;
    }

    /**
     * 指定目录下文件
     *
     * @param context
     * @param file
     * @return
     */
    public static byte[] ReadSettings(Context context, String file) {
        FileInputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = null;
        try {
            in = new FileInputStream(file);
            buffer = new byte[in.available()];
            in.read(buffer);
            out.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return buffer;
        }
    }

    public static Bitmap getLocalBitmap(String path,int size) {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = size;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    public static String intToIp(int i) {

        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 获取wifi下的IP地址
     *
     * @param context
     * @return
     */
    public static String getWifiIp(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    /**
     * 将图片写入到磁盘
     *
     * @param img      图片数据流
     * @param fileName 文件保存时的名称
     */
    public static void writeImageToDisk(byte[] img, String fileName) {
        try {
            File file = new File(fileName); //图片存放的路径
            file.createNewFile();
            FileOutputStream fops = new FileOutputStream(file);
            fops.write(img);
            fops.flush();
            fops.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean copyFile(String srcFileName, String destFileName, boolean reWrite)
            throws IOException {
        Log.d(TAG, "copyFile, begin");
        File srcFile = new File(srcFileName);
        File destFile = new File(destFileName);
        if(!srcFile.exists()) {
            Log.d(TAG, "copyFile, source file not exist.");
            return false;
        }
        if(!srcFile.isFile()) {
            Log.d(TAG, "copyFile, source file not a file.");
            return false;
        }
        if(!srcFile.canRead()) {
            Log.d(TAG, "copyFile, source file can't read.");
            return false;
        }
        if(destFile.exists() && reWrite){
            Log.d(TAG, "copyFile, before copy File, delete first.");
            destFile.delete();
        }

        try {
            InputStream inStream = new FileInputStream(srcFile);
            FileOutputStream outStream = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int byteRead = 0;
            while ((byteRead = inStream.read(buf)) != -1) {
                outStream.write(buf, 0, byteRead);
            }
            outStream.flush();
            outStream.close();
            inStream.close();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "copyFile, success");
        return true;
    }
}
