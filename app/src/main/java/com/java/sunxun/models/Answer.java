package com.java.sunxun.models;

public class Answer {
    private final String entity;  // 答案相关实体
    private final String entityUri;  // 答案相关实体 uri
    private final String predicate;  // 答案涉及的实体属性
    private final double score;  // 相关度得分
    private final String value;  // 答案值

    public Answer(String entity, String entityUri, String predicate, double score, String value) {
        this.entity = entity;
        this.entityUri = entityUri;
        this.predicate = predicate;
        this.score = score;
        this.value = value;
    }

    public String getEntity() {
        return entity;
    }

    public String getEntityUri() {
        return entityUri;
    }

    public String getPredicate() {
        return predicate;
    }

    public double getScore() {
        return score;
    }

    public String getValue() {
        return value;
    }
}
