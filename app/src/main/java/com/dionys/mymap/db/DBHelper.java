package com.dionys.mymap.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dionys.mymap.entity.PathRecord;
import com.dionys.mymap.util.LocUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHelper {

    public static final String KEY_ROWID = "id";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_SPEED = "averagespeed";
    public static final String KEY_LINE = "pathline";
    public static final String KEY_STRAT = "stratpoint";
    public static final String KEY_END = "endpoint";
    public static final String KEY_DATE = "date";
    private final static String DATABASE_PATH = android.os.Environment
            .getExternalStorageDirectory().getAbsolutePath() + "/recordPath";
    static final String DATABASE_NAME = DATABASE_PATH + "/" + "record.db";
    private static final int DATABASE_VERSION = 1;
    private static final String RECORD_TABLE = "record";
    private static final String RECORD_CREATE = "create table if not exists record("
            + KEY_ROWID + " integer primary key autoincrement,"
            + KEY_STRAT + " STRING,"
            + KEY_END + " STRING,"
            + KEY_LINE + " STRING,"
            + KEY_DISTANCE + " STRING,"
            + KEY_DURATION + " STRING,"
            + KEY_SPEED + " STRING,"
            + KEY_DATE +" STRING" + ");";

    public static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(RECORD_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private Context mCtx;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public DBHelper(Context ctx) {
        this.mCtx = ctx;
        dbHelper = new DatabaseHelper(mCtx);
    }

    public DBHelper open() throws SQLException {

        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public Cursor getall() {
        return db.rawQuery("SELECT * FROM record", null);
    }

    // remove an entry
    public boolean delete(long rowId) {

        return db.delete(RECORD_TABLE, "id=" + rowId, null) > 0;
    }


    /**
     * 向数据库存入一条轨迹
     *
     */
    public long createrecord(String distance, String duration,
                             String averagespeed, String pathline, String stratpoint,
                             String endpoint, String date) {
        ContentValues args = new ContentValues();
        args.put("distance", distance);
        args.put("duration", duration);
        args.put("averagespeed", averagespeed);
        args.put("pathline", pathline);
        args.put("stratpoint", stratpoint);
        args.put("endpoint", endpoint);
        args.put("date", date);
        return db.insert(RECORD_TABLE, null, args);
    }

    /**
     * 查询所有轨迹记录
     *
     * @return
     */
    public List<PathRecord> queryRecordAll() {
        List<PathRecord> allRecord = new ArrayList<>();
        Cursor allRecordCursor = db.query(RECORD_TABLE, getColumns(), null,
                null, null, null, null);
        while (allRecordCursor.moveToNext()) {
            PathRecord record = new PathRecord();
            record.setId(allRecordCursor.getInt(allRecordCursor
                    .getColumnIndex(DBHelper.KEY_ROWID)));
            record.setDistance(allRecordCursor.getString(allRecordCursor
                    .getColumnIndex(DBHelper.KEY_DISTANCE)));
            record.setDuration(allRecordCursor.getString(allRecordCursor
                    .getColumnIndex(DBHelper.KEY_DURATION)));
            record.setDate(allRecordCursor.getString(allRecordCursor
                    .getColumnIndex(DBHelper.KEY_DATE)));
            String lines = allRecordCursor.getString(allRecordCursor
                    .getColumnIndex(DBHelper.KEY_LINE));
            record.setPathline(LocUtil.parseLocations(lines));
            record.setStartpoint(LocUtil.parseLocation(allRecordCursor
                    .getString(allRecordCursor
                            .getColumnIndex(DBHelper.KEY_STRAT))));
            record.setEndpoint(LocUtil.parseLocation(allRecordCursor
                    .getString(allRecordCursor
                            .getColumnIndex(DBHelper.KEY_END))));
            allRecord.add(record);
        }
        Collections.reverse(allRecord);
        return allRecord;
    }

    /**
     * 按照id查询
     *
     */
    public PathRecord queryRecordById(int mRecordItemId) {
        String where = KEY_ROWID + "=?";
        String[] selectionArgs = new String[] { String.valueOf(mRecordItemId) };
        Cursor cursor = db.query(RECORD_TABLE, getColumns(), where,
                selectionArgs, null, null, null);
        PathRecord record = new PathRecord();
        if (cursor.moveToNext()) {
            record.setId(cursor.getInt(cursor
                    .getColumnIndex(DBHelper.KEY_ROWID)));
            record.setDistance(cursor.getString(cursor
                    .getColumnIndex(DBHelper.KEY_DISTANCE)));
            record.setDuration(cursor.getString(cursor
                    .getColumnIndex(DBHelper.KEY_DURATION)));
            record.setDate(cursor.getString(cursor
                    .getColumnIndex(DBHelper.KEY_DATE)));
            String lines = cursor.getString(cursor
                    .getColumnIndex(DBHelper.KEY_LINE));
            record.setPathline(LocUtil.parseLocations(lines));
            record.setStartpoint(LocUtil.parseLocation(cursor.getString(cursor
                    .getColumnIndex(DBHelper.KEY_STRAT))));
            record.setEndpoint(LocUtil.parseLocation(cursor.getString(cursor
                    .getColumnIndex(DBHelper.KEY_END))));
        }
        return record;
    }

    private String[] getColumns() {
        return new String[] { KEY_ROWID, KEY_DISTANCE, KEY_DURATION, KEY_SPEED,
                KEY_LINE, KEY_STRAT, KEY_END, KEY_DATE };
    }

}
