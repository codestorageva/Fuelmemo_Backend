package com.example.FuelMemo.MasterData.Dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StateExportDTO {

    private Integer stateId;

    private String stateName;

    private String countryName;

    private String updatedAt;

}
