package com.example.FuelMemo.VehicleModule.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor

public class VehicleSummaryDto {

    private String vehicleNumber;
    private String ownerName;
    private Boolean active;
    private BigDecimal totalFuel;
    private Long totalTrips;
    private LocalDate lastFuelDate;
    private Boolean monthClosed;


    public VehicleSummaryDto(String vehicleNumber, String ownerName, Boolean active,
                             BigDecimal totalFuel, Long totalTrips,
                             LocalDate lastFuelDate, Boolean monthClosed) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.active = active;
        this.totalFuel = totalFuel;
        this.totalTrips = totalTrips;
        this.lastFuelDate = lastFuelDate;
        this.monthClosed = monthClosed;
    }


}