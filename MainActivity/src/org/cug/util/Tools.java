package org.cug.util;

import android.content.Context;
import android.widget.Toast;

/**
 * @author 柳恒建
 */
public class Tools {

	public static void alert(Context context, String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * true 代表短时间提示，false代表长时间提示
	 */
	public static void alert(Context context, String msg, Boolean flag) {
		if (flag)
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

}
