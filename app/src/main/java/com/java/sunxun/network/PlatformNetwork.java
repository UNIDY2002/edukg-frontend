package com.java.sunxun.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.exceptions.PlatformApiException;
import com.java.sunxun.models.Answer;
import com.java.sunxun.models.Linking;
import com.java.sunxun.models.Subject;

import java.util.HashMap;
import java.util.Map;

public class PlatformNetwork {
    static String id;

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
                            dataItem.getDoubleValue("score"),
                            dataItem.getString("value")
                    ));
                } else {
                    handler.onError(new PlatformApiException(o.getString("code"), o.getString("msg")));
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void linking(Subject subject, String context, NetworkHandler<Linking> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("context", context);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/linkInstance", params, BaseNetwork.Method.POST, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                JSONObject o = JSON.parseObject(result);
                if ("0".equals(o.getString("code"))) {
                    Linking linking = new Linking(context);
                    JSONArray results = o.getJSONObject("data").getJSONArray("results");
                    for (int i = 0; i < results.size(); i++) {
                        JSONObject obj = results.getJSONObject(i);
                        linking.link(
                                obj.getString("entity_type"),
                                obj.getString("entity_url"),
                                obj.getIntValue("start_index"),
                                obj.getIntValue("end_index"),
                                obj.getString("entity")
                        );
                    }
                    handler.onSuccess(linking);
                } else {
                    handler.onError(new PlatformApiException(o.getString("code"), o.getString("msg")));
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }
}
