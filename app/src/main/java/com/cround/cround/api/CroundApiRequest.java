package com.cround.cround.api;

public class CroundApiRequest<T> {
    private T data;

    public CroundApiRequest(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
