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
public class LongResponse {
    private Boolean success;
    HttpStatus successCode;
    private Long count;

}
