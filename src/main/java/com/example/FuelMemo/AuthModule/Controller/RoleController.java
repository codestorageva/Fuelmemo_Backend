package com.example.FuelMemo.AuthModule.Controller;

import com.example.FuelMemo.AuthModule.Dto.Request.RoleRequestDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto.UpdateRoleRequestDto;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Service.RoleService;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.ListResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // ================= CREATE =================
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public MessageResponse createRole(
            @RequestBody RoleRequestDto dto,
            Authentication authentication) {

        return roleService.createRole(dto, authentication);
    }

    // ================= UPDATE =================
    @PutMapping("/update/{roleId}")
    @PreAuthorize("hasAuthority('UPDATE_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public MessageResponse updateRole(
            @PathVariable Integer roleId,
            @RequestBody UpdateRoleRequestDto dto,
            Authentication authentication) {

        return roleService.updateRole(roleId, dto, authentication);
    }

    // ================= DELETE =================
    @PutMapping("/soft-delete/{roleId}")
    @PreAuthorize("hasAuthority('DELETE_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> deleteRole(
            @PathVariable Integer roleId,
            Authentication authentication) {

        MessageResponse response = roleService.deleteRole(roleId, authentication);
        return ResponseEntity.ok(response);
    }

    // ================= RESTORE =================
    @PutMapping("/restore/{roleId}")
    @PreAuthorize("hasAuthority('RESTORE_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> restoreRole(
            @PathVariable Integer roleId,
            Authentication authentication) {

        MessageResponse response = roleService.restoreRole(roleId, authentication);
        return ResponseEntity.ok(response);
    }

    // ================= GET ALL =================
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public HttpResponse getAllRoles(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "false") Boolean isDeleted,
            Authentication authentication
    ) {
        return roleService.getAllRoles(
                keyword,
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                isDeleted,
                authentication
        );
    }

    // ================= GET BY COMPANY =================
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAuthority('VIEW_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public HttpResponse getRolesByCompany(
            @PathVariable Integer companyId,
            Authentication authentication
    ) {
        return roleService.getRolesByCompany(companyId, authentication);
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_ROLE')or hasRole('ROLE_SUPERADMIN')")
    public HttpResponse getRoleById(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        return roleService.getRoleById(id, authentication);
    }

    // ================= CURRENT USER ROLE PERMISSIONS =================
    @GetMapping("/permissionByCurrentUserRole")
    @PreAuthorize("isAuthenticated()or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<ListResponse> getAllRoleByNonSoftDeletedAndCurrentUserRole(
            @AuthenticationPrincipal User loggedInUser) {

        return ResponseEntity.ok(
                roleService.getAllRoleByNonSoftDeletedAndCurrentUserRole(loggedInUser)
        );
    }
}