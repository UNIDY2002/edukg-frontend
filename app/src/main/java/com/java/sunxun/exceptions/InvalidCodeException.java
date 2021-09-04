package com.java.sunxun.exceptions;

public class InvalidCodeException extends Exception {
    public InvalidCodeException(String code) {
        super("Invalid code: " + code);
    }
}
