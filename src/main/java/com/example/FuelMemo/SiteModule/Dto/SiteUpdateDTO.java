package com.example.FuelMemo.SiteModule.Dto;

import lombok.Data;

@Data
public class SiteUpdateDTO {

    private Integer siteId;
    private String siteName;
    private String siteCode;
    private String siteOwnerName;
    private String address;
    private Integer stateId;
    private Integer districtId;
    private String pincode;
    private Integer companyId;
}