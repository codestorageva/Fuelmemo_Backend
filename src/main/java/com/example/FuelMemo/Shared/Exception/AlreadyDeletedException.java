package com.example.FuelMemo.Shared.Exception;

public class AlreadyDeletedException extends RuntimeException {
    public AlreadyDeletedException() {
    }

    public AlreadyDeletedException(String msg) {
        super(msg);
    }
}
