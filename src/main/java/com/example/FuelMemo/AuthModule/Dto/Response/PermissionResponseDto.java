package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponseDto {

    private Integer permissionId;
    private String permissionName;
    private String permissionSuperGroup;
    private String permissionGroup;
//    private String actionName;
//    private Boolean active;
//    private Boolean isDeleted;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
}
