package com.java.sunxun.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

public class InfoByUri {
    private final String name;

    private final String type;

    private final Map<String, List<String>> features;

    public InfoByUri(String name, String type, Map<String, List<String>> features) {
        this.name = name;
        this.type = type;
        this.features = features;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public List<String> getFeature(@NonNull String key) {
        return features.get(key);
    }
}
