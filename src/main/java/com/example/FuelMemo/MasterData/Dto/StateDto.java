package com.example.FuelMemo.MasterData.Dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class StateDto {
    private Integer stateId;

    @NotEmpty(message = "State name should not be empty!")
    @NotNull(message = "State name should not be null!")
    @Pattern(regexp = "^[a-zA-Z ]{3,}$", message = "State name must contain only alphabets with at least 3 characters!")
    private String stateName;

    //    @NotNull(message = "Country ID should not be null!")
    //    @Min(value = 1, message = "Country ID must be a positive integer!")
    private Integer countryId;
    private String countryName;

    private Boolean status;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
