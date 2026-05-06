package com.example.FuelMemo.ReportModule.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DateWiseFuelMemoReportDto {
    private LocalDate date;
    private String vehicleNumber;
    private String driverName;
    private String siteName;
    private String fuelStationName;
    private BigDecimal quantity;

    // Constructor
    public DateWiseFuelMemoReportDto(LocalDate date, String vehicleNumber, String driverName,
                                     String siteName, String fuelStationName, BigDecimal  quantity) {
        this.date = date;
        this.vehicleNumber = vehicleNumber;
        this.driverName = driverName;
        this.siteName = siteName;
        this.fuelStationName = fuelStationName;
        this.quantity = quantity;
    }

    // Getters & Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }

    public String getFuelStationName() { return fuelStationName; }
    public void setFuelStationName(String fuelStationName) { this.fuelStationName = fuelStationName; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}