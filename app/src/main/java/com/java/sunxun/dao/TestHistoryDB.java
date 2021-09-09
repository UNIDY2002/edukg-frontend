package com.java.sunxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;
import com.java.sunxun.exceptions.UninitializedException;
import com.java.sunxun.models.Subject;

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
                db.execSQL("create table history(subject Integer, record String)");
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

    public void addHistory(Subject subject, String record) {
        db.delete(TABLE_NAME, "subject = ? and record = ?", new String[]{String.valueOf(subject.ordinal()), record});
        ContentValues content = new ContentValues();
        content.put("subject", subject.ordinal());
        content.put("record", record);
        db.insert(TABLE_NAME, null, content);
    }

    public List<Pair<Subject, String>> getHistory() {
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        ArrayList<Pair<Subject, String>> history = new ArrayList<>();
        while (cursor.moveToNext()) {
            history.add(new Pair<>(Subject.values()[cursor.getInt(0)], cursor.getString(1)));
        }
        cursor.close();
        Collections.reverse(history);
        return history;
    }

    public void close() {
        db.close();
        instance = null;
    }
}
