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
public class StateUpdateDto {
    private Integer stateId;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "State name must contain only alphabets and white spaces")
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
