package com.example.FuelMemo.CompanyModule.Dto.Response;

import com.example.FuelMemo.AuthModule.Dto.Response.RoleResponseDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompanyResponseDto {

    private Integer companyId;
    private String companyName;
    private List<RoleResponseDto> roles;
}
