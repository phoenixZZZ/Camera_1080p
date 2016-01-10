package com.jiuan.it.ipc.utils;

public class OpenCV {
	static {
		System.loadLibrary("OpenCV");
	}
	public native int Andon_MosaicIMG(String inputFile1, String inputFile2, String outputFile);
}
