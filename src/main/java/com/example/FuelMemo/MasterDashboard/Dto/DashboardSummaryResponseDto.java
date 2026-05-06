package com.example.FuelMemo.MasterDashboard.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardSummaryResponseDto {

    private Long totalCompanies;
    private Long activeCompanies;
    private Long inactiveCompanies;
    private Long totalUsers;
    private Long totalFuelMemos;
    private Long totalVehicles;
}
