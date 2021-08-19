package com.java.sunxun.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.exceptions.PlatformLoginFailureException;

import java.util.HashMap;
import java.util.Map;

public class PlatformNetwork {
    static String id;

    static public void login(String phone, String password, NetworkHandler<String> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        params.put("password", password);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeAuth/user/login", params, BaseNetwork.Method.POST, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                JSONObject o = JSON.parseObject(result);
                if ("0".equals(o.getString("code"))) {
                    handler.onSuccess(id = o.getString("id"));
                } else {
                    handler.onError(new PlatformLoginFailureException());
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }
}
