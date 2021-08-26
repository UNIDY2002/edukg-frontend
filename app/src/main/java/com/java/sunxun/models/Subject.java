package com.java.sunxun.models;

import android.content.Context;
import androidx.annotation.Nullable;
import com.java.sunxun.R;

public enum Subject {
    chinese, english, math, physics, chemistry, biology, history, geo, politics;

    @Nullable
    public static Subject fromName(Context context, String name) {
        if (context.getString(R.string.chinese).equals(name)) {
            return chinese;
        } else if (context.getString(R.string.english).equals(name)) {
            return english;
        } else if (context.getString(R.string.math).equals(name)) {
            return math;
        } else if (context.getString(R.string.physics).equals(name)) {
            return physics;
        } else if (context.getString(R.string.chemistry).equals(name)) {
            return chemistry;
        } else if (context.getString(R.string.biology).equals(name)) {
            return biology;
        } else if (context.getString(R.string.history).equals(name)) {
            return history;
        } else if (context.getString(R.string.geo).equals(name)) {
            return geo;
        } else if (context.getString(R.string.politics).equals(name)) {
            return politics;
        } else {
            return null;
        }
    }
}
