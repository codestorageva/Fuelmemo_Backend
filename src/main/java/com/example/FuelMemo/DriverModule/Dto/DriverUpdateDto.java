package com.example.FuelMemo.DriverModule.Dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverUpdateDto {

    private String driverName;
    private Object  companyId;
}