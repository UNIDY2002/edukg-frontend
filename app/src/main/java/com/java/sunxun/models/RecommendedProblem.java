package com.java.sunxun.models;

public class RecommendedProblem {
    public final String label;
    public final String uri;
    public final Problem problem;

    public RecommendedProblem(String label, String uri, Problem problem) {
        this.label = label;
        this.uri = uri;
        this.problem = problem;
    }
}
