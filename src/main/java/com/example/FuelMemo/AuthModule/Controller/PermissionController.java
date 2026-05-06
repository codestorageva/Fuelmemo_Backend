package com.example.FuelMemo.AuthModule.Controller;

import com.example.FuelMemo.AuthModule.Dto.Response.ModulePermissionDto;
import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")

public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/grouped")
    public HttpResponse getGroupedPermissions() {

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(permissionService.getGroupedPermissions())
                .build();
    }
    @PreAuthorize("hasAuthority('VIEW_GROUP_PERMISSION')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public HttpResponse getAllPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") String isDeleted,
            @RequestParam(defaultValue = "true") String status
    ) {
        return permissionService.getAllPermissions(
                keyword,
                isDeleted,
                status
        );
    }
        @PreAuthorize("hasAuthority('VIEW_GROUP_PERMISSION')or hasRole('ROLE_SUPERADMIN')")
        @GetMapping("/structured")
        public HttpResponse getAllPermissionsStructured() {
            return permissionService.getAllPermissionsStructured();
        }
}
