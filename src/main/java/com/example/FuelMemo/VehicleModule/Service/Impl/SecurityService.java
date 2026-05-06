package com.example.FuelMemo.VehicleModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    /**
     * Check if current user can access a company
     */
    public boolean canAccessCompany(Authentication authentication, Integer companyId) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            return false;
        }

        // SUPERADMIN check
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("ROLE_SUPERADMIN"));

        if (isSuperAdmin) {
            return true;
        }

        // Company access check
        return user.getUserCompanies().stream()
                .map(UserCompany::getCompany)
                .anyMatch(company -> company.getCompanyId().equals(companyId));
    }

    public Integer getCompanyIdFromAuth(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User user)) {
            throw new RuntimeException("Invalid user");
        }

        // 👉 SUPERADMIN case (optional logic)
        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("ROLE_SUPERADMIN"));

        if (isSuperAdmin) {
            // 👉 SUPERADMIN માટે default company return કે exception
            throw new RuntimeException("SuperAdmin must specify company");
        }

        // 👉 FIRST company return (common case)
        return user.getUserCompanies().stream()
                .map(UserCompany::getCompany)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No company assigned"))
                .getCompanyId();
    }

    /**
     * Check if user has a role
     */
    public boolean hasRole(Authentication auth, String roleName) {

        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof User user)) {
            return false;
        }

        return user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals(roleName));
    }
}