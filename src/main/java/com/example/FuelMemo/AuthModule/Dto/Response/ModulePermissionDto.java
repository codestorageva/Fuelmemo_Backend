package com.example.FuelMemo.AuthModule.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ModulePermissionDto {

    private String moduleName;
    private List<PermissionItemDto> permissions;
}

