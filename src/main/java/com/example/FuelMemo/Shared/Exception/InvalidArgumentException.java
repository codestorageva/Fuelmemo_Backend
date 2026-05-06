package com.example.FuelMemo.Shared.Exception;

public class InvalidArgumentException extends RuntimeException {
    public InvalidArgumentException() {
    }

    public InvalidArgumentException(String msg) {
        super(msg);
    }
}
