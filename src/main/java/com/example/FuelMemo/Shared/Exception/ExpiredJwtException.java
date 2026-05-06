package com.example.FuelMemo.Shared.Exception;

public class ExpiredJwtException extends RuntimeException {
    public ExpiredJwtException(String msg) {
        super(msg);
    }
}
