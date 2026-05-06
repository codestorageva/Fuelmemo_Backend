package com.example.FuelMemo.AuthModule.Dto;

import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.CompanyModule.Dto.CompanyDto;
import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    private Boolean success;
    private HttpStatus successCode;
    private String email;
    private String userName;
    private String mobNo;
    private List<RoleDto> roles;
    private Set<String> permissions;
    private List<CompanyResponseDto> companies; // if needed
    private String accessToken;
    private String refreshToken;
    private Boolean isSystemUser;

    // Add this:
    private Integer userId;
}