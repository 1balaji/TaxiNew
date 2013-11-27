package org.cug.driver;

import org.cug.util.Settings;
import org.cug.util.Tools;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jpush.android.api.JPushInterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA. User: liulin Date: 13-5-23 Time: 下午1:07 To change
 * this template use File | Settings | File Templates.
 */
public class ShowMessageActivity extends Activity {

	private TextView startName;
	private TextView endName;
	private TextView distance;
	private TextView usertime;
	private Button submitButton;
	private Button cancelButton;

	private String startNameStr;
	private String endNameStr;
	private String distanceStr;
	private String usertimeStr;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showmessage);

		startName = (TextView) findViewById(R.id.textView_showmessage_strname);
		endName = (TextView) findViewById(R.id.textView_showmessage_endname);
		distance = (TextView) findViewById(R.id.textView_showmeaasge_distance);
		usertime = (TextView) findViewById(R.id.textView_showmeaasge_time);
		submitButton = (Button) findViewById(R.id.button_showmeaasge_submit);
		cancelButton = (Button) findViewById(R.id.button_showmeaasge_cancel);

		if (!Settings.TESTMODE) {
			startName.setText("南湖名都(A)区");
			endName.setText("华中科技大学培训中心");
			distance.setText("645982米");
			usertime.setText("2012年01月12日 15时47分");

		} else {

			if (getSendMessage()) {
				startName.setText(startNameStr);
				endName.setText(endNameStr);
				distance.setText(distanceStr);
				usertime.setText(usertimeStr);
			} else {

				Tools.alert(getBaseContext(), "无新的推送信息！");
			}
		}

	}

	// 接收推送的自定义消息
	private boolean getSendMessage() {
		boolean isResult = false;
		Intent intent = getIntent();
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {

			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					JSONObject extraJson = new JSONObject(extras);
					if (null != extraJson && extraJson.length() > 0) {
						startNameStr = extraJson.getString("STARTADDRESS");
						endNameStr = extraJson.getString("ENDADDRESS");
						distanceStr = extraJson.getString("DISTANCE");
						usertimeStr = extraJson.getString("NEEDTIME");
						isResult = true;
					}
				} catch (JSONException e) {
				}
			}
		}
		return isResult;
	}

}