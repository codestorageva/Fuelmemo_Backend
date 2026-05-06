package com.example.FuelMemo.DriverModule.Dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverRequestDto {

    private String driverName;
    private Integer companyId;  // 🔥 change here
}