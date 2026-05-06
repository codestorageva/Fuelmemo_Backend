package com.example.FuelMemo.MasterData.Dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DistrictExportDTO {


    private Integer districtId;

    private String cityName;

    private String stateName;

    private String countryName;
    private String apiCityName;

    private String updatedAt;

}
