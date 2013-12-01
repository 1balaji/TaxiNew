package org.cug.bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.cug.network.NetService;

import android.app.Activity;
import android.content.Context;
import android.location.Address;

import com.amap.api.search.core.AMapException;
import com.amap.api.search.core.LatLonPoint;
import com.amap.api.search.geocoder.Geocoder;
import com.amap.api.search.route.Route;

/**
 * Created with IntelliJ IDEA. User: liulin Date: 13-4-25 Time: 上午10:48 To
 * change this template use File | Settings | File Templates.
 */
public class GetInfoListTool {

	static NetService netservice = NetService.getInstance();

	public static int getDistance(LatLonPoint startPoint, LatLonPoint endPoint,
			Context context) {
		Route.FromAndTo fromAndTo = new Route.FromAndTo(startPoint, endPoint);
		List<Route> routeResult = null;
		Route route;
		int distance = 0;
		try {
			routeResult = Route.calculateRoute((Activity) context, fromAndTo,
					Route.DrivingDefault);
		} catch (AMapException e) {
			e.printStackTrace();
		}
		if (routeResult != null && routeResult.size() > 0) {
			route = routeResult.get(0);
			distance = route.getLength();
		}
		return distance;
	}

	public static String getPointName(double mLat, double mLon) {
		String addressName = null;
		Geocoder coder = null;
		List<Address> address = null;
		try {
			address = coder.getFromLocation(mLat, mLon, 3);
		} catch (AMapException e) {
			e.printStackTrace();
		}
		if (address != null && address.size() > 0) {
			Address addres = address.get(0);
			addressName = addres.getAdminArea() + addres.getSubLocality()
					+ addres.getFeatureName() + "附近";
		}

		return addressName;
	}

	// 获取当前时间
	public static String getTimeStr(Date date) {
		String time;
		// 设置时间格式
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");// "yyyy-MM-dd HH:mm"
		time = format.format(date);
		return time;
	}

}
