package com.java.sunxun.network;

import android.util.Pair;
import androidx.annotation.NonNull;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.models.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlatformNetwork {
    @NonNull
    static String id = "";

    public static void searchInstance(@NonNull Subject subject, @NonNull String keyword, NetworkHandler<ArrayList<SearchResult>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("searchKey", keyword);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/instanceList", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                ArrayList<SearchResult> results = new ArrayList<>();
                Set<String> uriOccurred = new HashSet<>();
                uriOccurred.add(null);
                JSONArray data = o.getJSONArray("data");
                for (int i = 0; i < data.size(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    String uri = item.getString("uri");
                    if (!uriOccurred.contains(uri)) {
                        results.add(new SearchResult(item.getString("label"), item.getString("category"), uri));
                    }
                    uriOccurred.add(uri);
                }
                handler.onSuccess(results);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void queryByName(@NonNull Subject subject, @NonNull String name, NetworkHandler<InfoByName> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("name", name);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/infoByInstanceName", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                o = o.getJSONObject("data");

                // Get property
                ArrayList<Pair<String, String>> property = new ArrayList<>();
                JSONArray propertyJSONArray = o.getJSONArray("property");
                for (int i = 0; i < propertyJSONArray.size(); ++i) {
                    JSONObject propertyPair = propertyJSONArray.getJSONObject(i);
                    property.add(new Pair<>(
                            propertyPair.getString("predicateLabel"), propertyPair.getString("object")));
                }

                // Get relation
                ArrayList<Pair<String, InfoByName>> subjectRelation = new ArrayList<>();
                ArrayList<Pair<String, InfoByName>> objectRelation = new ArrayList<>();
                JSONArray relationJSONArray = o.getJSONArray("content");
                for (int i = 0; i < relationJSONArray.size(); ++i) {
                    JSONObject relationPair = relationJSONArray.getJSONObject(i);
                    if (relationPair.getString("object") == null) {
                        subjectRelation.add(new Pair<>(
                                relationPair.getString("predicate_label"), new InfoByName(relationPair.getString("subject_label"))));
                    } else {
                        objectRelation.add(new Pair<>(
                                relationPair.getString("predicate_label"), new InfoByName(relationPair.getString("object_label"))));
                    }
                }

                handler.onSuccess(new InfoByName(
                        o.getString("label"), property, subjectRelation, objectRelation
                ));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void queryByUri(@NonNull Subject subject, @NonNull String uri, NetworkHandler<InfoByUri> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("uri", uri);
        params.put("id", id);
        BaseNetwork.fetch("http://open.edukg.cn/opedukg/api/typeOpen/open/getKnowledgeCard", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                JSONObject data = o.getJSONObject("data");
                Map<String, List<String>> features = new HashMap<>();
                JSONArray featureList = data.getJSONArray("entity_features");
                String imageUrlIfExists = null;
                for (int i = 0; i < featureList.size(); i++) {
                    JSONObject feature = featureList.getJSONObject(i);
                    String key = feature.getString("feature_key");
                    List<String> value = features.get(key);
                    String featureValue = feature.getString("feature_value").trim();
                    if (value == null) {
                        value = new ArrayList<>();
                        value.add(featureValue);
                        features.put(key, value);
                    } else {
                        value.add(featureValue);
                    }
                    if (featureValue.startsWith("http://") || featureValue.startsWith("https://")){
                        imageUrlIfExists = featureValue;
                    }
                }
                handler.onSuccess(new InfoByUri(data.getString("entity_name"), data.getString("entity_type"), features, imageUrlIfExists));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void qa(@NonNull Subject subject, @NonNull String question, NetworkHandler<Answer> handler) {
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

    public static void linking(@NonNull Subject subject, @NonNull String context, NetworkHandler<Linking> handler) {
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

    public static void relatedProblems(@NonNull String name, NetworkHandler<ArrayList<Problem>> handler) {
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
