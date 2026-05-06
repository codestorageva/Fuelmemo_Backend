package com.example.FuelMemo.Shared.Exception;

public class CustomNullPointerException extends RuntimeException {

    public CustomNullPointerException() {
    }

    public CustomNullPointerException(String msg) {
        super(msg);
    }
}
