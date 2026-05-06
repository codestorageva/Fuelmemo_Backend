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
public class SubDistrictUpdateDto {

    private Integer subDistrictId;

    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Sub-District name must contain only alphabets and white spaces")
    private String subDistrictName;

    //    @NotNull(message = "District ID should not be null!")
//    @Min(value = 1, message = "District ID must be a positive integer!")
    private Integer districtId;
    private String cityName;

    //    @NotNull(message = "State ID should not be null!")
//    @Min(value = 1, message = "State ID must be a positive integer!")
    private Integer stateId;
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
