package org.cug.amap.location;

import org.cug.amap.util.AMapUtil;
import org.cug.driver.R;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;

/**
 * AMapV2地图中简单介绍定位.
 */
public class LocationSourceDemoActivity extends FragmentActivity implements
		LocationSource, AMapLocationListener {
	private AMap aMap;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_demo);
		init();
	}

	/**
	 * 初始化AMap对象
	 */
	private void init() {
		if (aMap == null) {
			aMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			if (AMapUtil.checkReady(this, aMap)) {
				setUpMap();
			}
		}

	}

	private void setUpMap() {
		mAMapLocationManager = LocationManagerProxy
				.getInstance(LocationSourceDemoActivity.this);
		aMap.setLocationSource(this);
		aMap.setMyLocationEnabled(true);

	}

	@Override
	protected void onPause() {
		super.onPause();
		deactivate();
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation aLocation) {
		if (mListener != null) {
			mListener.onLocationChanged(aLocation);
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mAMapLocationManager == null) {
			mAMapLocationManager = LocationManagerProxy.getInstance(this);
		}
		mAMapLocationManager.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 10, 5000, this);
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		mAMapLocationManager.removeUpdates(this);
		mAMapLocationManager.destory();
		mAMapLocationManager = null;
	}
}
