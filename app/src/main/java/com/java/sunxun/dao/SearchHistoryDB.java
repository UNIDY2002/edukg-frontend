package com.java.sunxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.java.sunxun.exceptions.UninitializedException;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryDB {

    private static volatile SearchHistoryDB instance = null;

    private final SQLiteDatabase db;

    private static final String TABLE_NAME = "history";

    private SearchHistoryDB(Context context) {
        db = new SQLiteOpenHelper(context, "searchHistory.db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("create table history(record String)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        }.getWritableDatabase();
    }

    public static SearchHistoryDB getInstance() throws UninitializedException {
        if (instance == null) throw new UninitializedException();
        return instance;
    }

    public static void init(Context context) {
        instance = new SearchHistoryDB(context);
    }

    public void addHistory(String record) {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        boolean exists = cursor.moveToNext();
        cursor.close();
        if (exists) removeHistory(record);
        ContentValues content = new ContentValues();
        content.put("record", record);
        db.insert(TABLE_NAME, null, content);
    }

    public List<String> getHistory() {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        ArrayList<String> history = new ArrayList<>();
        while (cursor.moveToNext()) {
            history.add(cursor.getString(0));
        }
        cursor.close();
        return history;
    }

    public void removeHistory(String record) {
        db.delete(TABLE_NAME, "record = ?", new String[]{record});
    }

    public void close() {
        db.close();
        instance = null;
    }
}
