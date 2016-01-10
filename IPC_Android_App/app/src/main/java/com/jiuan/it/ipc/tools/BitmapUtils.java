package com.jiuan.it.ipc.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;

public class BitmapUtils {
	
public boolean createMyBitmap(byte[] data, int width, int height,String path){
   int []colors = convertByteToColor(data);
    boolean flag= true;
   if (colors == null){
       flag = false;
       return flag;
   }
  Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,Bitmap.Config.RGB_565);
    try {
        if(bmp==null){
            return false;
        }
        saveBitmap(bmp,path);
    } catch (IOException e) {
        flag =false;
        e.printStackTrace();
    }
  return flag;
}

public void saveBitmap(Bitmap bm,String path) throws IOException {
	  File f = new File(path);
	  if (f.exists()) {
	    f.delete();
	  }
    FileOutputStream out = new FileOutputStream(f);
    bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
    out.flush();
    out.close();
	 }


// 将一个byte数转成int
// 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
public  int convertByteToInt(byte data){
  
  int heightBit = (int) ((data>>4) & 0x0F);
  int lowBit = (int) (0x0F & data);
  return heightBit * 16 + lowBit;
}


// 将纯RGB数据数组转化成int像素数组
public int[] convertByteToColor(byte[] data){
  int size = data.length;
  if (size == 0){
   return null;
  }
  
  int arg = 0;
  if (size % 3 != 0){
   arg = 1;
  }
  
  // 一般情况下data数组的长度应该是3的倍数，这里做个兼容，多余的RGB数据用黑色0XFF000000填充  
  int []color = new int[size / 3 + arg];
  int red, green, blue;
  
  if (arg == 0){
   for(int i = 0; i < color.length; ++i){
    red = convertByteToInt(data[i * 3]);
    green = convertByteToInt(data[i * 3 + 1]);
    blue = convertByteToInt(data[i * 3 + 2]); 
    
    // 获取RGB分量值通过按位或生成int的像素值      
    color[i] = (red << 16) | (green << 8) | blue | 0xFF000000; 
   }
  }else{
   for(int i = 0; i < color.length - 1; ++i){
    red = convertByteToInt(data[i * 3]);
    green = convertByteToInt(data[i * 3 + 1]);
    blue = convertByteToInt(data[i * 3 + 2]); 
    color[i] = (red << 16) | (green << 8) | blue | 0xFF000000; 
   }
   
   color[color.length - 1] = 0xFF000000;
  }

  return color;
}
}