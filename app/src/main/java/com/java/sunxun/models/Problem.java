package com.java.sunxun.models;

import android.util.Pair;
import androidx.annotation.Nullable;
import com.alibaba.fastjson.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Nullable
    public static Problem fromJson(JSONObject obj) {
        Pattern pattern = Pattern.compile("[A-Za-z]\\.");
        int id = obj.getIntValue("id");
        String qBody = obj.getString("qBody");
        String qAnswer = obj.getString("qAnswer");
        Matcher matcher = pattern.matcher(qBody);
        if (!matcher.find()) return null;
        int lastStart = matcher.start();
        String question = qBody.substring(0, lastStart);
        String answer = null;
        ArrayList<String> distractionList = new ArrayList<>();
        while (matcher.find()) {
            String currentTag = qBody.substring(lastStart, lastStart + 1);
            String option = qBody.substring(lastStart + 2, matcher.start());
            if (currentTag.equals(qAnswer)) {
                answer = option;
            } else {
                distractionList.add(option);
            }
            lastStart = matcher.start();
        }
        String currentTag = qBody.substring(lastStart, lastStart + 1);
        String option = qBody.substring(lastStart + 2);
        if (currentTag.equals(qAnswer)) {
            answer = option;
        } else {
            distractionList.add(option);
        }
        if (answer == null) return null;
        return new Problem(id, question, answer, distractionList.toArray(new String[0]));
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
