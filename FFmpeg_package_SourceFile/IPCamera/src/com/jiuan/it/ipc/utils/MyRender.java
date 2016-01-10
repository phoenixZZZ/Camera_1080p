package com.jiuan.it.ipc.utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.jiuan.it.ipcamera.R;




import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class MyRender implements GLSurfaceView.Renderer {
	Context mContext;
	public byte[] mFrameBuf = new byte[1280 * 720 * 3];
	private Texture2D mTexture2d ;
	Bitmap mBitmap ;
	private int mWidth = 0;
	private int mHeight = 0;
	private byte[] mData;

	public MyRender(Context context) {
		Log.e("进入此函数", "MyRender");
		mContext = context;
		mBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.img);
		mTexture2d = new Texture2D();

	}
	public void update(byte[] data, int width, int height) {
		Log.d("进入此函数", "update(byte[] data, int width, int height)");
		mWidth = width;
		mHeight = height;
		mData = data;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		Log.e("进入此函数", "onSurfaceCreated");
		// TODO Auto-generated method stub
		// 启用阴影平滑（用于控制opengl中绘制指定两点间其他点颜色的过渡模式，GL_SMOOTH会出现过度效果）
		gl.glShadeModel(GL10.GL_SMOOTH);
		// 设置背景颜色（0,0,0,0）表示黑色背景（指定清除颜色缓存时所使用的红、绿、蓝和alpha值，指定值的范围固定为[0.0f，1.0f]。）
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		// 设置深度缓存为 1，这个时候你往里面画一个物体， 由于物体的每个像素的深度值都小于等于 1，
		// 所以整个物体都被显示了出来。 如果初始值指定为 0， 物体的每个像素的深度值都大于等于
		// 0， 所以整个物体都不可见。如果初始值指定为 0.5， 那么物体就只有深度小于 0.5的那部分才是可见的
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);// 启用深度测试（打开服务器端的 GL 功能）
		// 所做深度测试的类型
		// gl.glDisable(GL10.GL_DITHER);
		// 告诉系统对透视进行修正（在
		// OpenGL中，许多细节的实现算法有所不同。这样，可以调用函数glHint()对图像质量和绘制速度之间的权衡作一些控制，但并非所有的实现都采
		// 用它）
		gl.glDepthFunc(GL10.GL_LEQUAL);
		// GL_NICEST为质量最好模式，GL_FASTEST为速度最快模式
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		// gl.glClearColor(1, 1, 1, 1);
		gl.glEnable(GL10.GL_CULL_FACE);// 开启剔除操作效果
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.e("进入此函数", "onSurfaceChanged");
		// TODO Auto-generated method stub
		int mVideoWidth = 16;
		int mVideoHeight = 9;
		/**
		 * glViewport 设定视见区域，即告诉 OpenGL 应把渲染之后的 图形绘制在窗体的哪个部位。当视见区域是整个窗体时， OpenGL
		 * 将把渲染结果绘制到整个窗口。 参数 X，Y 指定了视见区域的左下角在窗口中的位置，一般情况下为（0，0）， Width 和 Height
		 * 指定了视见区域的宽度和高度。
		 */
		if ( mVideoWidth * height  < width * mVideoHeight ) {
			int mwidth = height * mVideoWidth / mVideoHeight;
			int w = (width - mwidth)/2;
			gl.glViewport(w, 0, mwidth, height);// 设置 OpenGL场景的大小
    	} else if ( mVideoWidth * height  > width * mVideoHeight ) {
    		int mheight = width * mVideoHeight / mVideoWidth;
    		int h = (height- mheight)/2;
    		gl.glViewport(0, h, width, mheight);// 设置 OpenGL场景的大小
    	}
		Log.d("onSurfaceChanged", "onSurfaceChanged中的宽高分别是：width:"+width+" height:"+height);
		
		/**
		 * 设置当前矩阵的模式和说明： ① GL_MODELVIEW,对模型视景矩阵堆栈应用随后的矩阵操作； ②
		 * GL_PROJECTION,对投影矩阵应用随后的矩阵操作； ③ GL_TEXTURE,对纹理矩阵堆栈应用随后的矩阵操作
		 */
		gl.glMatrixMode(GL10.GL_PROJECTION);// 设置投影矩阵
		gl.glLoadIdentity();// 重置投影矩阵(与 glMatrixMode一起使用，该函数的功能是重置当前指定的矩阵为单位矩阵。在glLoadIdentity()之后我们为场景设置了透视图。)
		
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		// 清除屏幕和深度缓存
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		// 重置当前的模型观察矩阵
		gl.glLoadIdentity();
		gl.glTranslatef(0f, 0f, 0f);
		//TODO 尝试着用另一个函数  然后用update int类型数据
		if((mData!=null)){
			mTexture2d.bind(gl,mData,mWidth,mHeight );
			mTexture2d.draw(gl, 0, 0);
			mTexture2d.delete(gl);	
		}

	}

	
}
