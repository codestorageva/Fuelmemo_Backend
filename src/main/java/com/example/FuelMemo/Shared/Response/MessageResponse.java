package com.example.FuelMemo.Shared.Response;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private Boolean success;
    private HttpStatus successCode;
    private String message;
}