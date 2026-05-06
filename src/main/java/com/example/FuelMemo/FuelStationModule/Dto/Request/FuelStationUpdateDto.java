package com.example.FuelMemo.FuelStationModule.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FuelStationUpdateDto {

    private String fuelStationName;

    private Integer stateId;

    private Integer districtId;

    private String subDistrict;

    private String village;

    private String ownerName;

    private String ownerMobileNumber;

    private Boolean deleted;

    private Integer companyId;
}
