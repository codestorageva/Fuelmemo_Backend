package com.example.FuelMemo.VehicleModule.Dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyVehicleResponseDTO {

    private Integer companyId;
    private String companyName;
    private List<VehicleResponseDTO> vehicles;
}