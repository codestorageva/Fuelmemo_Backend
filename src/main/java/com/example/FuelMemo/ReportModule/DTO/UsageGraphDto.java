package com.example.FuelMemo.ReportModule.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsageGraphDto {
    private String label;
    private double value;
}
