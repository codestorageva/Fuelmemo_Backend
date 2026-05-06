package com.example.FuelMemo.AuthModule.Dto.Response;


import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer userId;
    private String userName;
    private String email;
    private String pwd;
    private String firstName;
    private String lastName;
    private LocalDate dob;
    private String mobNo;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
//    private List<Integer> companyIds;
//    private List<Integer> roleIds;
private List<CompanyResponseDto> companies;
    private List<RoleResponseDto> roles;

//    private Set<String> roles; // role names
}

