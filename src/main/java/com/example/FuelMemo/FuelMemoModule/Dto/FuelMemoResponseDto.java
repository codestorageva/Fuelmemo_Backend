package com.example.FuelMemo.FuelMemoModule.Dto;

import com.example.FuelMemo.FuelMemoModule.Entity.EntryType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelMemoResponseDto {

    private Integer id;
    private Integer srNo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate date;
    private BigDecimal quantity;

    private String preparedBy;

    // ---------------- VEHICLE ----------------
    private Integer vehicleId;
    private String vehicleName;

    // ---------------- DRIVER ----------------
    private Integer driverId;
    private String driverName;

    // ---------------- SITE ----------------
    private Integer siteId;
    private String siteName;

    private Integer royaltyId;
    private String royaltyNumber;

    // ---------------- FUEL STATION ----------------
    private Integer fuelStationId;
    private String fuelStationName;

    // ---------------- COMPANY ----------------
    private Integer companyId;
    private String companyName;

    private EntryType entryType;
    private Boolean deleted;

    private boolean editable;
    private String monthStatus;
    private Boolean isClosed;
    private Boolean isReopened;

    private String vehicleNo;
}