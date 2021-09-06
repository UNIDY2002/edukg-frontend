package com.java.sunxun.network;

import androidx.annotation.NonNull;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.models.Subject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        BaseNetwork.fetch("http://10.0.2.2:8000/api/getRandomEntity", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    /**
     * 用户收藏/取消收藏实体
     * 未收藏的实体，操作后被收藏。已收藏的实体，操作后被取消收藏。
     *
     * @param uri     实体 uri
     * @param handler false 表示取消收藏成功，true 表示收藏成功
     */
    public static void star(String uri, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("uri", uri);
        BaseNetwork.fetch(DATABASE_URL + "/api/star", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    /**
     * 询问用户是否已收藏某给定实体
     *
     * @param uri     实体 uri
     * @param handler false 表示用户未收藏，true 表示用户已收藏
     */
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

    public static void getStarList(NetworkHandler<ArrayList<String>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        BaseNetwork.fetch(DATABASE_URL + "/api/getStarList", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                JSONArray data = o.getJSONArray("data");
                ArrayList<String> result = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    result.add(data.getString(i));
                }
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
