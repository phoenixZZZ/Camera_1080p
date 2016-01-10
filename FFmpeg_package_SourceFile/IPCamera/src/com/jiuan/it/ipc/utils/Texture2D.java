package com.jiuan.it.ipc.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;


public class Texture2D {
	long mTime = System.currentTimeMillis();

	private Bitmap mBitmap = null;

	private int textureId = 0;

	int[] mTextures = new int[1];

	public void delete(GL10 gl) {
		if (textureId != 0) {
			gl.glDeleteTextures(1, new int[] { textureId }, 0);
			textureId = 0;
		}

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

			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, width,
					height, 0, GL10.GL_RGB, GL10.GL_UNSIGNED_BYTE,
					ByteBuffer.wrap(data));
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	}
	
	public static FloatBuffer mBuffer;

	public static FloatBuffer floatToBuffer(float[] a) {
		ByteBuffer mbb = ByteBuffer.allocateDirect(a.length * 4);
		mbb.order(ByteOrder.nativeOrder());
		mBuffer = mbb.asFloatBuffer();
		mBuffer.put(a);
		mBuffer.position(0);
		return mBuffer;
	}
	public void draw(GL10 gl, float x, float y) {
		gl.glEnable(GL10.GL_TEXTURE_2D);//开启2D纹理
		gl.glEnable(GL10.GL_CULL_FACE);//开启剔除操作效果
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);//启用顶点数组
		mTime = System.currentTimeMillis();
		float[] f1 = new float[] { -0.5f, 0f, 0f, 0f, -0.5f, 0.5f, 0f, 0.5f, };

		FloatBuffer verticleBuffer = floatToBuffer(f1);

		FloatBuffer coordBuffer = floatToBuffer(new float[] { 0f, 1f, 1f, 1f,
				0f, 0f, 1f, 0, });
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, coordBuffer);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verticleBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		float[] f2 = new float[] { -1f, -1.2f, 1.2f, -1.2f, -1f, 1f, 1.2f, 1f, };
		FloatBuffer verticleBuffer1 = floatToBuffer(f2);
		gl.glVertexPointer(2, GL10.GL_FLOAT, 0, verticleBuffer1);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}

}
