package com.example.FuelMemo.AuthModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Dto.*;
import com.example.FuelMemo.AuthModule.Entity.*;
import com.example.FuelMemo.AuthModule.Repository.*;
import com.example.FuelMemo.AuthModule.Service.AuthService;
import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import com.example.FuelMemo.Security.jwt.JwtService;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserCompanyRepository userCompanyRepository;

    // ===================== LOGIN =====================
    @Override
    public AuthenticationResponse login(LoginRequest request) {

        String loginInput = request.getIdentifier();

        User user = userRepository.findByEmailIgnoreCaseOrUserNameIgnoreCase(loginInput, loginInput);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + loginInput);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new AuthenticationException("Invalid credentials");
        }

        user.setIsLoggedOut(false);

        String deviceId = "WEB";
        String accessToken = jwtService.generateAccessToken(user, deviceId);

        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .deviceId(deviceId)
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        user.setJwtToken(accessToken);
        userRepository.save(user);

        return buildAuthResponse(user, accessToken, refreshTokenValue);
    }

    // ===================== REFRESH =====================
    @Override
    public AuthenticationResponse refreshToken(String refreshTokenValue) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue);
        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        User user = refreshToken.getUser();

        if (Boolean.TRUE.equals(user.getIsLoggedOut())) {
            throw new AuthenticationException("User logged out");
        }

        String newToken = jwtService.generateAccessToken(user, refreshToken.getDeviceId());

        user.setJwtToken(newToken);
        userRepository.save(user);

        return buildAuthResponse(user, newToken, refreshTokenValue);
    }

    // ===================== LOGOUT =====================
    @Override
    public MessageResponse logout(String accessToken) {

        String email = jwtService.extractUsername(accessToken);
        User user = userRepository.findByEmailIgnoreCase(email);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        user.setIsLoggedOut(true);
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setJwtToken(null);

        refreshTokenRepository.deleteByUser(user);
        userRepository.save(user);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Logged out successfully")
                .build();
    }

    // ===================== AUTH RESPONSE =====================
    public AuthenticationResponse buildAuthResponse(User user, String accessToken, String refreshToken) {

        boolean isSystemUser = isSystemUser(user);

        return isSystemUser
                ? buildSystemAuthResponse(user, accessToken, refreshToken)
                : buildCompanyAuthResponse(user, accessToken, refreshToken);
    }

    // ===================== SYSTEM USER CHECK =====================
    private boolean isSystemUser(User user) {

        if (user.getRoles() == null) return false;

        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(Role::getRoleName)
                .anyMatch(r ->
                        "ROLE_SUPERADMIN".equalsIgnoreCase(r) ||
                                "ROLE_PP".equalsIgnoreCase(r)
                );
    }

    // ===================== SYSTEM RESPONSE =====================
    private AuthenticationResponse buildSystemAuthResponse(User user, String accessToken, String refreshToken) {

        List<RoleDto> roles = Optional.ofNullable(user.getRoles())
                .orElse(Collections.emptySet())
                .stream()
                .filter(Objects::nonNull)
                .map(r -> RoleDto.builder()
                        .roleId(r.getRoleId())
                        .roleName(r.getRoleName())
                        .build())
                .toList();

        Set<String> permissions = Optional.ofNullable(user.getRoles())
                .orElse(Collections.emptySet())
                .stream()
                .filter(Objects::nonNull)
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .filter(Objects::nonNull)
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());

        return AuthenticationResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .email(user.getEmail())
                .userName(user.getUserName())
                .mobNo(user.getMobNo())
                .roles(roles)
                .permissions(permissions)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isSystemUser(true)
                .userId(user.getUserId())
                .build();
    }

    // ===================== COMPANY RESPONSE =====================
    private AuthenticationResponse buildCompanyAuthResponse(User user, String accessToken, String refreshToken) {

        Set<UserCompany> userCompanies = getUserCompanies(user);

        validateUserCompanies(userCompanies);

        List<RoleDto> roles = userCompanies.stream()
                .map(UserCompany::getRole)
                .filter(Objects::nonNull)
                .map(r -> RoleDto.builder()
                        .roleId(r.getRoleId())
                        .roleName(r.getRoleName())
                        .build())
                .toList();

        Set<String> permissions = userCompanies.stream()
                .map(UserCompany::getRole)
                .filter(Objects::nonNull)
                .filter(r -> r.getPermissions() != null)
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());

        List<CompanyResponseDto> companies = userCompanies.stream()
                .map(UserCompany::getCompany)
                .filter(Objects::nonNull)
                .map(c -> CompanyResponseDto.builder()
                        .companyId(c.getCompanyId())
                        .companyName(c.getCompanyName())
                        .build())
                .toList();

        return AuthenticationResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .email(user.getEmail())
                .userName(user.getUserName())
                .mobNo(user.getMobNo())
                .roles(roles)
                .permissions(permissions)
                .companies(companies)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isSystemUser(false)
                .userId(user.getUserId())
                .build();
    }

    // ===================== USER COMPANIES =====================
    private Set<UserCompany> getUserCompanies(User user) {

        if (isSystemUser(user)) {
            return Collections.emptySet();
        }

        return new HashSet<>(
                userCompanyRepository.findByUserWithRoleAndCompany(user)
        );
    }

    // ===================== VALIDATION =====================
    private void validateUserCompanies(Set<UserCompany> userCompanies) {

        if (userCompanies == null || userCompanies.isEmpty()) {
            throw new RuntimeException("User company not found");
        }

        userCompanies.forEach(uc -> {
            if (uc.getRole() == null) {
                throw new RuntimeException(
                        "Role missing for company: " +
                                (uc.getCompany() != null ? uc.getCompany().getCompanyId() : "NULL")
                );
            }
        });
    }

    // ===================== EXCEPTIONS =====================
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String msg) { super(msg); }
    }

    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String msg) { super(msg); }
    }

    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String msg) { super(msg); }
    }
}