package com.example.FuelMemo.AuthModule.Dto.Request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private String identifier; // user can type email or username
    private String password;
}