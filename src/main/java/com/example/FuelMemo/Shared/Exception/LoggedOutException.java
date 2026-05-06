package com.example.FuelMemo.Shared.Exception;

public class LoggedOutException extends RuntimeException {
    public LoggedOutException() {
    }

    public LoggedOutException(String msg) {
        super(msg);
    }

}
