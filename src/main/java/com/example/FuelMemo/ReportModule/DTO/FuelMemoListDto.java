package com.example.FuelMemo.ReportModule.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FuelMemoListDto {

    private String id;
    private String vehicleNumber;
    private String driverName;
    private String quantity;
    private Boolean active;
    private String date;

    public FuelMemoListDto(
            String id,
            String vehicleNumber,
            String driverName,
            String quantity,
            Boolean active,
            String date
    ) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.driverName = driverName;
        this.quantity = quantity;
        this.active = active;
        this.date = date;
    }
}
