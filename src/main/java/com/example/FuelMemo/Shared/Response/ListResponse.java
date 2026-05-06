package com.example.FuelMemo.Shared.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListResponse {
    private Boolean success;
    HttpStatus successCode;
    List<?> data = new ArrayList<>();
}
