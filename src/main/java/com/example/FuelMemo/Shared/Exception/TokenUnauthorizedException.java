package com.example.FuelMemo.Shared.Exception;

public class TokenUnauthorizedException extends RuntimeException {

    public TokenUnauthorizedException(String msg) {
        super(msg);
    }
}
