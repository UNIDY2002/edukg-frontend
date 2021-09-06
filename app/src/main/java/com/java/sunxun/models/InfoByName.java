package com.java.sunxun.models;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InfoByName {
    final public static List<String> blackList = new ArrayList<>(Arrays.asList(
            "出处", "类型", "包含", "例", "下位于"
    ));

    final private String label;
    final private ArrayList<Pair<String, InfoByName>> subjectRelation;
    final private ArrayList<Pair<String, InfoByName>> objectRelation;
    final private ArrayList<Pair<String, String>> property;

    private void filterAndSortProperty() {
        ArrayList<String> propertyValues = new ArrayList<>();
        HashMap<String, String> tmpProperty = new HashMap<>();

        // TODO: http://kb.cs.tsinghua.edu.cn... is picture
        // TODO: Handle "图片" keys
        // TODO: Handle long paragraphs
        // TODO: Empty component

        /**
         * Filter keys in blacklist & remove duplicated properties
         * Handle duplicated keys & Handle empty value
         */
        for (Pair<String, String> kvPair: property) {
            Log.d("Entity props", "Entity " + label + " has key " + kvPair.first + ": " + kvPair.second);
            if (blackList.contains(kvPair.first) || propertyValues.contains(kvPair.second) || kvPair.second.trim().isEmpty()) continue;
            propertyValues.add(kvPair.second);
            if (tmpProperty.containsKey(kvPair.first)) {
                String originVal = tmpProperty.get(kvPair.first);
                tmpProperty.remove(kvPair.first);
                tmpProperty.put(kvPair.first, originVal + "\n" + kvPair.second);
            } else tmpProperty.put(kvPair.first, kvPair.second);
        }

        property.clear();
        // Remember to end the sentence
        for (String key: tmpProperty.keySet()) property.add(new Pair<>(key, tmpProperty.get(key)));
        property.sort((o1, o2) -> o1.second.length() - o2.second.length());
    }

    public InfoByName(String label) {
        this.label = label;
        this.property = new ArrayList<>();
        this.subjectRelation = new ArrayList<>();
        this.objectRelation = new ArrayList<>();
    }

    public InfoByName(
            String label,
            ArrayList<Pair<String, String>> property,
            ArrayList<Pair<String, InfoByName>> subjectRelation,
            ArrayList<Pair<String, InfoByName>> objectRelation) {
        this.label = label;
        this.property = property;
        this.subjectRelation = subjectRelation;
        this.objectRelation = objectRelation;
        filterAndSortProperty();
    }

    @NonNull
    @Override
    public String toString() {
        return "Entity " + label + " has " + property.size() + " properties & "
                + subjectRelation.size() + " subject relations & " + objectRelation.size() + " object relations.\n";
    }

    public ArrayList<Pair<String, String>> getPropertyList() {
        return property;
    }
}
