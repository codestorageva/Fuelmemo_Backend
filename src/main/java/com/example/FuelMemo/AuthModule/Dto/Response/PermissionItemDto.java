package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PermissionItemDto {

    private Integer permissionId;
    private String permissionName;
    private String actionName;
//    private String permissionSuperGroup;
}

