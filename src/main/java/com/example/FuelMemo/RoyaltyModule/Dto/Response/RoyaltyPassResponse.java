package com.example.FuelMemo.RoyaltyModule.Dto.Response;

import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoyaltyPassResponse {
    private Integer royaltyId;
    private String royaltyNumber;
    private Integer companyId;
    private PassStatus status;
    private Boolean deleted;
}
