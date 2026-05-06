package com.example.FuelMemo.FuelMemoModule.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FuelMemoRequestDto {

    @NotNull(message = "SR No is required")
    private Integer srNo;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    private String preparedBy;

    @NotNull(message = "Vehicle ID is required")
    private Integer vehicleId;

    @NotNull(message = "Driver ID is required")
    private Integer driverId;

    @NotNull(message = "Site ID is required")
    private Integer siteId;

    @NotNull(message = "Fuel Station ID is required")
    private Integer fuelStationId;

    @NotNull(message = "Company ID is required")
    private Integer companyId;

    private Integer royaltyPassId; // optional
}
