package com.java.sunxun.models;

public class SearchResult {
    private final String label;

    private final String category;

    private final String uri;

    public SearchResult(String label, String category, String uri) {
        this.label = label;
        this.category = category;
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public String getUri() {
        return uri;
    }
}
