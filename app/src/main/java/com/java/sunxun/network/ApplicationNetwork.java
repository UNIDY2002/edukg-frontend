package com.java.sunxun.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.exceptions.PlatformLoginFailureException;
import com.java.sunxun.models.Subject;

import java.util.HashMap;
import java.util.Map;

public class ApplicationNetwork {
    static String id;

    private static final String BASE_URL = "http://unidy.cn:8080";

    static public void getEntityList(NetworkHandler<String> handler) {
        BaseNetwork.fetch("https://www.baidu.com", new HashMap<String, String>(), BaseNetwork.Method.GET, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                JSONObject o = JSON.parseObject(result);
                if ("0".equals(o.getString("code"))) {
                    handler.onSuccess(o.getString("data"));
                } else {
                    // TODO: Write a better exception!
                    handler.onError(new Exception());
                }
            }

            @Override
            public void onError(Exception e) { handler.onError(e); }
        });
    }

    public static void login(String username, String password, NetworkHandler<String> handler) {
        JSONObject params = new JSONObject();
        params.put("username", username);
        params.put("password", password);
        BaseNetwork.fetch(BASE_URL + "/api/login", params, BaseNetwork.Method.POST, new NetworkHandler<String>(handler.activity) {
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

    public static void getId(NetworkHandler<String> handler) {
        BaseNetwork.fetch(BASE_URL + "/api/getId", new HashMap<>(), BaseNetwork.Method.GET, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                JSONObject o = JSON.parseObject(result);
                if ("0".equals(o.getString("code"))) {
                    handler.onSuccess(PlatformNetwork.id = o.getString("id"));
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
