package com.java.sunxun.exceptions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InvalidCodeException extends Exception {
    public String code;
    @Nullable
    public String msg;

    public InvalidCodeException(String code) {
        super("Invalid code: " + code);
        this.code = code;
    }

    public InvalidCodeException(String code, @NonNull String msg) {
        super("Invalid code: " + code + ", msg: " + msg);
        this.code = code;
        this.msg = msg;
    }
}
