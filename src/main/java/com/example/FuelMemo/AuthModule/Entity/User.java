package com.example.FuelMemo.AuthModule.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true, length = 100)
    private String userName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String pwd;

    private String firstName;
    private String lastName;
    private LocalDate dob;

    @Column(length = 15)
    private String mobNo;

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    // ================= ROLE MAPPING =================
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private final Set<Role> roles = new HashSet<>();

    // ================= COMPANY MAPPING =================


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private final Set<UserCompany> userCompanies = new HashSet<>();
//                public boolean hasPermission(String permissionName,Integer companyId) {
//                    if (userCompanies == null) return false;
//
//                    return userCompanies.stream()
//                            .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
//                            .map(UserCompany::getRole)
//                            .filter(role -> role != null && Boolean.TRUE.equals(role.getActive()))
//                            .flatMap(role -> role.getPermissions() == null
//                                    ? Stream.empty()
//                                    : role.getPermissions().stream())
//                            .filter(p -> Boolean.TRUE.equals(p.getActive()))
//                            .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
//                            .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permissionName));
//                }

    @Column(columnDefinition = "text")
    private String jwtToken;

    @Column(name = "token_version")
    private Integer tokenVersion = 0;

    @Column(columnDefinition = "text")
    private String lastActiveToken;

    private Boolean isDeleted = false;
    private Boolean isLoggedOut = false;
    private Boolean activeStatus = true;

    // ================= AUDIT FIELDS =================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        activeStatus = true;
        isDeleted = false;
        isLoggedOut = false;
        if (tokenVersion == null) tokenVersion = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ================= SPRING SECURITY =================
    //            @Override
    //            public Collection<? extends GrantedAuthority> getAuthorities() {
    //                Set<GrantedAuthority> authorities = new HashSet<>();
    //
    //                if (roles != null) {
    //                    for (Role role : roles) {
    //                        authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
    //                        if (role.getPermissions() != null) {
    //                            role.getPermissions().forEach(p ->
    //                                    authorities.add(new SimpleGrantedAuthority(p.getPermissionName()))
    //                            );
    //                        }
    //                    }
    //                }
    //
    //                if (userCompanies != null) {
    //                    for (UserCompany uc : userCompanies) {
    //                        Role role = uc.getRole();
    //                        if (role != null) {
    //                            authorities.add(new SimpleGrantedAuthority(role.getRoleName()));
    //                            if (role.getPermissions() != null) {
    //                                role.getPermissions().forEach(p ->
    //                                        authorities.add(new SimpleGrantedAuthority(p.getPermissionName()))
    //                                );
    //                            }
    //                        }
    //                    }
    //                }
    //
    //                return authorities;
    //            }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        //  GLOBAL ROLES (SUPERADMIN)
        if (roles != null) {
            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(p ->
                            authorities.add(new SimpleGrantedAuthority(p.getPermissionName()))
                    );
                }
            }
        }

        //  COMPANY ROLES
        if (userCompanies != null) {
            for (UserCompany uc : userCompanies) {
                Role role = uc.getRole();

                if (role != null) {
                    authorities.add(new SimpleGrantedAuthority(role.getRoleName()));

                    if (role.getPermissions() != null) {
                        role.getPermissions().forEach(p ->
                                authorities.add(new SimpleGrantedAuthority(p.getPermissionName()))
                        );
                    }
                }
            }
        }

        return authorities;
    }

    public boolean isSuperAdmin() {
        if (userCompanies == null) return false;

        return userCompanies.stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        uc.getRole().getRoleName() != null &&
                        uc.getRole().getRoleName().toUpperCase().contains("ROLE_SUPERADMIN"));
    }

    public boolean hasPermission(String permissionName, Integer companyId) {
        if (userCompanies == null) return false;

        return userCompanies.stream()
                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
                .filter(uc -> uc.getCompany() != null &&
                        uc.getCompany().getCompanyId().equals(companyId))
                .map(UserCompany::getRole)
                .filter(role -> role != null && Boolean.TRUE.equals(role.getActive()))
                .flatMap(role -> role.getPermissions() == null
                        ? Stream.empty()
                        : role.getPermissions().stream())
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permissionName));
    }

    public boolean hasGlobalPermission(String permissionName) {

        if (userCompanies == null) return false;

        return userCompanies.stream()
                .map(UserCompany::getRole)
                .filter(role -> role != null && Boolean.TRUE.equals(role.getActive()))
                .flatMap(role -> role.getPermissions() == null
                        ? Stream.empty()
                        : role.getPermissions().stream())
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permissionName));
    }

    public boolean hasRole(String roleName, Integer companyId) {

        if (userCompanies == null) return false;

        return userCompanies.stream()
                .filter(uc -> uc.getCompany() != null &&
                        uc.getCompany().getCompanyId().equals(companyId))
                .anyMatch(uc -> uc.getRole() != null &&
                        uc.getRole().getRoleName().equalsIgnoreCase(roleName));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !Boolean.TRUE.equals(isDeleted);
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(activeStatus) && !Boolean.TRUE.equals(isDeleted);
    }

    public String getUserName() {
        return userName;
    }
}