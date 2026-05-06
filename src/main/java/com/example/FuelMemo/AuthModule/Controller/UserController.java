package com.example.FuelMemo.AuthModule.Controller;


import com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto.UserUpdateDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UserDto;
import com.example.FuelMemo.AuthModule.Service.UserService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")

public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PreAuthorize("hasAuthority('CREATE_USER')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/create")
    public ResponseEntity<MessageResponse> addUser(
            @RequestBody UserDto userDto,
            Authentication authentication   // 🔥 add this
    ) {
        MessageResponse response = userService.addUser(userDto, authentication);
        return ResponseEntity.status(response.getSuccessCode()).body(response);
    }

    @PreAuthorize("hasAuthority('VIEW_USER')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/{userId}")
    public ResponseEntity<DataResponse> getUserById(
            @PathVariable Integer userId,
            Authentication authentication
    ) {
        DataResponse response = userService.getUserById(userId, authentication);
        return ResponseEntity.ok(response);
    }

    // ================= GET ALL USERS (CURRENT COMPANY) =================
    @PreAuthorize("hasAuthority('VIEW_USER')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public ResponseEntity<DataResponse> getUsers(
            Authentication authentication,
            @RequestParam(required = false) Integer companyId
    ) {
        DataResponse response = userService.getUsers(authentication, companyId);
        return ResponseEntity.ok(response);
    }
    // ================= GET DELETED USERS (SUPERADMIN) =================
    @PreAuthorize("hasAuthority('VIEW_USER')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/deleted")
    public ResponseEntity<DataResponse> getDeletedUsers(Authentication authentication) {
        DataResponse response = userService.getAllDeletedUsers(authentication);
        return ResponseEntity.ok(response);
    }


    // ================= GET ALL USERS =================
    @PreAuthorize("hasAuthority('VIEW_USER')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("selectAll")
    public HttpResponse getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication
    ) {
        return userService.getAllUsers(keyword, pageNumber, pageSize, sortBy, sortDir, authentication);
    }

    // ================= UPDATE USER =================
    @PreAuthorize("hasAuthority('UPDATE_USER')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/update/{userId}")
    public ResponseEntity<MessageResponse> updateUser(
            @PathVariable Integer userId,
            @RequestBody UserUpdateDto userDto,
            Authentication authentication   // 🔥 ADD
    ) {
        MessageResponse response = userService.updateUser(userId, userDto, authentication);
        return ResponseEntity.status(response.getSuccessCode()).body(response);
    }

    // ================= SOFT DELETE USER =================
    @PreAuthorize("hasAuthority('DELETE_USER')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/softDelete/{userId}")
    public MessageResponse softDeleteUser(
            @PathVariable Integer userId,
            Authentication authentication
    ) {
        return userService.softDeleteUser(userId, authentication);
    }


    // ================= RESTORE USER =================
    @PreAuthorize("hasAuthority('RESTORE_USER')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/restore/{userId}")
    public MessageResponse restoreUser(
            @PathVariable Integer userId,
            Authentication authentication
    ) {
        return userService.restoreUser(userId, authentication);
    }

    // ================= DELETE USER =================
    @PreAuthorize("hasAuthority('DELETE_USER')or hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("/delete/{userId}")
    public MessageResponse deleteUser(
            @PathVariable Integer userId,
            Authentication authentication
    ) {
        return userService.deleteUser(userId, authentication);
    }
}