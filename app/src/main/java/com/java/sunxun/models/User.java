package com.java.sunxun.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

public class User {
    private final String username;
    private final String password;

    private final static User VISITOR = new User("visitor", "12345678");

    private final static ArrayList<Consumer<String>> onUsernameChangedListeners = new ArrayList<>();

    @NonNull
    public static User currentUser = User.VISITOR;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    private static void performLogin(String username, String password) {

    }

    public static User login(String username, String password) {
        performLogin(username, password);
        onUsernameChangedListeners.forEach(listener -> listener.accept(username));
        return new User(username, password);
    }

    public void refreshCredentials() {
        performLogin(username, password);
    }

    public void logout() {
        currentUser = VISITOR;
    }

    public static void addOnUsernameChangedListener(Consumer<String> listener) {
        onUsernameChangedListeners.add(listener);
    }
}
