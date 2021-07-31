package com.cround.cround.api;

public class ResponseResult<T> {
    private String message;
    private T data;

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
