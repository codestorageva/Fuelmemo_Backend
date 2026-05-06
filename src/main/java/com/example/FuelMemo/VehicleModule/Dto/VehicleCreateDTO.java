package com.example.FuelMemo.VehicleModule.Dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleCreateDTO {

    private String vehicleNumber;
    private String ownerName;
    private String ownerPhone;
    private Boolean active;

    private Integer companyId;   // multiple companies assign karva mate
}