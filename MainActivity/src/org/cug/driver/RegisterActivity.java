package org.cug.driver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.cug.network.NetService;
import org.cug.util.Settings;
import org.cug.util.SharedPreferencesTool;
import org.cug.util.Tools;

import java.net.UnknownHostException;

public class RegisterActivity extends Activity {
	private EditText idEdit;
	private EditText passEdit;
	private EditText nameEdit;
	private EditText phoneEdit;
	private EditText caridEdit;
	private ProgressDialog progressDialog;
	/**
	 * 用于控制按钮不能在短时间内连续点击
	 */
	private Boolean clicking = false;

	/* 司机输入的注册信息 */
	private String userID;
	private String userPassword;
	private String userName;// 车牌照
	private String userPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);

		idEdit = (EditText) findViewById(R.id.editText_register_id);
		passEdit = (EditText) findViewById(R.id.editText_register_password);
		nameEdit = (EditText) findViewById(R.id.editText_register_name);
		phoneEdit = (EditText) findViewById(R.id.editText_register_phone);

		initUserInfo();

		Button clearButton = (Button) findViewById(R.id.button_register_clear);
		clearButton.setOnClickListener(new clearButtonClickListener());

	}

	/**
	 * 第一次打开应用时，读取本地用户信息设置
	 */
	private void initUserInfo() {
		SharedPreferencesTool.loadUserInfo(getBaseContext(), "DriverInfo");
		idEdit.setText(Settings.USERID);
		passEdit.setText(Settings.PASSWORD);
		nameEdit.setText(Settings.USERNAME);
		phoneEdit.setText(Settings.PHONEID);
	}

	/**
	 * 清空按钮
	 */
	private class clearButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			idEdit.setText("");
			passEdit.setText("");
			nameEdit.setText("");
			phoneEdit.setText("");

		}
	}

	/**
	 * 检查输入信息,有问题返回false,没问题返回true
	 */
	private Boolean checkInput() {
		// 判断输入是否为空
		if (idEdit.getText().toString().equals("")) {
			Toast.makeText(RegisterActivity.this, "请输入用户名", Toast.LENGTH_SHORT)
					.show();
			return false;
		} else if (passEdit.getText().toString().equals("")) {
			Toast.makeText(RegisterActivity.this, "请输入密码", Toast.LENGTH_SHORT)
					.show();
			return false;
		} else if (nameEdit.getText().toString().equals("")) {
			Toast.makeText(RegisterActivity.this, "请输入车牌照", Toast.LENGTH_SHORT)
					.show();
			return false;
		} else if (phoneEdit.getText().toString().equals("")) {
			Toast.makeText(RegisterActivity.this, "请输入手机号", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		return true;
	}

	// 司机注册-----3
	// 格式为:用户ID@司机姓名@司机电话@车牌照@密码@推送码
	// 提交命令：flag=3&columns=ID@NAME@TEL@CARCODE@PASSWORD@SENDCODE&values=%s@%s@%s@%s@%s@%s
	private String makeParameter() {
		String parameter = "";

		userID = idEdit.getText().toString();
		userPassword = passEdit.getText().toString();
		userName = nameEdit.getText().toString();
		userPhone = phoneEdit.getText().toString();

		StringBuilder builder = new StringBuilder();
		builder.append("flag=3&columns=ID@NAME@TEL@CARCODE@PASSWORD@SENDCODE&values=");
		builder.append(userID);
		builder.append("@");
		builder.append(userName);
		builder.append("@");
		builder.append(userPhone);
		builder.append("@");
		builder.append(userName);
		builder.append("@");
		builder.append(userPassword);
		builder.append("@");
		builder.append(userID);

		parameter = builder.toString();

		Log.d("RegisterActivity", parameter);

		return parameter;
	}

	/**
	 * 点击注册按钮
	 */
	public void onRegister(View view) throws UnknownHostException {

		if (Settings.TESTMODE) {
			// 同步控制
			if (clicking == false) {
				clicking = true;
			}
			startActivity(new Intent(RegisterActivity.this, MapActivity.class)); // fade效果切换窗口
			RegisterActivity.this.finish();
		} else {
			// 同步控制
			if (clicking == false) {
				clicking = true;
				if (!checkInput()) {
					clicking = false;
					return;
				}
				final String content = makeParameter();// 请求参数
				try {
					progressDialog = ProgressDialog.show(RegisterActivity.this,
							"注册中...", "请稍候...", true, false);
					final NetService netservice = NetService.getInstance();
					Thread th = new Thread() {
						@Override
						public void run() {
							String result = netservice.getWSReponse(content);
							Log.d("result", result);// 注意result是登录返回的字符串

							Message msg = new Message();
							Bundle bundle = new Bundle();
							bundle.putString("msg2", result); // 往Bundle中存放数据
							msg.setData(bundle);// message利用Bundle传递数据

							handler.sendMessage(msg);
						}
					};
					th.start();
				} finally {
					clicking = false;
				}
			}
		}
	}

	/**
	 * 用Handler来更新UI
	 */
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// 关闭ProgressDialog
			progressDialog.dismiss();
			if (Settings.TESTMODE) {
				startActivity(new Intent(RegisterActivity.this,
						MapActivity.class));
				overridePendingTransition(R.anim.fade, R.anim.hold);// fade效果切换窗口
				RegisterActivity.this.finish();// 销毁窗口
			} else {
				String resultString = msg.getData().getString("msg2");
				if (resultString.contains(Settings.SUCC)) {
					saveUserInfo(getBaseContext(), "DriverInfo");
					startActivity(new Intent(RegisterActivity.this,
							MapActivity.class)); // fade效果切换窗口
					overridePendingTransition(R.anim.fade, R.anim.hold);
					RegisterActivity.this.finish();

				} else {
					String[] arr = resultString.split("@");
					String faultString = arr[2];
					Tools.alert(RegisterActivity.this, "注册失败:" + faultString
							+ " 请检查网络或重试!");

				}

			}
		}
	};

	/**
	 * 保存用户信息到配置文件
	 */
	public void saveUserInfo(Context context, String name) {
		SharedPreferences perference = context.getSharedPreferences(name,
				Activity.MODE_PRIVATE);
		Editor editor = perference.edit();
		editor.putString("USERID", userID);// 用户ID
		editor.putString("PASSWORD", userPassword);// 密码
		editor.putString("PHONEID", userPhone);// 手机号
		editor.putString("USERNAME", userName);// 用户名或者车牌照
		editor.commit();// 未调用commit前，数据实际是没有存储进文件中的。 调用后，存储
	}

}
