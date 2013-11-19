package org.cug.amap.location;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import org.cug.driver.R;

public class LocManagerProxyDemoActivity extends Activity implements
        AMapLocationListener {
    private LocationManagerProxy mAMapLocManager = null;

    private TextView myLocation;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            myLocation.setText((String) msg.obj);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        myLocation = (TextView) findViewById(R.id.myLocation);
        mAMapLocManager = LocationManagerProxy.getInstance(this);
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

    public void disableMyLocation() {
        mAMapLocManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableMyLocation();
    }

    @Override
    protected void onPause() {
        disableMyLocation();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAMapLocManager != null) {
            mAMapLocManager.removeUpdates(this);
            mAMapLocManager.destory();
        }
        mAMapLocManager = null;
        super.onDestroy();
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

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            Double geoLat = location.getLatitude();
            Double geoLng = location.getLongitude();
            String cityCode = "";
            String desc = "";
            Bundle locBundle = location.getExtras();
            if (locBundle != null) {
                cityCode = locBundle.getString("citycode");
                desc = locBundle.getString("desc");
            }
            String str = ("定位成功:(" + geoLng + "," + geoLat + ")"
                    + "\n精    度    :" + location.getAccuracy() + "米"
                    + "\n城市编码:" + cityCode + "\n位置描述:" + desc);
            Message msg = new Message();
            msg.obj = str;
            if (handler != null) {
                handler.sendMessage(msg);
            }
        }
    }
}
