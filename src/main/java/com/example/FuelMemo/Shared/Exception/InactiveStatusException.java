package com.example.FuelMemo.Shared.Exception;

public class InactiveStatusException extends RuntimeException {
    public InactiveStatusException() {
    }

    public InactiveStatusException(String msg) {
        super(msg);
    }

}
