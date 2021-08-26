package com.java.sunxun.models;

public class Answer {
    private final String entityName;  // 答案相关实体
    private final String entityUri;  // 答案相关实体 uri
    private final String predicate;  // 答案涉及的实体属性
    private final Double score;  // 相关度得分
    private final String value;  // 答案值

    public Answer(String entityName, String entityUri, String predicate, Double score, String value) {
        this.entityName = entityName;
        this.entityUri = entityUri;
        this.predicate = predicate;
        this.score = score;
        this.value = value;
    }

    public String getEntityName() { return entityName; }

    public String getEntityUri() { return entityUri; }

    public String getPredicate() { return predicate; }

    public Double getScore() { return score; }

    public String getValue() { return value; }
}
