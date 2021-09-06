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
            "出处", "类型", "包含"
    ));

    final private String label;
    final private ArrayList<Pair<String, InfoByName>> subjectRelation;
    final private ArrayList<Pair<String, InfoByName>> objectRelation;
    private ArrayList<Pair<String, String>> property;

    private void filterAndSortProperty() {
        ArrayList<Pair<String, String>> newProperty = new ArrayList<>();
        ArrayList<String> propertyValues = new ArrayList<>();

        // Filter keys in blacklist & remove duplicated properties
        for (Pair<String, String> kvPair: property) {
            if (blackList.contains(kvPair.first) || propertyValues.contains(kvPair.second)) continue;
            propertyValues.add(kvPair.second);
            newProperty.add(kvPair);
        }
        property = newProperty;
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
