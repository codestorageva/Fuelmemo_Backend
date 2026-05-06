package com.example.FuelMemo.ReportModule.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiteFuelReportDto {

    private Integer siteId;
    private String siteName;
    private Double totalFuel;
    private Integer totalMemo;

}
