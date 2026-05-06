package com.example.FuelMemo.SiteModule.Dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteResponseDTO {

    private Integer siteId;
    private String siteName;
    private String siteCode;
    private String siteOwnerName;
    private String address;
    private String pincode;

    private Integer stateId;
    private String stateName;

    private Integer districtId;
    private String cityName;

    private Integer companyId;
    private String companyName;

    private Boolean active;
}
