package org.cug.amap.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;

/**
 * Created with IntelliJ IDEA.
 * User: liulin
 * Date: 13-4-27
 * Time: 上午10:39
 * To change this template use File | Settings | File Templates.
 */
public class LocationOverLay implements LocationSource, AMapLocationListener {

    private AMap mMap;
    private Context mContext;
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;


    public LocationOverLay(Context context, AMap map) {
        mContext = context;
        mMap = map;


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
            mAMapLocationManager = LocationManagerProxy.getInstance(mContext);
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
