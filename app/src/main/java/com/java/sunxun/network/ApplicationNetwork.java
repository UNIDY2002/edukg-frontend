package com.java.sunxun.network;

import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java.sunxun.R;
import com.java.sunxun.exceptions.DuplicatedStarException;
import com.java.sunxun.exceptions.DuplicatedUnstarException;
import com.java.sunxun.exceptions.RetryTimeExceededException;
import com.java.sunxun.models.*;

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

    public static void register(String username, String password, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("username", username);
        params.put("password", password);
        BaseNetwork.fetch(BACKEND_URL + "/api/register", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    public static void modifyPassword(String username, String prev, String next, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("username", username);
        params.put("oldPassword", prev);
        params.put("newPassword", next);
        BaseNetwork.fetch(BACKEND_URL + "/api/modifyPassword", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    public static void modifyProfile(String profile, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("profile", profile);
        BaseNetwork.fetch(BACKEND_URL + "/api/modifyProfile", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    public static void getProfile(NetworkHandler<String> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        BaseNetwork.fetch(BACKEND_URL + "/api/getProfile", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                String data = o.getString("data");
                if ("ashitemaru".equals(data)) {
                    handler.onSuccess(null);
                } else {
                    handler.onSuccess(data);
                }
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

    public static void star(Subject subject, String uri, String name, String category, @Nullable String folder, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("course", subject.toString());
        params.put("uri", uri);
        params.put("label", name);
        params.put("category", category);
        if (folder != null) params.put("folder", folder);
        BaseNetwork.fetch(DATABASE_URL + "/api/star", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                String data = o.getString("data");
                switch (data) {
                    case "0": {
                        handler.onSuccess(true);
                        break;
                    }
                    case "1": {
                        handler.onError(new DuplicatedStarException());
                        break;
                    }
                    default: {
                        handler.onError(new RuntimeException());
                        break;
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void unstar(String uri, @Nullable String folder, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("uri", uri);
        if (folder != null) params.put("folder", folder);
        BaseNetwork.fetch(DATABASE_URL + "/api/unstar", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                String data = o.getString("data");
                switch (data) {
                    case "0": {
                        handler.onSuccess(true);
                        break;
                    }
                    case "1": {
                        handler.onError(new DuplicatedUnstarException());
                        break;
                    }
                    default: {
                        handler.onError(new RuntimeException());
                        break;
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void isStar(String uri, NetworkHandler<List<Pair<String, Boolean>>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("uri", uri);
        BaseNetwork.fetch(DATABASE_URL + "/api/isStar", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                if ("0".equals(o.getString("data"))) {
                    handler.onSuccess(new ArrayList<>());
                } else {
                    List<Pair<String, Boolean>> result = new ArrayList<>();
                    JSONArray folders = o.getJSONArray("folders");
                    for (int i = 0; i < folders.size(); i++) {
                        JSONObject item = folders.getJSONObject(i);
                        String name = item.getString("name");
                        result.add(new Pair<>(
                                "default".equals(name) ? handler.activity.getString(R.string.default_folder) : name,
                                item.getIntValue("isStar") == 1
                        ));
                    }
                    handler.onSuccess(result);
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void getStarList(@Nullable String folder, NetworkHandler<ArrayList<Star>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        if (folder != null) params.put("folder", folder);
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
                            item.getString("label"),
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

    public static void getFolderList(NetworkHandler<List<String>> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        BaseNetwork.fetch(DATABASE_URL + "/api/getFolderList", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                List<String> result = new ArrayList<>();
                result.add(handler.activity.getString(R.string.default_folder));
                JSONArray data = o.getJSONArray("data");
                for (int i = 0; i < data.size(); i++) result.add(data.getString(i));
                handler.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    private static void modifyFolder(@NonNull String url, @NonNull String folder, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("folder", folder);
        BaseNetwork.fetch(url, params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    public static void addFolder(@NonNull String folder, NetworkHandler<Boolean> handler) {
        modifyFolder(DATABASE_URL + "/api/addFolder", folder, handler);
    }

    public static void removeFolder(@NonNull String folder, NetworkHandler<Boolean> handler) {
        modifyFolder(DATABASE_URL + "/api/removeFolder", folder, handler);
    }

    public static void addHistory(Subject subject, String uri, String name, String category, @Nullable Boolean hasProblem, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("course", subject.toString());
        params.put("uri", uri);
        params.put("label", name);
        params.put("category", category);
        if (hasProblem != null) params.put("hasProblem", hasProblem ? "1" : "0");
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

    public static void uploadTestResult(String uri, String name, boolean isCorrect, NetworkHandler<Boolean> handler) {
        JSONObject params = new JSONObject();
        params.put("id", id);
        params.put("uri", uri);
        params.put("label", name);
        params.put("correct", isCorrect ? 1 : 0);
        BaseNetwork.fetch(DATABASE_URL + "/api/addProblemRecord", params, BaseNetwork.Method.POST, new JsonResponseNetworkHandler(handler.activity, "0") {
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

    public static void getRecommendedProblem(int retryTimes, NetworkHandler<RecommendedProblem> handler) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        params.put("onlyNew", String.valueOf(retryTimes < 5 ? 1 : 0));
        BaseNetwork.fetch(DATABASE_URL + "/api/getRecommendedProblem", params, BaseNetwork.Method.GET, new JsonResponseNetworkHandler(handler.activity, "0") {
            @Override
            public void onJsonSuccess(JSONObject o) {
                if (o.getIntValue("iszero") == 0) {
                    JSONObject data = o.getJSONObject("data");
                    Problem problem = Problem.fromJson(data.getJSONObject("problem"));
                    if (problem != null) {
                        handler.onSuccess(new RecommendedProblem(data.getString("label"), data.getString("uri"), problem));
                    } else if (retryTimes > 0) {
                        getRecommendedProblem(retryTimes - 1, handler);
                    } else {
                        handler.onError(new RetryTimeExceededException());
                    }
                } else if (retryTimes > 0) {
                    getRecommendedProblem(retryTimes - 1, handler);
                } else {
                    handler.onError(new RetryTimeExceededException());
                }
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void logout() {
        id = "";
    }
}
