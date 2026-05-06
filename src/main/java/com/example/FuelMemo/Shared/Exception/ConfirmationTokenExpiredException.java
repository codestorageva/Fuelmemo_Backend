package com.example.FuelMemo.Shared.Exception;

public class ConfirmationTokenExpiredException extends RuntimeException {

    public ConfirmationTokenExpiredException(String msg) {
        super(msg);
    }
}