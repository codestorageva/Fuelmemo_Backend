package com.example.FuelMemo.Shared.Response;


import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DataResponse {
    private Boolean success;
    HttpStatus successCode;
    private Object data;
}
