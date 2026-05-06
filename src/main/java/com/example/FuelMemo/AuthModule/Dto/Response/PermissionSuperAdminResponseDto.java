package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionSuperAdminResponseDto {

    private String permissionSuperGroupName;
    private List<PermissionGroupDto> permissionGroups;

}