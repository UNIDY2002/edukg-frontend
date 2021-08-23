package com.java.sunxun.network;

import android.app.Activity;
import android.view.View;
import androidx.fragment.app.Fragment;

public abstract class NetworkHandler<T> {
    public Activity activity;

    public NetworkHandler(Activity activity) {
        this.activity = activity;
    }

    public NetworkHandler(Fragment fragment) {
        this.activity = (Activity) fragment.getContext();
    }

    public NetworkHandler(View view) {
        this.activity = (Activity) view.getContext();
    }

    public abstract void onSuccess(T result);

    public abstract void onError(Exception e);
}
