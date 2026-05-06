package com.example.FuelMemo.DriverModule.Dto;

import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class DriverResponseDto {

    private Integer driverId;
    private String driverName;

    private Set<CompanyResponseDto> companies;
}
