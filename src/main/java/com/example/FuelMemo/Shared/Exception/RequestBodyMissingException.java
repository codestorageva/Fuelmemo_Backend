package com.example.FuelMemo.Shared.Exception;

public class RequestBodyMissingException extends RuntimeException {

    public RequestBodyMissingException() {
    }

    public RequestBodyMissingException(String msg) {
        super(msg);
    }
}