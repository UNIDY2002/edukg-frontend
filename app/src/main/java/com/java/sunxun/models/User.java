package com.java.sunxun.models;

import androidx.annotation.NonNull;
import com.java.sunxun.exceptions.ApplicationLoginFailureException;
import com.java.sunxun.network.ApplicationNetwork;
import com.java.sunxun.network.NetworkHandler;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class User {
    private final String username;
    private final String password;

    private final static ArrayList<Consumer<String>> onUsernameChangedListeners = new ArrayList<>();

    public final static User VISITOR = new User("visitor", "12345678");

    @NonNull
    public static User currentUser = User.VISITOR;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    private static void performLogin(String username, String password, NetworkHandler<Boolean> handler) {
        ApplicationNetwork.login(username, password, new NetworkHandler<String>(handler.activity) {
            @Override
            public void onSuccess(String result) {
                ApplicationNetwork.getId(new NetworkHandler<String>(handler.activity) {
                    @Override
                    public void onSuccess(String result) {
                        handler.onSuccess(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        handler.onError(e);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                handler.onError(e);
            }
        });
    }

    public static void login(String username, String password, NetworkHandler<User> handler) {
        if (!Objects.equals(VISITOR.username, username)) {
            performLogin(username, password, new NetworkHandler<Boolean>(handler.activity) {
                @Override
                public void onSuccess(Boolean result) {
                    onUsernameChangedListeners.forEach(listener -> listener.accept(username));
                    handler.onSuccess(currentUser = new User(username, password));
                }

                @Override
                public void onError(Exception e) {
                    handler.onError(e);
                }
            });
        } else {
            handler.onError(new ApplicationLoginFailureException());
        }
    }

    public void refreshCredentials(NetworkHandler<Boolean> handler) {
        performLogin(username, password, handler);
    }

    public void logout() {
        currentUser = VISITOR;
        onUsernameChangedListeners.forEach(listener -> listener.accept(VISITOR.username));
    }

    public static boolean isVisitor() {
        return Objects.equals(currentUser.username, VISITOR.username);
    }

    public static void addOnUsernameChangedListener(Consumer<String> listener) {
        onUsernameChangedListeners.add(listener);
    }
}
