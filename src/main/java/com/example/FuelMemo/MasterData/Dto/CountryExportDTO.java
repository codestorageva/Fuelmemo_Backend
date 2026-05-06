package com.example.FuelMemo.MasterData.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountryExportDTO {
    private Integer countryId;
    private String countryName;
    private String updatedAt;
}
