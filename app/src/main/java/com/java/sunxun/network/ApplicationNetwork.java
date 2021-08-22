package com.java.sunxun.network;

import java.util.HashMap;

public class ApplicationNetwork {
    static public void getEntityList(NetworkHandler<String> handler) {
        BaseNetwork.fetch("https://www.baidu.com", new HashMap<String, String>(), BaseNetwork.Method.GET, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                System.out.println(result);
                handler.onSuccess("Hello, baidu!");
            }

            @Override
            public void onError(Exception e) { handler.onError(e); }
        });
    }
}
