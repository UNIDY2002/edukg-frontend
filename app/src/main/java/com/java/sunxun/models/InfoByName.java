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
    final private HashMap<String, InfoByName> subjectRelation;
    final private HashMap<String, InfoByName> objectRelation;
    private HashMap<String, String> property;

    private void filterProperty() {
        HashMap<String, String> newProperty = new HashMap<>();
        ArrayList<String> propertyValues = new ArrayList<>();

        // Filter keys in blacklist & remove duplicated properties
        for (String key: property.keySet()) {
            String val = property.get(key);
            if (blackList.contains(key) || propertyValues.contains(val)) continue;
            propertyValues.add(val);
            newProperty.put(key, val);
            Log.d("Entity property", key + ": " + val);
        }
        property = newProperty;
    }

    public InfoByName(String label) {
        this.label = label;
        this.property = new HashMap<>();
        this.subjectRelation = new HashMap<>();
        this.objectRelation = new HashMap<>();
    }

    public InfoByName(String label, HashMap<String, String> property, HashMap<String, InfoByName> subjectRelation, HashMap<String, InfoByName> objectRelation) {
        this.label = label;
        this.property = property;
        this.subjectRelation = subjectRelation;
        this.objectRelation = objectRelation;
        filterProperty();
    }

    @NonNull
    @Override
    public String toString() {
        return "Entity " + label + " has " + property.size() + " properties & "
                + subjectRelation.size() + " subject relations & " + objectRelation.size() + " object relations.\n";
    }

    public ArrayList<Pair<String, String>> getSortedPropertyList() {
        ArrayList<Pair<String, String>> sortedProperty = new ArrayList<>();
        for (String key: property.keySet()) sortedProperty.add(new Pair<>(key, property.get(key)));
        sortedProperty.sort((o1, o2) -> o1.second.length() - o2.second.length());
        return sortedProperty;
    }
}
