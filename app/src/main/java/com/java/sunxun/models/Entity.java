package com.java.sunxun.models;

public class Entity {
    final private Subject subject;
    final private String label;
    final private String category;
    final private String Uri;

    public Entity(Subject subject, String label, String category, String Uri) {
        this.subject = subject;
        this.label = label;
        this.category = category;
        this.Uri = Uri;
    }

    public Subject getSubject() { return this.subject; }

    public String getLabel() { return this.label; }

    public String getCategory() { return this.category; }

    public String getUri() { return this.Uri; }
}
