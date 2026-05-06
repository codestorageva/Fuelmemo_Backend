package com.example.FuelMemo.ReportModule.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DashboardSummaryDto {


    private long totalDrivers;
    private double driversGrowth;

    private long totalVehicles;
    private double vehiclesGrowth;

    private long totalFuelMemos;
    private double fuelMemoGrowth;

    private double monthlyUsage;
    private double usageGrowth;



}