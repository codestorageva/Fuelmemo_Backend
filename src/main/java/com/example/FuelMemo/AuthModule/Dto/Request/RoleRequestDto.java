package com.example.FuelMemo.AuthModule.Dto.Request;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for creating/updating roles.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDto {

    private Integer roleId;
    private String roleName;
    private String description;
    private Boolean active;
    private Boolean systemDefined;
    private Boolean isDeleted; // soft delete flag
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // Nested DTO for permissions
    private List<PermissionDto> permissions;

    // Assign all permissions if true
    private Boolean allPermissions;

    // Assign permissions per module: key = moduleName, value = list of IDs (-1 = all in module)
    private Map<String, List<Integer>> modulePermissions;

    // Flat list of permission IDs
    private List<Integer> permissionIds;
    private Integer companyId;
}