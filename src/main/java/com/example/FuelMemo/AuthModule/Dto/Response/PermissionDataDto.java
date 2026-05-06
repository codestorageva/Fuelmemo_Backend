package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionDataDto {

    private Integer permissionId;
    private String permissionName;

}
