package com.example.FuelMemo.Shared.Exception;

public class DuplicateEntryException extends RuntimeException {
    public DuplicateEntryException() {
    }

    public DuplicateEntryException(String msg) {
        super(msg);
    }
}
