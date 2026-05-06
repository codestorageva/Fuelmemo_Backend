package com.example.FuelMemo.AuthModule.Service;

import com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto.UserUpdateDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UserDto;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import org.springframework.security.core.Authentication;

public interface UserService {

    MessageResponse addUser(UserDto request, Authentication authentication);

//    DataResponse getAllDeletedUsers();
//
//    DataResponse getUserById(Integer userId);
//
//    DataResponse getUsersByCompanyId(Integer companyId);
DataResponse getUserById(Integer userId, Authentication authentication);

    // ================= GET ALL (CURRENT COMPANY) =================
    DataResponse getUsers(Authentication authentication, Integer companyId);

    // ================= GET DELETED USERS (SUPERADMIN ONLY) =================
    DataResponse getAllDeletedUsers(Authentication authentication);

    HttpResponse getAllUsers(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDir,
            Authentication authentication
    );


    MessageResponse updateUser(Integer userId, UserUpdateDto dto, Authentication authentication);

    MessageResponse softDeleteUser(Integer userId, Authentication authentication);

    MessageResponse restoreUser(Integer userId, Authentication authentication);

    MessageResponse deleteUser(Integer userId, Authentication authentication);
}
