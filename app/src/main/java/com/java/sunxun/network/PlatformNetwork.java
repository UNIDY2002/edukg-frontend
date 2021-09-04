package com.java.sunxun.network;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.models.Answer;
import com.java.sunxun.models.Linking;
import com.java.sunxun.models.Problem;
import com.java.sunxun.models.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlatformNetwork {
    static String id;

    public static void qa(Subject subject, String question, NetworkHandler<Answer> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("inputQuestion", question);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/inputQuestion", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                JSONObject dataItem = o.getJSONArray("data").getJSONObject(0);
                final String NO_ANSWER = "此问题没有找到答案！";
                if (NO_ANSWER.equals(dataItem.getString("message"))) {
                    handler.onSuccess(new Answer("", "", "", 100, NO_ANSWER));
                    return;
                }
                handler.onSuccess(new Answer(
                        dataItem.getString("subject"),
                        dataItem.getString("subjectUri"),
                        dataItem.getString("predicate"),
                        dataItem.getDoubleValue("score"),
                        dataItem.getString("value")
                ));
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
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/linkInstance", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
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
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void relatedProblems(String name, NetworkHandler<ArrayList<Problem>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("uriName", name);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/questionListByUriName", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                JSONArray problems = o.getJSONArray("data");
                Pattern pattern = Pattern.compile("[A-Za-z]\\.");
                ArrayList<Problem> results = new ArrayList<>();
                for (int i = 0; i < problems.size(); i++) {
                    try {
                        JSONObject obj = problems.getJSONObject(i);
                        int id = obj.getIntValue("id");
                        String qBody = obj.getString("qBody");
                        String qAnswer = obj.getString("qAnswer");
                        Matcher matcher = pattern.matcher(qBody);
                        if (!matcher.find()) continue;
                        int lastStart = matcher.start();
                        String question = qBody.substring(0, lastStart);
                        String answer = null;
                        ArrayList<String> distractionList = new ArrayList<>();
                        while (matcher.find()) {
                            String currentTag = qBody.substring(lastStart, lastStart + 1);
                            String option = qBody.substring(lastStart + 2, matcher.start());
                            if (currentTag.equals(qAnswer)) {
                                answer = option;
                            } else {
                                distractionList.add(option);
                            }
                            lastStart = matcher.start();
                        }
                        String currentTag = qBody.substring(lastStart, lastStart + 1);
                        String option = qBody.substring(lastStart + 2);
                        if (currentTag.equals(qAnswer)) {
                            answer = option;
                        } else {
                            distractionList.add(option);
                        }
                        if (answer == null) continue;
                        results.add(new Problem(id, question, answer, distractionList.toArray(new String[0])));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handler.onSuccess(results);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }
}
