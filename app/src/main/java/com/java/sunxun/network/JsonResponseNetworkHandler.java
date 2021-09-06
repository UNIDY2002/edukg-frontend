package com.java.sunxun.network;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.exceptions.InvalidCodeException;

import java.nio.file.LinkOption;

public abstract class JsonResponseNetworkHandler extends NetworkHandler<String> {
    @NonNull
    private final String validCode;

    public JsonResponseNetworkHandler(Activity activity, @NonNull String validCode) {
        super(activity);
        this.validCode = validCode;
    }

    public JsonResponseNetworkHandler(Fragment fragment, @NonNull String validCode) {
        super(fragment);
        this.validCode = validCode;
    }

    public JsonResponseNetworkHandler(View view, @NonNull String validCode) {
        super(view);
        this.validCode = validCode;
    }

    @Override
    public final void onSuccess(String result) {
        JSONObject o = JSON.parseObject(result);
        String code = o.getString("code");
        if (validCode.equals(code)) {
            onJsonSuccess(o);
        } else {
            onError(new InvalidCodeException(code));
        }
    }

    public abstract void onJsonSuccess(JSONObject o);
}
