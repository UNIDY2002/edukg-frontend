package com.java.sunxun.models;

public class Star {
    final private Subject subject;
    final private String uri;
    final private String name;
    final private String category;

    public Star(Subject subject, String uri, String name, String category) {
        this.subject = subject;
        this.uri = uri;
        this.name = name;
        this.category = category;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
