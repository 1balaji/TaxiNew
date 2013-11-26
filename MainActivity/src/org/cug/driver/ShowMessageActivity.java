package org.cug.driver;

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showmessage);

		startName = (TextView) findViewById(R.id.textView_showmessage_strname);
		endName = (TextView) findViewById(R.id.textView_showmessage_endname);
		distance = (TextView) findViewById(R.id.textView_showmeaasge_distance);
		usertime = (TextView) findViewById(R.id.textView_showmeaasge_time);
		submitButton = (Button) findViewById(R.id.button_showmeaasge_submit);
		cancelButton = (Button) findViewById(R.id.button_showmeaasge_cancel);

		Intent intent = getIntent();
		if (null != intent) {
			Bundle bundle = getIntent().getExtras();
			String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					JSONObject extraJson = new JSONObject(extras);
					String valueStr = extraJson.getString("语文");
					Log.d("MyReceiver", valueStr);
					if (null != extraJson && extraJson.length() > 0) {

					}
				} catch (JSONException e) {

				}

			}

		}

	}

}