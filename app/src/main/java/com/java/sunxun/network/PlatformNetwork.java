package com.java.sunxun.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.exceptions.PlatformLoginFailureException;
import com.java.sunxun.models.Answer;
import com.java.sunxun.models.Subject;

import java.util.HashMap;
import java.util.Map;

public class PlatformNetwork {
    static String id;

    public static void login(String phone, String password, NetworkHandler<String> handler) {
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

    public static void qa(Subject subject, String question, NetworkHandler<Answer> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("inputQuestion", question);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/inputQuestion", params, BaseNetwork.Method.POST, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                JSONObject o = JSON.parseObject(result);
                if ("0".equals(o.getString("code"))) {
                    JSONObject dataItem = o.getJSONArray("data").getJSONObject(0);
                    handler.onSuccess(new Answer(
                            dataItem.getString("subject"),
                            dataItem.getString("subjectUri"),
                            dataItem.getString("predicate"),
                            dataItem.getDouble("score"),
                            dataItem.getString("value")
                    ));
                } else {
                    handler.onError(new PlatformLoginFailureException());
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
