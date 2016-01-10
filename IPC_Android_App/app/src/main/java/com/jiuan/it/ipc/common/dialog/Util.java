package com.jiuan.it.ipc.common.dialog;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
	public final static String TAG = Util.class.getSimpleName();

	/**
	 * get exter store status if state is Environment.MEDIA_MOUNTED the exter
	 * sotre can read/write
	 * 
	 * @return
	 */
	public static String getExternalStorageState() {
		return Environment.getExternalStorageState();
	}

	/**
	 * get exter store is can read/write
	 * 
	 * @return
	 */
	public static boolean getExternalStoreState() {
		if (Environment.MEDIA_MOUNTED.equals(Util.getExternalStorageState()))
			return true;
		return false;
	}

	/**
	 * get exter store file
	 * 
	 * @return
	 */
	public static File getExternalStorageFile() {
		return Environment.getExternalStorageDirectory();
	}

	/**
	 * get exter store path
	 * 
	 * @return
	 */
	public static String getExternalStoragePath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	/**
	 * judge the list is null or empty
	 * 
	 */
	public static boolean isEmpty(List<? extends Object> list) {
		if (list == null || list.isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * judge the set is null or empty
	 */
	public static boolean isEmpty(Set<? extends Object> set) {
		if (set == null || set.isEmpty())
			return true;
		return false;
	}

	/**
	 * judge the map is null or empty
	 */
	public static boolean isEmpty(Map<? extends Object, ? extends Object> map) {
		if (map == null || map.isEmpty())
			return true;
		return false;
	}

	/**
	 * get the width of the device screen
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * get the height of the device screen
	 * 
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * get the density of the device screen
	 * 
	 * @param context
	 * @return
	 */
	public static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	/**
	 * dip to px
	 * 
	 * @param context
	 * @param px
	 * @return
	 */
	public static int dip2px(Context context, float px) {
		final float scale = getScreenDensity(context);
		return (int) (px * scale + 0.5);
	}

	/**
	 * hide softinput method
	 * 
	 * @param view
	 */
	public static void hideSoftInput(View view) {
		if (view == null)
			return;
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
		}
	}

	/**
	 * show softinput method
	 * 
	 * @param view
	 */
	public static void showSoftInput(View view) {
		if (view == null)
			return;
		InputMethodManager imm = (InputMethodManager) view.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
	}
}
