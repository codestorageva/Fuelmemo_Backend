package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {

    private Integer permissionId;
    private String permissionName;
}
