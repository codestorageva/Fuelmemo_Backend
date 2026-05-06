package com.example.FuelMemo.ReportModule.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FuelMemoDetailReportDto {

    private Integer srNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate date;
    private String vehicleNo;
    private String driverName;
    private String siteName;
    private String fuelStationName;
    private String royaltyPassNo;
    private BigDecimal quantity;
    private String preparedBy;
    private String entryType;

}