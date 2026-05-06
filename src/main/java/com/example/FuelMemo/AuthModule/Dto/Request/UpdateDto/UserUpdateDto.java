package com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {

    private String userName;

    private String email;

    private String password;

    private String firstName;


    private String lastName;

    private LocalDate dob;

    private String mobNo;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private List<Integer> companyIds;
    private List<Integer> roleIds;


}
