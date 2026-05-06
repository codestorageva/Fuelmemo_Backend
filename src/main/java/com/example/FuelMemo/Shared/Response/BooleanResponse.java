package com.example.FuelMemo.Shared.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BooleanResponse {
    HttpStatus successCode;
    private Boolean success;
    private Boolean isPermission;
}
