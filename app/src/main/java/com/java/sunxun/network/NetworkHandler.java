package com.java.sunxun.network;

import android.app.Activity;
import androidx.fragment.app.Fragment;

public abstract class NetworkHandler<T> {
    Activity activity;

    public NetworkHandler(Activity activity) {
        this.activity = activity;
    }

    public NetworkHandler(Fragment fragment) {
        this.activity = (Activity) fragment.getContext();
    }

    public abstract void onSuccess(T result);

    public abstract void onError(Exception e);
}
