package com.cround.cround.api;

public class ResponseError {
    private int errno;
    private String message;

    public int getErrno() {
        return errno;
    }

    public String getMessage() {
        return message;
    }
}
