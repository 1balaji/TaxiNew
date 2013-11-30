package org.cug.driver;

import java.util.ArrayList;
import java.util.List;

import org.cug.network.NetService;
import org.cug.util.SQLiteTool;
import org.cug.util.Settings;
import org.cug.util.SharedPreferencesTool;
import org.cug.util.SysApplication;
import org.cug.util.Tools;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import cn.jpush.android.api.JPushInterface;

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

	private JSONObject extraJson = new JSONObject();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showmessage);

		// 设置程序完全退出
		SysApplication.getInstance().addActivity(this);

		startName = (TextView) findViewById(R.id.textView_showmessage_strname);
		endName = (TextView) findViewById(R.id.textView_showmessage_endname);
		distance = (TextView) findViewById(R.id.textView_showmeaasge_distance);
		usertime = (TextView) findViewById(R.id.textView_showmeaasge_time);
		submitButton = (Button) findViewById(R.id.button_showmeaasge_submit);
		submitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (null != extraJson && extraJson.length() > 0) {
					sendHandleTaxiRequest();
				} else {
					Tools.alert(getBaseContext(), "当前无订单信息！");
					return;
				}

			}
		});
		cancelButton = (Button) findViewById(R.id.button_showmeaasge_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		if (Settings.TESTMODE) {
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

	/**
	 * 获取推送来的消息
	 */
	private boolean getSendMessage() {
		boolean isResult = false;
		getIntent();
		Bundle bundle = getIntent().getExtras();
		if (null != bundle) {

			bundle.getString(JPushInterface.EXTRA_MESSAGE);
			String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
			if (!ExampleUtil.isEmpty(extras)) {
				try {
					extraJson = new JSONObject(extras);
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

	/**
	 * 向后台发送抢单请求
	 */
	private void sendHandleTaxiRequest() {

		String content = makeParameter();// 请求参数
		NetService netservice = NetService.getInstance();
		String resultString = netservice.getWSReponse(content);
		if (resultString.contains(Settings.SUCC)) {
			// goToMapActivity();
			Tools.alert(ShowMessageActivity.this, "已成功抢单！");
			routeInfoIntoDb(extraJson);
			ShowMessageActivity.this.finish();
		} else {
			Tools.alert(ShowMessageActivity.this, "未能成功抢单,请检查网络或重试!");
		}

	}

	/**
	 * 格式为:司机ID@接受订单的时间@订单ID@订单标识@发布人ID 提交命令：flag=9&columns=RECEIVEDRIVERID@RECEIVETIME@ORDERID@ORDERIDS@USERID
	 * &values=*@*@*@
	 */
	private String makeParameter() {

		SharedPreferencesTool.loadUserInfo(getBaseContext(), "DriverInfo");

		String parameter = "";
		StringBuilder builder = new StringBuilder();
		builder.append("flag=9&columns=RECEIVEDRIVERID@RECEIVETIME@ORDERID@ORDERIDS@USERID&values=");
		builder.append(Settings.USERID);
		builder.append("@");
		try {
			builder.append(extraJson.getString("NEEDTIME"));
			builder.append("@");
			builder.append(extraJson.getString("ORDERIDS"));
			builder.append("@");
			builder.append(extraJson.getString("ORDERID"));
			builder.append("@");
			builder.append(extraJson.getString("USERID"));

			parameter = builder.toString();
			Log.d("ShowMessageActivity", parameter);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return parameter;
	}

	/**
	 * 把抢单成功的打车信息存入数据库
	 */
	public void routeInfoIntoDb(JSONObject extraJson) {

		List<String> content = new ArrayList<String>();

		SQLiteTool sqLiteTool = new SQLiteTool();

		try {
			String shapeStart = extraJson.getString("SHAPESTART");
			String[] shapeStartArr = shapeStart.split(",");
			String startlat = shapeStartArr[0];
			String startlon = shapeStartArr[1];

			content.add(startlat);
			content.add(startlon);
			content.add(extraJson.getString("STARTADDRESS"));

			String shapeEnd = extraJson.getString("SHAPEEND");
			String[] shapeEndArr = shapeEnd.split(",");
			String endlat = shapeEndArr[0];
			String endlon = shapeEndArr[1];

			content.add(endlat);
			content.add(endlon);
			content.add(extraJson.getString("ENDADDRESS"));

			content.add(extraJson.getString("DISTANCE"));
			content.add(extraJson.getString("NEEDTIME"));

			content.add("姓名");
			content.add(extraJson.getString("USERPHONE"));
			content.add(extraJson.getString("USERID"));

			content.add(extraJson.getString("ORDERIDS"));
			content.add(extraJson.getString("issuccessed"));

			sqLiteTool.insertContent(content, "passengerinfo");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}