package com.cround.cround.api;

public class SuccessfulResponse<T> {
    private ResponseResult<T> result;

    public ResponseResult<T> getResult() {
        return result;
    }
}
