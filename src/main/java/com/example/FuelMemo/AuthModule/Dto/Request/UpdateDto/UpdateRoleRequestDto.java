package com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto;


import com.example.FuelMemo.AuthModule.Dto.Request.PermissionDto;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UpdateRoleRequestDto {

    private String roleName;           // Optional (can rename role)
    private String description;
    private Boolean active;
    private Boolean systemDefined;
    private Integer companyId;

    private Boolean allPermissions;

    private Map<String, List<Integer>> modulePermissions;

    private List<Integer> permissionIds;

    private List<PermissionDto> permissions;
}