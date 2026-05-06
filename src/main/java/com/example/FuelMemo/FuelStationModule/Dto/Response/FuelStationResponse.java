package com.example.FuelMemo.FuelStationModule.Dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelStationResponse {
    private Integer fuelStationId;
    private String fuelStationName;
    private Integer state;
    private Integer district;
    private String subDistrict;
    private String village;
    private String ownerName;
    private String ownerMobileNumber;
    private Boolean active;
    private Boolean deleted;
    private Integer companyId;
    private String companyName;
}
