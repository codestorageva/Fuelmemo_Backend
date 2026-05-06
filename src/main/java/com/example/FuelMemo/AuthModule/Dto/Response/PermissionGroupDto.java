package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionGroupDto {

    private String permissionGroupName;
    private List<PermissionDataDto> permissionData;

}
