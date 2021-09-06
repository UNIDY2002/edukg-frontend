package com.java.sunxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.java.sunxun.exceptions.UninitializedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestHistoryDB {

    private static volatile TestHistoryDB instance = null;

    private final SQLiteDatabase db;

    private static final String TABLE_NAME = "history";

    private TestHistoryDB(Context context) {
        db = new SQLiteOpenHelper(context, "testHistory.db", null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("create table history(record String)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            }
        }.getWritableDatabase();
    }

    public static TestHistoryDB getInstance() throws UninitializedException {
        if (instance == null) throw new UninitializedException();
        return instance;
    }

    public static void init(Context context) {
        instance = new TestHistoryDB(context);
    }

    public void addHistory(String record) {
        db.delete(TABLE_NAME, "record = ?", new String[]{record});
        ContentValues content = new ContentValues();
        content.put("record", record);
        db.insert(TABLE_NAME, null, content);
    }

    public List<String> getHistory() {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        ArrayList<String> history = new ArrayList<>();
        while (cursor.moveToNext()) history.add(cursor.getString(0));
        cursor.close();
        Collections.reverse(history);
        return history;
    }

    public void close() {
        db.close();
        instance = null;
    }
}
