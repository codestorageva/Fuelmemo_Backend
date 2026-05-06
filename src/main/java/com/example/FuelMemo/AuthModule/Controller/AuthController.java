package com.example.FuelMemo.AuthModule.Controller;

import com.example.FuelMemo.AuthModule.Dto.AuthenticationResponse;
import com.example.FuelMemo.AuthModule.Dto.LoginRequest;
import com.example.FuelMemo.AuthModule.Service.AuthService;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/api/auth")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ===================== LOGIN =====================
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ===================== REFRESH TOKEN =====================
    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@RequestParam String refreshToken) {
        AuthenticationResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    // ===================== LOGOUT =====================
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String authHeader) {
        // Extract token from "Bearer <token>"
        String token = authHeader.replace("Bearer ", "").trim();

        // Call service to logout and get MessageResponse
        MessageResponse response = authService.logout(token);

        // Return response with HTTP status from MessageResponse
        return new ResponseEntity<>(response, response.getSuccessCode());
    }
}


