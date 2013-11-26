package org.cug.driver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.cug.network.NetService;
import org.cug.util.Settings;
import org.cug.util.SharedPreferencesTool;
import org.cug.util.SysApplication;
import org.cug.util.Tools;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

import java.net.UnknownHostException;

public class LoginActivity extends Activity {

    private EditText idEdit;
    private EditText passEdit;
    private ProgressDialog progressDialog;
    /**
     * 用于控制按钮不能在短时间内连续点击
     */
    private Boolean clicking = false;
    
    
	/* 司机输入的注册信息 */
	private String userID;
	private String userPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        idEdit = (EditText) findViewById(R.id.editText_login_id);
        passEdit = (EditText) findViewById(R.id.editText_login_password);
        initUserInfo();

        Button registerButton = (Button) findViewById(R.id.button_login_register);
        registerButton.setOnClickListener(new RegisterClickListener());

        if (Settings.TESTMODE) {
            Tools.alert(this, "欢迎进入测试模式！");
        } else {
            checkNetWorkState();
        }
    }

    /**
     * 检查网络状态
     */
    private void checkNetWorkState() {
        NetService netService = NetService.getInstance();
        boolean bNetState = netService.checkNet(getBaseContext());
        if (!bNetState) {
            Builder builder = new Builder(LoginActivity.this);
            builder.setTitle("网络错误");
            builder.setMessage("网络连接失败，是否设置网络?");
            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent mIntent = new Intent("/");
                            ComponentName comp = new ComponentName(
                                    "com.android.settings",
                                    "com.android.settings.WirelessSettings");
                            mIntent.setComponent(comp);
                            mIntent.setAction("android.intent.action.VIEW");
                            startActivity(mIntent);
                        }
                    });
            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SysApplication.getInstance().exit();
                        }
                    });
            builder.show();
        }
    }

    /**
     * 第一次打开应用时，读取本地用户信息设置
     */
    private void initUserInfo() {
        SharedPreferencesTool.loadUserInfo(getBaseContext(), "DriverInfo");
        idEdit.setText(Settings.USERID);
        passEdit.setText(Settings.PASSWORD);
    }

    /**
     * 检查输入信息,有问题返回false,没问题返回true
     */
    private Boolean checkInput() {
        // 判断输入是否为空
        if (idEdit.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT)
                    .show();
            return false;
        } else if (passEdit.getText().toString().equals("")) {
            Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        return true;
    }
    
    // 司机登陆------4
    // 格式为:用户ID@用户密码
    // 提交命令：flag=4&columns=ID@PASSWORD&values=%s@%s
	private String makeParameter() {
		String parameter = "";
		userID = idEdit.getText().toString();
		userPassword = passEdit.getText().toString();

		StringBuilder builder = new StringBuilder();
		builder.append("flag=4&columns=ID@PASSWORD&values=");
		builder.append(userID);
		builder.append("@");
		builder.append(userPassword);
		parameter = builder.toString();

		Log.d("LoginActivity", parameter);

		return parameter;
	}
    
    

    /**
     * 注册按钮
     */
    private class RegisterClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)); // fade效果切换窗口
            LoginActivity.this.finish();
        }
    }

    /**
     * 点击登录按钮
     */
    public void onLogin(View view) throws UnknownHostException {

    	
    	//测试，添加用户别名
    	
    	
    	JPushInterface.setAlias(LoginActivity.this, "test1", null);
    	
    	
        if (Settings.TESTMODE) {
            // 同步控制
            if (clicking == false) {
                clicking = true;
            }
            startActivity(new Intent(LoginActivity.this, MapActivity.class)); // fade效果切换窗口

            //RouteDemoActivity
            LoginActivity.this.finish();
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
                    progressDialog = ProgressDialog.show(LoginActivity.this,
                            "登陆中...", "请稍候...", true, false);
                    final NetService netservice = NetService.getInstance();
                    Thread th = new Thread() {
                        @Override
                        public void run() {
                            String result = netservice.getWSReponse(content);
                            Log.d("LoginActivity", result);// 注意result是登录返回的字符串

                            Message msg = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", result); // 往Bundle中存放数据
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
                startActivity(new Intent(LoginActivity.this, MapActivity.class));
                overridePendingTransition(R.anim.fade, R.anim.hold);// fade效果切换窗口
                LoginActivity.this.finish();// 销毁窗口
            } else {
                String resultString = msg.getData().getString("msg");
                if (resultString.contains(Settings.SUCC)) {
                	saveUserInfo(getBaseContext(), "DriverInfo");
                    startActivity(new Intent(LoginActivity.this,
                            MapActivity.class)); // fade效果切换窗口
                    overridePendingTransition(R.anim.fade, R.anim.hold);
                    LoginActivity.this.finish();
                } else  {
                    Tools.alert(LoginActivity.this, "登陆失败,请检查网络或重试!");
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
		editor.commit();// 未调用commit前，数据实际是没有存储进文件中的。 调用后，存储
	}

    /**
     * 增加判断，是否退出系统
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 确认对话框
            final AlertDialog isExit = new Builder(this).create();
            // 对话框标题
            isExit.setTitle("系统提示");
            // 对话框消息
            isExit.setMessage("确定要退出吗?");
            // 实例化对话框上的按钮点击事件监听
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case AlertDialog.BUTTON1: // "确认"按钮退出程序
                            SysApplication.getInstance().exit();
                            break;
                        case AlertDialog.BUTTON2: // "取消"第二个按钮取消对话框
                            isExit.cancel();
                            break;
                        default:
                            break;
                    }
                }
            };
            // 注册监听
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            // 显示对话框
            isExit.show();
            return false;
        }
        return false;
    }
}
