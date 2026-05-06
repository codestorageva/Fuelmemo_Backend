package com.example.FuelMemo.FuelMemoModule.Dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FuelMemoUpdateRequestDto {

    private Integer srNo;
    private LocalDate date;
    private BigDecimal quantity;
    private String preparedBy;

    private Integer vehicleId;
    private Integer driverId;
    private Integer siteId;
    private Integer fuelStationId;
    private Integer companyId;

    private Integer royaltyPassId;
}
