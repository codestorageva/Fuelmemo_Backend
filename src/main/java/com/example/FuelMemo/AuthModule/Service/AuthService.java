package com.example.FuelMemo.AuthModule.Service;

import com.example.FuelMemo.AuthModule.Dto.AuthenticationResponse;
import com.example.FuelMemo.AuthModule.Dto.LoginRequest;
import com.example.FuelMemo.Shared.Response.MessageResponse;

public interface AuthService {
    AuthenticationResponse login(LoginRequest request);

    AuthenticationResponse refreshToken(String refreshToken);

    MessageResponse logout(String accessToken);
}
