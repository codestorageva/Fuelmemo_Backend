package com.example.FuelMemo.AuthModule.Dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    private Integer roleId;
    private String roleName;
    private Boolean systemDefined;
}

