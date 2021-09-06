package com.java.sunxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.java.sunxun.exceptions.UninitializedException;

public class DetailCacheDB {

    private static volatile DetailCacheDB instance = null;

    private final SQLiteDatabase db;

    private static final String TABLE_NAME = "cache";

    private DetailCacheDB(Context context) {
        db = new SQLiteOpenHelper(context, "detailCache.db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("create table cache(uri String)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        }.getWritableDatabase();
    }

    public static DetailCacheDB getInstance() throws UninitializedException {
        if (instance == null) throw new UninitializedException();
        return instance;
    }

    public static void init(Context context) {
        instance = new DetailCacheDB(context);
    }

    public void addCache(String uri) {
        if (!hasCache(uri)) {
            ContentValues content = new ContentValues();
            content.put("uri", uri);
            db.insert(TABLE_NAME, null, content);
        }
    }

    public boolean hasCache(String uri) {
        Cursor cursor = db.query(TABLE_NAME, new String[0], "uri = ?", new String[]{uri}, null, null, null, null);
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    public void close() {
        db.close();
        instance = null;
    }
}
