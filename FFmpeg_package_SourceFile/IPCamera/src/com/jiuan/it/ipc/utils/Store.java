package com.jiuan.it.ipc.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.os.Environment;

public class Store {
	public void store(String str,byte[] date){
		// 保存数据
		 if (Environment.getExternalStorageState().equals(
				 Environment.MEDIA_MOUNTED)) {
				
				 File path = Environment
				 .getExternalStoragePublicDirectory("IPC");
				 File file = new File(path, str);
				 try {
				 path.mkdirs();// Creates the directory named by this file,
				 // creating missing parent directories if
				 // necessary.
				 OutputStream os;
				 os = new FileOutputStream(file, true);
				 os.write(date);
				 os.close();
				
				 } catch (FileNotFoundException e) {
				 e.printStackTrace();
				
				 } catch (IOException e) {
				 e.printStackTrace();
				 }
				
				 }
	}
	
}
