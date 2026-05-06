package com.example.FuelMemo.FuelStationModule.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelStationDto {

    @NotBlank(message = "Fuel station name is required")
    @Size(max = 150, message = "Fuel station name cannot exceed 150 characters")
    private String fuelStationName;

    @NotNull(message = "State is required")
    private Integer stateId;

    @NotNull(message = "District is required")
    private Integer  districtId;

    @NotBlank(message = "Sub-district is required")
    private String  subDistrict;

    @NotBlank(message = "Village is required")
    private String village;

    @Size(max = 100, message = "Owner name cannot exceed 100 characters")
    private String ownerName;

    @Pattern(regexp = "^[0-9]{10}$", message = "Owner mobile number must be 10 digits")
    private String ownerMobileNumber;

    @NotNull(message = "Company ID is required")
    private Integer companyId;
}