package org.cug.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.cug.network.NetService;
import org.cug.util.Settings;
import org.cug.util.SharedPreferencesTool;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.LocationSource;

/**
 * 实时上传司机的位置信息
 */
public class GetGPSInfoService extends Service implements AMapLocationListener,
		LocationSource {

	private MyBinder myBinder = new MyBinder();

	private String longitude = null;
	private String latitude = null;

	private LocationManagerProxy mAMapLocManager = null;
	private OnLocationChangedListener mListener;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d("GetGPSInfoService", "onBind");
		return myBinder;
	}

	@Override
	public void onCreate() {
		Log.d("GetGPSInfoService", "onCreate");
		super.onCreate();

		if (mAMapLocManager == null) {
			mAMapLocManager = LocationManagerProxy.getInstance(this);
		}
		mAMapLocManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 10, 5000, this);

	}

	@Override
	public void onDestroy() {
		Log.d("GetGPSInfoService", "onDestroy");
		super.onDestroy();

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

	// 司机上传位置信息----7
	// 格式为：车辆ID@位置坐标@车辆状态@车辆推送码@添加时间@车牌照@电话@司机姓名
	// 指令格式：flag=7&columns=CARID@SHAPE@CARSTATE@CARCODE@TJSJ@CPZ@PHONE@DRIVERNAME&values=*@*@*@*@*@*@*@
	// 返回结果为：
	// 1 成功 @ZDSUC@***
	// 2 失败 @ZDFAL@保存信息失败
	public boolean sendXY() {

		Log.d("sendXY", longitude);
		Log.d("sendXY", latitude);

		NetService netservice = NetService.getInstance();

		SharedPreferences perference = getBaseContext().getSharedPreferences(
				"DriverInfo", Activity.MODE_PRIVATE);
		Settings.USERID = perference.getString("USERID", "");
		Settings.PASSWORD = perference.getString("SSXQNum", "");
		Settings.PHONEID = perference.getString("PHONEID", "");
		Settings.USERNAME = perference.getString("USERNAME", "");

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
		// Log.d("GetGPSInfoService", parameter + "");
		String resultString = netservice.getWSReponse(parameter);
		if (resultString.contains(Settings.SUCC)) {
			Log.d("GetGPSInfoService", "上传位置信息" + resultString);
			return true;
		} else {
			Log.d("GetGPSInfoService", "上传位置信息失败");
			return false;
		}

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

		if (mListener != null) {
			mListener.onLocationChanged(location);
		}

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

			latitude = Double.toString(geoLat);
			longitude = Double.toString(geoLng);

			if (sendXY()) {
				Log.d("GetGPSInfoService", "上传位置信息成功");
			}

		}
	}

	@Override
	public void activate(OnLocationChangedListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

}
