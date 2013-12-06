package org.cug.driver;

import java.util.List;

import org.cug.amap.jpush.ExampleUtil;
import org.cug.amap.route.RouteOverlay;
import org.cug.amap.util.AMapUtil;
import org.cug.amap.util.Constants;
import org.cug.services.GetGPSInfoService;
import org.cug.util.Settings;
import org.cug.util.SharedPreferencesTool;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.jpush.android.api.JPushInterface;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.search.core.AMapException;
import com.amap.api.search.core.LatLonPoint;
import com.amap.api.search.poisearch.PoiPagedResult;
import com.amap.api.search.route.Route;

public class MapActivity extends FragmentActivity implements LocationSource,
		AMapLocationListener, InfoWindowAdapter {

	private final static int ITEM0 = Menu.FIRST;
	private final static int ITEM1 = Menu.FIRST + 1;
	private final static int ITEM2 = Menu.FIRST + 2;
	private final static int ITEM3 = Menu.FIRST + 3;

	// 用于startActivityForResult
	private static final int CarList = 0;
	private static final int RoutePlan = 1;
	private static final int RouteList = 2;

	private AMap aMap;
	private OnLocationChangedListener mListener;
	private LocationManagerProxy mAMapLocationManager;

	// 定位坐标值
	private Double geoLat;
	private Double geoLng;

	// 选择终点
	private PoiPagedResult endSearchResult;
	private String strEnd;
	private String strStart;
	private ProgressDialog progDialog;

	// 选取路线
	private LatLonPoint startPoint = null;
	private LatLonPoint endPoint = null;
	private List<Route> routeResult;
	private LinearLayout routeNav;
	private ImageButton routePre, routeNext;
	private RouteOverlay routeOverlay;
	private Route route;
	private Marker startMk;

	private int distance;

	public static boolean isForeground = false;

	private Intent GPSServiceIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		init();
		initEndTextView();

		// 初始化JPush推送
		initJpush();
		registerMessageReceiver();

	}

	/*
	 * 以下为初始化JPush部分
	 */

	// 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
	private void initJpush() {

		SharedPreferences perference = getBaseContext().getSharedPreferences(
				"DriverInfo", Activity.MODE_PRIVATE);

		String CarID = perference.getString("USERID", "");

		JPushInterface.setAlias(getApplicationContext(), CarID, null);
		JPushInterface.init(getApplicationContext());
	}

	// for receive customer msg from jpush server
	private MessageReceiver mMessageReceiver;
	public static final String MESSAGE_RECEIVED_ACTION = "org.cug.driver.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";

	public void registerMessageReceiver() {
		mMessageReceiver = new MessageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		filter.addAction(MESSAGE_RECEIVED_ACTION);
		registerReceiver(mMessageReceiver, filter);
	}

	public class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
				String messge = intent.getStringExtra(KEY_MESSAGE);
				String extras = intent.getStringExtra(KEY_EXTRAS);
				StringBuilder showMsg = new StringBuilder();
				showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
				if (!ExampleUtil.isEmpty(extras)) {
					showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
				}

			}
		}
	}

	/*
	 * 以上为初始化JPush部分
	 */

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
				.getInstance(MapActivity.this);
		aMap.setLocationSource(this);
		aMap.setMyLocationEnabled(true);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// deactivate();
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
		if (aLocation != null) {
			geoLat = aLocation.getLatitude();
			geoLng = aLocation.getLongitude();
			String cityCode = "";
			String desc = "";
			Bundle locBundle = aLocation.getExtras();
			if (locBundle != null) {
				cityCode = locBundle.getString("citycode");
				desc = locBundle.getString("desc");
			}
			String str = ("定位成功:(" + geoLng + "," + geoLat + ")"
					+ "\n精    度    :" + aLocation.getAccuracy() + "米"
					+ "\n城市编码:" + cityCode + "\n位置描述:" + desc);
			Message msg = new Message();
			msg.obj = str;

			// if (Settings.TESTMODE) {
			// GPSServiceIntent = new Intent(MapActivity.this,
			// GetGPSInfoService.class);
			// startService(GPSServiceIntent);
			// }

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

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		deactivate();
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

	// 初始化终点
	public void initEndTextView() {

		routePre = (ImageButton) findViewById(R.id.pre_index);
		routePre.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (routeOverlay != null) {
					boolean enablePre = routeOverlay.showPrePopInfo();
					if (!enablePre) {
						routePre.setBackgroundResource(R.drawable.prev_disable);
						routeNext
								.setBackgroundResource(R.drawable.btn_route_next);
					} else {
						routePre.setBackgroundResource(R.drawable.btn_route_pre);
						routeNext
								.setBackgroundResource(R.drawable.btn_route_next);
					}
				}
			}
		});
		routeNext = (ImageButton) findViewById(R.id.next_index);
		routeNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (routeOverlay != null) {
					boolean enableNext = routeOverlay.showNextPopInfo();
					if (!enableNext) {
						routePre.setBackgroundResource(R.drawable.btn_route_pre);
						routeNext
								.setBackgroundResource(R.drawable.next_disable);
					} else {
						routePre.setBackgroundResource(R.drawable.btn_route_pre);
						routeNext
								.setBackgroundResource(R.drawable.btn_route_next);
					}
				}
			}
		});

		routeNav = (LinearLayout) findViewById(R.id.LinearLayoutLayout_index_bottom);
	}

	// 初始化起點為定位點
	public void initStartPoint() {

		if (geoLat != null) {
			startMk = aMap.addMarker(new MarkerOptions()
					.anchor(0.5f, 1)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.point))
					.position(new LatLng(geoLat, geoLng)).title("所在位置"));
			startMk.showInfoWindow();
		}

	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	// 路线规划
	public void searchRouteResult(LatLonPoint startPoint, LatLonPoint endPoint) {
		progDialog = ProgressDialog.show(MapActivity.this, null, "正在获取线路",
				true, true);
		final Route.FromAndTo fromAndTo = new Route.FromAndTo(startPoint,
				endPoint);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					routeResult = Route.calculateRoute(MapActivity.this,
							fromAndTo, Route.DrivingDefault);
					if (progDialog.isShowing()) {
						if (routeResult != null || routeResult.size() > 0)
							routeHandler.sendMessage(Message
									.obtain(routeHandler,
											Constants.ROUTE_SEARCH_RESULT));
					}
				} catch (AMapException e) {
					Message msg = new Message();
					msg.what = Constants.ROUTE_SEARCH_ERROR;
					msg.obj = e.getErrorMessage();
					routeHandler.sendMessage(msg);
				}
			}
		});
		t.start();

	}

	private Handler routeHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.ROUTE_SEARCH_RESULT) {
				progDialog.dismiss();
				if (routeResult != null && routeResult.size() > 0) {
					route = routeResult.get(0);
					distance = route.getLength();
					if (route != null) {
						routeOverlay = new RouteOverlay(MapActivity.this, aMap,
								route);
						routeOverlay.removeFormMap();
						routeOverlay.addMarkerLine();
						routeNav.setVisibility(View.VISIBLE);
						routePre.setBackgroundResource(R.drawable.prev_disable);
						routeNext
								.setBackgroundResource(R.drawable.btn_route_next);

					}
				}

			} else if (msg.what == Constants.ROUTE_SEARCH_ERROR) {
				progDialog.dismiss();
				showToast((String) msg.obj);
			}
		}
	};

	public void showToast(String showString) {
		Toast.makeText(getApplicationContext(), showString, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CarList:

			break;

		case RoutePlan:
			if (data.hasExtra("startPointLon")) {
				Bundle extras = data.getExtras();
				double startPointLon = extras.getDouble("startPointLon");
				double startPointLat = extras.getDouble("startPointLat");
				double endPointLon = extras.getDouble("endPointLon");
				double endPointLat = extras.getDouble("endPointLat");
				LatLonPoint startPoi = new LatLonPoint(startPointLat,
						startPointLon);
				LatLonPoint endPoi = new LatLonPoint(endPointLat, endPointLon);
				aMap.clear();
				searchRouteResult(startPoi, endPoi);
			}
			break;
		}
	}

	// Option Menu
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, ITEM0, 0, "推送信息");
		menu.add(0, ITEM1, 0, "实时路况");
		menu.add(0, ITEM2, 0, "任务列表");
		menu.add(0, ITEM3, 0, "配置信息");

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM0:
			startActivity(new Intent(MapActivity.this,
					ShowMessageActivity.class));
			break;
		case ITEM1:
			// initStartPoint();
			aMap.setTrafficEnabled(true);
			break;
		case ITEM2:
			Intent intent3 = new Intent(MapActivity.this,
					RouteInfoListActivity.class);
			startActivityForResult(intent3, RoutePlan);
			break;
		case ITEM3:
			startActivity(new Intent(MapActivity.this, RegisterActivity.class));
			break;
		}
		return true;
	}

	// 画单独的点
	public void drawMarker(double lat, double lon, int iconsource, String title) {

		LatLng markeri = new LatLng(lat, lon);
		aMap.addMarker(new MarkerOptions().position(markeri).title(title)
				.icon(BitmapDescriptorFactory.fromResource(iconsource)));

	}

}
