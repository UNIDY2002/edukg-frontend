package com.java.sunxun.utils;

import android.content.Intent;
import androidx.fragment.app.Fragment;

public class Share {
    public static void share(Fragment fragment, String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        fragment.startActivity(intent);
    }
}
