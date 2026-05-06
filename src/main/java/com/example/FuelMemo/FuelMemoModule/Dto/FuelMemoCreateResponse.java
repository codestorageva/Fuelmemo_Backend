package com.example.FuelMemo.FuelMemoModule.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public class FuelMemoCreateResponse {
    private Integer srNo;
    private String message;
    private boolean success;
    private HttpStatus successCode;
}
