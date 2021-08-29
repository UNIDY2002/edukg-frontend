package com.java.sunxun.models;

import android.util.Pair;

import java.util.*;

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

    public Pair<String[], Integer> genRandomOptions(int maxOptionCount) {
        ArrayList<String> options = new ArrayList<>();
        options.add(answer);
        options.addAll(Arrays.asList(distractions));
        while (options.size() > maxOptionCount) options.remove(options.size() - 1);
        Collections.shuffle(options);
        return new Pair<>(options.toArray(new String[0]), options.indexOf(answer));
    }
}
