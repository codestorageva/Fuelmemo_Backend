package com.example.FuelMemo.RoyaltyModule.Dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoyaltyPassUpdateDto {

    private String royaltyNumber;

    private Integer companyId;

    private Boolean deleted;

}
