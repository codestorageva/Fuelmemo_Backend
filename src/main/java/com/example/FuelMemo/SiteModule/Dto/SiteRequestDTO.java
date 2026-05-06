package com.example.FuelMemo.SiteModule.Dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SiteRequestDTO {

    @NotBlank(message = "Site name is required")
    @Size(max = 150, message = "Site name must not exceed 150 characters")
    private String siteName;

    @Size(max = 50, message = "Site code must not exceed 50 characters")
    private String siteCode;

    @Size(max = 100, message = "Site owner name must not exceed 100 characters")
    private String siteOwnerName;

    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @NotNull(message = "State is required")
    private Integer stateId;

    @NotNull(message = "District is required")
    private Integer districtId;

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @NotNull(message = "Company is required")
    private Integer companyId;
}