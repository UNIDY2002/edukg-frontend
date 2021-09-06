package com.java.sunxun.network;

import androidx.annotation.NonNull;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.models.History;
import com.java.sunxun.models.Star;
import com.java.sunxun.models.Subject;

import java.text.SimpleDateFormat;
import java.util.*;

public class ApplicationNetwork {
    @NonNull
    static String id = "";

    private static final String BACKEND_URL = "https://www.unidy.cn/edukg/backend";
    private static final String DATABASE_URL = "https://www.unidy.cn/edukg/database";

    static public void getEntityList(Subject subject, int page, int pageSize, int seed, NetworkHandler<String> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("course", subject.toString());
        params.put("page", "" + page);
        params.put("pageSize", "" + pageSize);
        params.put("seed", "" + seed);
        BaseNetwork.fetch(DATABASE_URL + "/api/getRandomEntity", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess(o.getString("data"));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void login(String username, String password, NetworkHandler<String> handler) {
        JSONObject params = new JSONObject();
        params.put("username", username);
        params.put("password", password);
        BaseNetwork.fetch(BACKEND_URL + "/api/login", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess(id = o.getString("id"));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void getId(NetworkHandler<String> handler) {
        BaseNetwork.fetch(BACKEND_URL + "/api/getId", new HashMap<>(), BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess(PlatformNetwork.id = o.getString("id"));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void star(Subject subject, String uri, String name, String category, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("course", subject.toString());
        params.put("uri", uri);
        params.put("label", name);
        params.put("category", category);
        BaseNetwork.fetch(DATABASE_URL + "/api/star", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess("0".equals(o.getString("data")));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void unstar(String uri, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("uri", uri);
        BaseNetwork.fetch(DATABASE_URL + "/api/unstar", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess("0".equals(o.getString("data")));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void isStar(String uri, NetworkHandler<Boolean> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("uri", uri);
        BaseNetwork.fetch(DATABASE_URL + "/api/isStar", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess("1".equals(o.getString("data")));
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void getStarList(NetworkHandler<ArrayList<Star>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        BaseNetwork.fetch(DATABASE_URL + "/api/getStarList", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                JSONArray data = o.getJSONArray("data");
                ArrayList<Star> result = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    result.add(new Star(
                            Subject.fromString(item.getString("course")),
                            item.getString("uri"),
                            item.getString("name"),
                            item.getString("category")
                    ));
                }
                handler.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void addHistory(Subject subject, String uri, String name, String category, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("course", subject.toString());
        params.put("uri", uri);
        params.put("label", name);
        params.put("category", category);
        BaseNetwork.fetch(DATABASE_URL + "/api/addHistory", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess(true);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void getHistoryList(NetworkHandler<List<History>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        BaseNetwork.fetch(DATABASE_URL + "/api/getHistoryList", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                JSONArray data = o.getJSONArray("data");
                List<History> result = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    try {
                        JSONObject item = data.getJSONObject(i);
                        result.add(new History(
                                Subject.fromString(item.getString("course")),
                                item.getString("uri"),
                                item.getString("label"),
                                item.getString("category"),
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.CHINA).parse(item.getString("time"))
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                result.sort((o1, o2) -> -o1.getTime().compareTo(o2.getTime()));
                handler.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void uploadTestResult(String name, boolean isCorrect, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("label", name);
        params.put("correct", isCorrect ? 1 : 0);
        BaseNetwork.fetch(DATABASE_URL + "/api/addProblem", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                handler.onSuccess(true);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }
}
