package org.cug.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.cug.driver.ShowMessageActivity;
import org.cug.network.NetService;
import org.cug.util.Settings;
import org.cug.util.SharedPreferencesTool;
import org.cug.util.Tools;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * 实时上传司机的位置信息
 */
public class GetGPSInfoService extends Service implements AMapLocationListener {

	private MyBinder myBinder = new MyBinder();
	private Timer timer;
	private TimerTask timertask;
	private LocationManagerProxy mAMapLocManager = null;
	private String longitude = null;
	private String latitude = null;


	@Override
	public IBinder onBind(Intent intent) {
		Log.d("GetGPSInfoService", "onBind");
		return myBinder;
	}

	@Override
	public void onCreate() {
		Log.d("GetGPSInfoService", "onCreate");
		super.onCreate();
		timer = new Timer();
		timertask = new GpsTask();
		timer.schedule(timertask, 0, 180000);// 三分钟触发一次

	}

	@Override
	public void onDestroy() {
		Log.d("GetGPSInfoService", "onDestroy");
		super.onDestroy();
		cancelTask();
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d("GetGPSInfoService", "onRebind");
		super.onRebind(intent);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("GetGPSInfoService", "onStart");
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d("GetGPSInfoService", "onUnbind");
		return super.onUnbind(intent);
	}

	public class MyBinder extends Binder {
		public GetGPSInfoService getService() {
			return GetGPSInfoService.this;
		}
	}

	public boolean enableMyLocation() {
		boolean result = false;
		if (mAMapLocManager
				.isProviderEnabled(LocationProviderProxy.AMapNetwork)) {
			mAMapLocManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 10, this);
			result = true;
		}
		return result;
	}
	
	// 司机上传位置信息----7
	// 格式为：车辆ID@位置坐标@车辆状态@车辆推送码@添加时间@车牌照@电话@司机姓名
	// 指令格式：flag=7&columns=CARID@SHAPE@CARSTATE@CARCODE@TJSJ@CPZ@PHONE@DRIVERNAME&values=*@*@*@*@*@*@*@
	// 返回结果为：
	// 1 成功 @ZDSUC@***
	// 2 失败 @ZDFAL@保存信息失败
	public boolean sendXY() {
		NetService netservice = NetService.getInstance();
		SharedPreferencesTool.loadUserInfo(getBaseContext(), "DriverInfo");
		// 时间
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		Date curDate = new Date(System.currentTimeMillis());
		String strDate = formatter.format(curDate);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("flag=7&columns=CARID@SHAPE@CARSTATE@CARCODE@TJSJ@CPZ@PHONE@DRIVERNAME&values=");
		stringBuilder.append(Settings.USERID);// 司机用户ID
		stringBuilder.append("@");
		stringBuilder.append(longitude + "," + latitude);
		stringBuilder.append("@");
		stringBuilder.append("true");
		stringBuilder.append("@");
		stringBuilder.append(Settings.USERID);// 车辆推送码,即司机用户ID
		stringBuilder.append("@");
		stringBuilder.append(strDate);
		stringBuilder.append("@");
		stringBuilder.append(Settings.USERNAME);// 车牌照
		stringBuilder.append("@");
		stringBuilder.append(Settings.PHONEID);// 电话
		stringBuilder.append("@");
		stringBuilder.append(Settings.USERNAME);// 司机姓名
		String parameter = stringBuilder.toString();
		Log.d("GetGPSInfoService", parameter + "");
		String resultString = netservice.getWSReponse(parameter);
		if (resultString.contains(Settings.SUCC)) {
			Log.d("GetGPSInfoService", "上传位置信息" + resultString);
			return true;
		} else {
			Log.d("GetGPSInfoService", "上传位置信息失败");
			return false;
		}

	}

	/**
	 * 上传GPS信息的定时器
	 * 
	 */
	class GpsTask extends TimerTask {
		public void run() {
			System.out.format("GetGPSInfoService up!");
			if (enableMyLocation()) {
				Log.d("GetGPSInfoService", "获取到位置信息");
				sendXY();
			} else {
				Log.d("GetGPSInfoService", "没有获取到位置信息");
				return;
			}
		}
	}

	/**
	 * 关闭定时器
	 */
	public void cancelTask() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (timertask != null) {
			timertask.cancel();
			timertask = null;
		}
		Log.d("GetGPSInfoService", "关闭GPS服务");
	}

	public void disableMyLocation() {
		mAMapLocManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation location) {
		// TODO Auto-generated method stub
		if (location != null) {
			Double geoLat = location.getLatitude();
			Double geoLng = location.getLongitude();

			Log.d("GetGPSInfoService", Double.toString(geoLat));
			Log.d("GetGPSInfoService", Double.toString(geoLng));

			String cityCode = "";
			String desc = "";
			Bundle locBundle = location.getExtras();
			if (locBundle != null) {
				cityCode = locBundle.getString("citycode");
				desc = locBundle.getString("desc");
			}

			// String str = ("定位成功:(" + geoLng + "," + geoLat + ")"
			// + "\n精    度    :" + location.getAccuracy() + "米"
			// + "\n城市编码:" + cityCode + "\n位置描述:" + desc);

			String str = Double.toString(geoLat) + "@"
					+ Double.toString(geoLng);

			Message msg = new Message();
			msg.obj = str;
			if (handler != null) {
				handler.sendMessage(msg);
			}
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String LatLngStr = (String) msg.obj;
			String[] arr = LatLngStr.split("@");
			longitude = arr[1];
			latitude = arr[0];
		}
	};
}
