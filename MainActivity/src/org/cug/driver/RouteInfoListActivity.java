package org.cug.driver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.cug.util.SQLiteTool;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: liulin
 * Date: 13-5-14
 * Time: 上午11:58
 * To change this template use File | Settings | File Templates.
 */
public class RouteInfoListActivity extends Activity {

    private ListView listView;
    private ArrayList<HashMap<String, Object>> listItem;
    private SimpleAdapter listItemAdapter;// 列表是适配器

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roteinfolist);
        listView = (ListView) findViewById(R.id.routeInfoList);
        listItem = new ArrayList<HashMap<String, Object>>();
        SQLiteTool dbTool = new SQLiteTool();
        listItem = dbTool.getRouteInfoList();

        // 生成适配器的Item和动态数组对应的元素
        listItemAdapter = new SimpleAdapter(this, listItem,
                R.layout.list_route_item,
                new String[]{"startname", "endname", "passengerphone", "timetext"}, new int[]{
                R.id.item_name, R.id.item_detail, R.id.item_distance, R.id.item_fee});


        listView.setAdapter(listItemAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                HashMap<String, Object> map = (HashMap<String, Object>) listView
                        .getItemAtPosition(arg2);
                Double startLon = Double.valueOf(map.get("startlon").toString());
                Double startLat = Double.valueOf(map.get("startlat").toString());
                Double endLon = Double.valueOf(map.get("endlon").toString());
                Double endlat = Double.valueOf(map.get("endlat").toString());
                Intent intent = new Intent();
                intent.putExtra("startPointLon", startLon);
                intent.putExtra("startPointLat", startLat);
                intent.putExtra("endPointLon", endLon);
                intent.putExtra("endPointLat", endlat);
                RouteInfoListActivity.this.setResult(RESULT_OK, intent);
                RouteInfoListActivity.this.finish();


            }
        });


    }


    /**
     * 返回按键重写
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 这里重写返回键
            Intent intent = new Intent();
            RouteInfoListActivity.this.setResult(RESULT_OK, intent);
            RouteInfoListActivity.this.finish();
            return true;
        }
        return false;
    }
}



