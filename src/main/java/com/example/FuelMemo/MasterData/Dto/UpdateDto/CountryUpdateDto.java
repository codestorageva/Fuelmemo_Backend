package com.example.FuelMemo.MasterData.Dto.UpdateDto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountryUpdateDto {

    private Integer countryId;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Country name must contain only alphabets and white space")
    private String countryName;

    private Boolean status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
