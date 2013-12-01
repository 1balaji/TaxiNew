package org.cug.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesTool {

	private static SharedPreferencesTool instance = null;

	public static SharedPreferencesTool getInstance() {
		if (instance == null) {
			instance = new SharedPreferencesTool();
		}
		return instance;
	}

	/**
	 * 保存用户信息到配置文件
	 */
	public static void saveUserInfo(Context context, String name) {

		SharedPreferences perference = context.getSharedPreferences(name,
				Activity.MODE_PRIVATE);
		Editor editor = perference.edit();

		editor.putString("USERID", Settings.USERID);// 用户ID
		editor.putString("PASSWORD", Settings.PASSWORD);// 密码
		editor.putString("PHONEID", Settings.PHONEID);// 手机号
		editor.putString("USERNAME", Settings.USERNAME);// 用户名或者车牌照
		editor.commit();// 未调用commit前，数据实际是没有存储进文件中的。 调用后，存储

	}

	/**
	 * 第一次打开应用时，读取本地用户信息设置
	 */
	public static void loadUserInfo(Context context, String name) {
		SharedPreferences perference = context.getSharedPreferences(name,
				Activity.MODE_PRIVATE);
		Settings.USERID = perference.getString("USERID", "");
		Settings.PASSWORD = perference.getString("SSXQNum", "");
		Settings.PHONEID = perference.getString("PHONEID", "");
		Settings.USERNAME = perference.getString("USERNAME", "");
	}

	/**
	 * 清空SharedPreferences中保存的信息
	 */
	public static void clearSharedPreferences(Context context, String name) {

		SharedPreferences perference = context.getSharedPreferences(name,
				Activity.MODE_PRIVATE);
		final Editor editor = perference.edit();
		editor.clear();
		editor.commit();
	}

}
