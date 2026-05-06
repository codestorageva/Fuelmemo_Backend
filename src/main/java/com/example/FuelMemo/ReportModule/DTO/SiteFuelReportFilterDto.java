package com.example.FuelMemo.ReportModule.DTO;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SiteFuelReportFilterDto {

    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer siteId;

}
