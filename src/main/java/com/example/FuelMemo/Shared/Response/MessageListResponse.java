package com.example.FuelMemo.Shared.Response;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageListResponse {
    HttpStatus successCode;
    Object  data = new HashMap<>();
    private boolean success;
    private String message;
}