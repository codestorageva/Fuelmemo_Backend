package com.example.FuelMemo.AuthModule.Service;

import com.example.FuelMemo.AuthModule.Dto.Response.ModulePermissionDto;
import com.example.FuelMemo.AuthModule.Dto.Response.PermissionItemDto;
import com.example.FuelMemo.Shared.Response.HttpResponse;

import java.util.List;
import java.util.Map;

public interface PermissionService {


    Map<String, Map<String, List<PermissionItemDto>>> getGroupedPermissions();

    boolean hasPermission(String authorization, String countryRead);

    HttpResponse getAllPermissions(
            String keyword,
            String isDeleted,
            String status
    );
    HttpResponse getAllPermissionsStructured();
}
