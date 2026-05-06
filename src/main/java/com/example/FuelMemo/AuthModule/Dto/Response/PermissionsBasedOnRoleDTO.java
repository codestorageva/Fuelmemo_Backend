package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PermissionsBasedOnRoleDTO {

    private Integer permissionId;
    private String permissionSuperGroup;
    private String permissionGroup;
    private String permissionName;
}
