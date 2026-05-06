package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDto {

    private Integer roleId;

    private String roleName;

    private String description;

    private Boolean active;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private Integer companyId;



//    // For API/UI that expects PermissionResponseDto
//    private Set<PermissionResponseDto> permissionsDto;
//
//    // For internal use that expects PermissionResponse
//    private Set<PermissionResponse> permissions;
private List<PermissionSuperAdminResponseDto> permissions;
}