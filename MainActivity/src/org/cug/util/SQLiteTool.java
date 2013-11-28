package org.cug.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SQLiteTool {

    SQLiteDatabase mDb;
    private final static String DATABASE_NAME = "dache.db";
    private final static String TABLE_NAME = "passengerinfo";
    
    public final static String FIELD1 = "startlat";
    public final static String FIELD2 = "startlon";
    public final static String FIELD3 = "startname";
    
    public final static String FIELD4 = "endlat";
    public final static String FIELD5 = "endlon";
    public final static String FIELD6 = "endname";
    
    public final static String FIELD7 = "distance";
    public final static String FIELD8 = "timetext";
    
    public final static String FIELD9 = "passengername";
    public final static String FIELD10 = "passengerphone";
    public final static String FIELD11 = "passengerid";
    
    public final static String FIELD12 = "routeinfoid";
    public final static String FIELD13 = "issuccessed";


    public final static String[] columns = {FIELD1, FIELD2, FIELD3, FIELD4, FIELD5, FIELD6, FIELD7, FIELD8, FIELD9, FIELD10, FIELD11, FIELD12, FIELD13};


    String filename;

    public SQLiteTool() {
        filename = android.os.Environment.getExternalStorageDirectory()
                + Settings.DBDIRECTORY + DATABASE_NAME;
        mDb = SQLiteDatabase.openOrCreateDatabase(filename, null);

        try {
            mDb.execSQL("create table if not exists " + TABLE_NAME
                    + " ( " + FIELD1
                    + " text not null, " + FIELD2 + " text not null," + FIELD3 + " text not null," + FIELD4 + " text not null," + FIELD5 + " text not null," + FIELD6 + " text not null," + FIELD7 + " text not null,"
                    + FIELD8 + " text not null," + FIELD9 + " text not null," + FIELD10 + " text not null," + FIELD11 + " text not null," + FIELD12 + " text not null," + FIELD13 + " text not null)");
        } catch (SQLException e) {
            Log.w("MessageLogDao", e.getMessage());
        }
    }

    /**
     * 向表中插入信息
     * 字段名字
     */
    public boolean insertContent(List content, String tableName) {

        ContentValues values = new ContentValues();
        for (int i = 0; i < columns.length; i++) {
            values.put(columns[i], content.get(i).toString());
        }

        try {
            if (-1 == mDb.insert(tableName, null, values))
                return false;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    //获取路线信息列表
    public ArrayList<HashMap<String, Object>> getRouteInfoList() {
        ArrayList<HashMap<String, Object>> arraylist = new ArrayList<HashMap<String, Object>>();
        try {
            Cursor c = mDb.rawQuery("select * from " + TABLE_NAME
                    , null);
            // 获取表的内容
            while (c.moveToNext()) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                for (int i = 0; i < columns.length; i++) {
                    map.put(columns[i], c.getString(i));
                }
                arraylist.add(map);
            }
        } catch (Exception e) {
            return null;
        }
        return arraylist;
    }


    /**
     * 清空所有表格
     *
     * @param tableName
     * @return
     */
    public boolean clearTable(String tableName) {

        try {
            if (mDb == null)
                return false;
            mDb.delete(TABLE_NAME, "", new String[]{""});
        } catch (SQLException e) {
            return false;
        }
        return true;

    }

    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

}
