package com.java.sunxun.models;

public class Problem {
    private final int id;
    private final String question;
    private final String answer;
    private final String[] distractions;

    public Problem(int id, String question, String answer, String[] distractions) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.distractions = distractions;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String[] getDistractions() {
        return distractions;
    }
}
