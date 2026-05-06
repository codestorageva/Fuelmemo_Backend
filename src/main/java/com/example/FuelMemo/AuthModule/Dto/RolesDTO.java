package com.example.FuelMemo.AuthModule.Dto;

import com.example.FuelMemo.AuthModule.Dto.Request.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolesDTO {
    private String roleName;
    private List<UserDto> users;
}
