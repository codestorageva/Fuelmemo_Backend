package com.example.FuelMemo.AuthModule.Service;

import com.example.FuelMemo.AuthModule.Dto.Request.RoleRequestDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto.UpdateRoleRequestDto;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.ListResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import org.springframework.security.core.Authentication;

public interface RoleService {

    // ================= CREATE =================
    MessageResponse createRole(RoleRequestDto dto, Authentication authentication);

    // ================= UPDATE =================
    MessageResponse updateRole(Integer roleId, UpdateRoleRequestDto dto, Authentication authentication);

    // ================= DELETE (SOFT DELETE) =================
    MessageResponse deleteRole(Integer roleId, Authentication authentication);

    // ================= RESTORE =================
    MessageResponse restoreRole(Integer roleId, Authentication authentication);

    // ================= GET ALL (PAGINATION + FILTER) =================
    HttpResponse getAllRoles(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted,
            Authentication authentication
    );

    // ================= GET BY COMPANY =================
    HttpResponse getRolesByCompany(Integer companyId, Authentication authentication);

    // ================= GET BY ID =================
    HttpResponse getRoleById(Integer id, Authentication authentication);

    // ================= ROLE BASED PERMISSION FETCH =================
    ListResponse getAllRoleByNonSoftDeletedAndCurrentUserRole(User loggedInUser);
}