package com.java.sunxun.utils;

import android.content.Context;
import android.content.Intent;

public class Share {
    public static void share(Context context, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(intent);
    }
}
