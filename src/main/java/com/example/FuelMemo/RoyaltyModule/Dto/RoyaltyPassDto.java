package com.example.FuelMemo.RoyaltyModule.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoyaltyPassDto {

    @NotBlank(message = "Royalty number is required")
    private String royaltyNumber;

    @NotNull(message = "Company ID is required")
    private Integer companyId;

}
