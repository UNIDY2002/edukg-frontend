package com.java.sunxun.models;

import java.util.Date;

public class History {
    final private Subject subject;
    final private String uri;
    final private String name;
    final private String category;
    final private Date time;

    public History(Subject subject, String uri, String name, String category, Date time) {
        this.subject = subject;
        this.uri = uri;
        this.name = name;
        this.category = category;
        this.time = time;
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

    public Date getTime() {
        return time;
    }
}
