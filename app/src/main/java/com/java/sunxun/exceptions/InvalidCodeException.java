package com.java.sunxun.exceptions;

public class InvalidCodeException extends Exception {
    public String code;

    public InvalidCodeException(String code) {
        super("Invalid code: " + code);
        this.code = code;
    }
}
