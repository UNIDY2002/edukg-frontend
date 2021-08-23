package com.java.sunxun.network;

import com.java.sunxun.exceptions.NetworkFailureException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class BaseNetwork {
    enum Method {
        GET, POST
    }

    static String serialize(Map<String, String> params) {
        return params.entrySet().stream()
                .map((e) -> {
                    try {
                        return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException();
                    }
                })
                .collect(Collectors.joining("&"));
    }

    static void fetch(String url, Map<String, String> params, BaseNetwork.Method method, NetworkHandler<String> handler) {
        new Thread(() -> {
            try {
                String fullUrl = method == Method.GET && !params.isEmpty() ? url + "?" + serialize(params) : url;
                HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl).openConnection();
                if (method == Method.POST) connection.setRequestMethod("POST");
                connection.connect();
                if (method == Method.POST)
                    connection.getOutputStream().write(serialize(params).getBytes(StandardCharsets.UTF_8));
                if (connection.getResponseCode() == 200) {
                    Stream<String> lines = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines();
                    if (lines == null) {
                        throw new NetworkFailureException();
                    }
                    handler.activity.runOnUiThread(() -> {
                        try {
                            handler.onSuccess(lines.collect(Collectors.joining("\n")));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                } else {
                    throw new NetworkFailureException();
                }
            } catch (Exception e) {
                handler.activity.runOnUiThread(() -> {
                    try {
                        handler.onError(e);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }).start();
    }
}
