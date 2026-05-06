package com.example.FuelMemo.AuthModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Dto.Response.PermissionResponseDto;
import com.example.FuelMemo.AuthModule.Dto.Request.PermissionDto;
import com.example.FuelMemo.AuthModule.Dto.Response.ModulePermissionDto;
import com.example.FuelMemo.AuthModule.Dto.Response.PermissionItemDto;
import com.example.FuelMemo.AuthModule.Dto.Response.PermissionSuperAdminResponseDto;
import com.example.FuelMemo.AuthModule.Entity.Permission;
import com.example.FuelMemo.AuthModule.Repository.PermissionRepository;
import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final DataUtil dateUtil;

    @Override
    public Map<String, Map<String, List<PermissionItemDto>>> getGroupedPermissions() {

        List<Permission> permissions = permissionRepository.findAll();

        return permissions.stream()
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .collect(Collectors.groupingBy(
                        Permission::getPermissionSuperGroup,   // 1️⃣ Top Level
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                Permission::getPermissionGroup, // 2️⃣ Second Level
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        p -> new PermissionItemDto(
                                                p.getPermissionId(),
                                                p.getPermissionName(),
                                                p.getActionName()
//                                                p.getPermissionSuperGroup()
                                        ),
                                        Collectors.toList()
                                )
                        )
                ));
    }
    @Override
    public boolean hasPermission(String authorization, String permissionName) {
        // 1️⃣ Extract user/role info from the token if needed
        // For simplicity, let's assume authorization contains the user info
        // or you can skip it if you already handle user/role context elsewhere.

        // 2️⃣ Check if the permission exists in DB
        Permission permission = permissionRepository.findByPermissionName(permissionName);

        return permission != null; // or more complex logic if needed
    }

    @Override
    public HttpResponse getAllPermissions(
            String keyword,
            String isDeleted,
            String status) {

        Boolean deleted = Boolean.valueOf(isDeleted);
        Boolean active = Boolean.valueOf(status);

        List<Permission> permissionList;

        if (keyword != null && !keyword.isEmpty()) {
            permissionList = permissionRepository
                    .findByPermissionNameContainingIgnoreCaseAndIsDeletedAndActive(
                            keyword, deleted, active);
        } else {
            permissionList = permissionRepository
                    .findByIsDeletedAndActive(deleted, active);
        }

        List<PermissionResponseDto> dtoList =
                dateUtil.permissionsToDtos(permissionList);

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dtoList)
                .build();
    }
    @Override
    public HttpResponse getAllPermissionsStructured() {

        List<Permission> permissions = permissionRepository.findAll();

        List<PermissionSuperAdminResponseDto> responseDto =
                dateUtil.mapToStructuredPermissions(permissions);

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(responseDto)
                .build();
    }

}

