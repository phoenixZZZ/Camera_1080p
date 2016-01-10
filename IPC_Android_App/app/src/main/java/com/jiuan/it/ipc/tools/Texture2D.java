/**
 * Name        : Texture2D.java
 * Copyright   : Copyright (c) Tencent Inc. All rights reserved.
 * Description : TODO
 */
package com.jiuan.it.ipc.tools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLUtils;
import android.widget.Toast;

import com.jiuan.it.ipc.Config;

/**
 * @author ianmao
 *
 */
public class Texture2D {
	private int mWidth;
	private int mHeight;
	private int mPow2Width;
	private int mPow2Height;
	private float maxU = 1.0f;
	private float maxV = 1.0f;

	long mTime = System.currentTimeMillis();

	private Bitmap mBitmap = null;

	private int textureId = 0;

	int[] mTextures = new int[1];

	// ɾ���������
	public void delete(GL10 gl) {
		if (textureId != 0) {
			gl.glDeleteTextures(1, new int[] { textureId }, 0);
			textureId = 0;
		}

		// bitmap
		if (mBitmap != null) {
			if (mBitmap.isRecycled())
				mBitmap.recycle();
			mBitmap = null;
		}

	}

	public static int pow2(int size) {
		int small = (int) (Math.log((double) size) / Math.log(2.0f));
		if ((1 << small) >= size)
			return 1 << small;
		else
			return 1 << (small + 1);
	}

	public Texture2D() {

	}

	public void bind(GL10 gl, Bitmap bmp) {
		// TODO 该宽高与与显示有关系，可以认为进行设置  (由于指数函数不是线性的，所以在这对其缩放会引起图像的宽高不协调)

		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();

		 mPow2Height = pow2(mHeight);
		 mPow2Width = pow2(mWidth);

		// maxU = mWidth / (float) mPow2Width;
		// maxV = mHeight / (float) mPow2Height;

//		Bitmap bitmap = Bitmap.createBitmap(mWidth, mWidth, bmp
//				.hasAlpha() ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(bitmap);
//		canvas.drawBitmap(bmp, 0, 0, null);
//		mBitmap = bitmap;

//		Canvas canvas = new Canvas(bmp);
//		canvas.drawBitmap(bmp, 0, 0, null);
		mBitmap = bmp;

		if (textureId != 0) {
			gl.glDeleteTextures(1, mTextures, 0);
			textureId = 0;
		} else {

			gl.glGenTextures(1, mTextures, 0);
			textureId = mTextures[0];

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_LINEAR);

			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	}

	public void bind(GL10 gl, byte[] data, int width, int height) {
		if (textureId != 0) {
			gl.glDeleteTextures(1, mTextures, 0);
			textureId = 0;
		} else {
			gl.glGenTextures(1, mTextures, 0);
			textureId = mTextures[0];

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_LINEAR);

//			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, width,
//					height, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE,
//					ByteBuffer.wrap(data));

			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, width,
					height, 0, GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE,
					ByteBuffer.wrap(data));

			//GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	}

	public void bind(GL10 gl, int[] data, int width, int height) {
		if (textureId != 0) {
			gl.glDeleteTextures(1, mTextures, 0);
			textureId = 0;
		} else {
			gl.glGenTextures(1, mTextures, 0);
			textureId = mTextures[0];

			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
					GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
					GL10.GL_LINEAR);

			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, width,
					height, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE,
					IntBuffer.wrap(data));
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	}

	public static FloatBuffer mBuffer;

	public static FloatBuffer floatToBuffer(float[] a) {
		// �ȳ�ʼ��buffer������ĳ���*4����Ϊһ��floatռ4���ֽ�
		ByteBuffer mbb = ByteBuffer.allocateDirect(a.length * 4);
		// ����������nativeOrder
		mbb.order(ByteOrder.nativeOrder());
		mBuffer = mbb.asFloatBuffer();
		mBuffer.put(a);
		mBuffer.position(0);
		return mBuffer;
	}

	// ���Ƶ���Ļ��
	public void draw(GL10 gl, float x, float y) {
		gl.glEnable(GL10.GL_TEXTURE_2D);//开启2D纹理
		gl.glEnable(GL10.GL_CULL_FACE);//开启剔除操作效果
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);//启用顶点数组

		// this.bind(gl);

		long time = System.currentTimeMillis() - mTime;
		mTime = System.currentTimeMillis();
		float step = 0.008f;

		// ӳ��
		// FloatBuffer verticleBuffer = floatToBuffer(
		// new float[] {
		// x , y + mHeight,
		// x + mWidth, y + mHeight,
		// x + mWidth, y,
		// x, y,
		// });
		//
		// FloatBuffer coordBuffer = floatToBuffer(
		// new float[] {
		// 0, 0,
		// maxU, 0,
		// maxU, maxV,
		// 0, maxV, });
		float[] f1 = new float[] { -0.5f, 0f, 0f, 0f, -0.5f, 0.5f, 0f, 0.5f, };

		FloatBuffer verticleBuffer = floatToBuffer(f1);

		FloatBuffer coordBuffer = floatToBuffer(new float[] { 0f, 1f, 1f, 1f,
				0f, 0f, 1f, 0, });
		//
		// for(int i = 0; i < mVerticles.size(); i++)
		// {
		// FloatBuffer verticleBuffer1 = floatToBuffer(mVerticles.get(i));
		// FloatBuffer coordBuffer1 = floatToBuffer(mUVs.get(i));
		//
		// gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordBuffer1);
		// gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verticleBuffer1);
		// gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
		// }
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordBuffer);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verticleBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		float[] f2 = new float[] { -1f, -1.2f, 1.2f, -1.2f, -1f, 1f, 1.2f, 1f, };
		FloatBuffer verticleBuffer1 = floatToBuffer(f2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verticleBuffer1);
		gl.glRotatef(Config.XROT, 0f, 0f, 0.1f);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}
}
