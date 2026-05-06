package com.example.FuelMemo.VehicleModule.Dto;

import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class VehicleResponseDTO {

    private Integer vehicleId;
    private String vehicleNumber;
    private String ownerName;
    private String ownerPhone;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CompanyResponseDto company;

}
