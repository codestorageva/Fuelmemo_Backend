package com.example.FuelMemo.Shared.Exception;

public class ResourceAlreadyExistException extends RuntimeException {
    public ResourceAlreadyExistException() {

    }

    public ResourceAlreadyExistException(String msg) {
        super(msg);
    }
}
