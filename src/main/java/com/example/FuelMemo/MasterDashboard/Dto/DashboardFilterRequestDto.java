package com.example.FuelMemo.MasterDashboard.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardFilterRequestDto {

    private Integer companyId;
    private LocalDate fromDate;
    private LocalDate toDate;
}
