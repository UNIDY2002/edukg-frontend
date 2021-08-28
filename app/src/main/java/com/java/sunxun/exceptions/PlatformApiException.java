package com.java.sunxun.exceptions;

public class PlatformApiException extends Exception {
    public PlatformApiException(String code, String message) {
        super("Code: " + code + "; message: " + message);
    }
}
