package com.example.FuelMemo.VehicleModule.Dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateDTO {
    private String vehicleNumber;
    private String ownerName;
    private String ownerPhone;
    private Integer companyId; // optional update
}
