package com.example.FuelMemo.Security;

import com.example.FuelMemo.AuthModule.Entity.Permission;
import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.AuthModule.Repository.PermissionRepository;
import com.example.FuelMemo.AuthModule.Repository.RoleRepository;
import com.example.FuelMemo.AuthModule.Repository.UserCompanyRepository;
import com.example.FuelMemo.AuthModule.Repository.UserRepository;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Transactional
public class InitializationConfig implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    public InitializationConfig(PermissionRepository permissionRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, UserCompanyRepository userCompanyRepository, CompanyRepository companyRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCompanyRepository = userCompanyRepository;
        this.companyRepository = companyRepository;
    }

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCompanyRepository userCompanyRepository;
    private final CompanyRepository companyRepository;



    @Override
    @Transactional
    public void run(String... args) {

        createAllPermissions();

        Company defaultCompany = createDefaultCompany();

        createSuperAdminRole();
        assignPermissionsToSuperAdmin();

        if (defaultCompany != null) {
            createSuperAdminUser(defaultCompany);

            System.out.println("System Fully Initialized");
        }
    }

    // ======================================================
    // CREATE ALL PERMISSIONS
    // ======================================================
    private void createAllPermissions() {

        List<String> permissions = List.of(
                "WRITE_VILLAGE","READ_VILLAGE","UPDATE_VILLAGE","DELETE_VILLAGE","RESTORE_VILLAGE","EXPORT_VILLAGE","IMPORT_VILLAGE",
                "WRITE_SUB_DISTRICT","READ_SUB_DISTRICT","UPDATE_SUB_DISTRICT","DELETE_SUB_DISTRICT","RESTORE_SUB_DISTRICT","EXPORT_SUB_DISTRICT","IMPORT_SUB_DISTRICT",
                "WRITE_DISTRICT","READ_DISTRICT","UPDATE_DISTRICT","DELETE_DISTRICT","RESTORE_DISTRICT","EXPORT_DISTRICT","IMPORT_DISTRICT",
                "WRITE_STATE","READ_STATE","UPDATE_STATE","DELETE_STATE","RESTORE_STATE","EXPORT_STATE","IMPORT_STATE",
                "WRITE_COUNTRY","READ_COUNTRY","UPDATE_COUNTRY","DELETE_COUNTRY","RESTORE_COUNTRY","EXPORT_COUNTRY","IMPORT_COUNTRY",

                // OPERATIONS
                "CREATE_COMPANY","UPDATE_COMPANY","DELETE_COMPANY","VIEW_COMPANY","RESTORE_COMPANY",
                "CREATE_VEHICLE","UPDATE_VEHICLE","DELETE_VEHICLE","VIEW_VEHICLE","RESTORE_VEHICLE",
                "CREATE_DRIVER","UPDATE_DRIVER","DELETE_DRIVER","VIEW_DRIVER","RESTORE_DRIVER",
                "CREATE_SITE","UPDATE_SITE","DELETE_SITE","VIEW_SITE","RESTORE_SITE",
                "CREATE_PARTY","UPDATE_PARTY","DELETE_PARTY","VIEW_PARTY","RESTORE_PARTY",
                "CREATE_FUEL_STATION","UPDATE_FUEL_STATION","DELETE_FUEL_STATION","VIEW_FUEL_STATION","RESTORE_FUEL_STATION",
                "CREATE_ROYALTY_PASS","UPDATE_ROYALTY_PASS","DELETE_ROYALTY_PASS","VIEW_ROYALTY_PASS","RESTORE_ROYALTY_PASS",

                //FUEL_MEMO
                "CREATE_FUEL_MEMO","UPDATE_FUEL_MEMO","DELETE_FUEL_MEMO","RESTORE_FUEL_MEMO","ADJUSTMENT_CREATE_FUEL_MEMO",
                "CLOSE_FUEL_MEMO","REOPEN_FUEL_MEMO","VIEW_FUEL_MEMO","EXPORT_FUEL_MEMO",

                // REPORT
                "VIEW_ALL_REPORTS","VIEW_COMPANY_REPORT","VIEW_SITE_REPORT",
                "VIEW_DRIVER_REPORT","VIEW_MINERAL_REPORT","VIEW_FUEL_MEMO_REPORT",

                // AUTH
                "CREATE_USER","UPDATE_USER","DELETE_USER","VIEW_USER","RESET_PASSWORD","RESTORE_USER",
                "ASSIGN_ROLE","ASSIGN_PERMISSION","MANAGE_ROLE_PERMISSION","VIEW_GROUP_PERMISSION",
                "CREATE_ROLE","UPDATE_ROLE","DELETE_ROLE","VIEW_ROLE","RESTORE_ROLE"
        );

        for (String permName : permissions) {
            if (!permissionRepository.existsByPermissionName(permName)) {

                String[] parts = permName.split("_", 2);
                String action = parts[0];
                String group = parts[1];

                Permission permission = Permission.builder()
                        .permissionName(permName)
                        .actionName(action)
                        .permissionGroup(group)
                        .permissionSuperGroup(resolveSuperGroup(group))
                        .active(true)
                        .isDeleted(false)
                        .build();

                permissionRepository.save(permission);
            }
        }
    }

    // ======================================================
    // DETERMINE SUPER GROUP
    // ======================================================
    private String resolveSuperGroup(String group) {

        if (group.contains("VILLAGE") || group.contains("DISTRICT") || group.contains("STATE") || group.contains("COUNTRY")) {
            return "MASTER_MANAGEMENT";
        }
        if (group.contains("USER") || group.contains("ROLE")) {
            return "AUTH_MANAGEMENT";
        }
        if (group.contains("REPORT")) {
            return "REPORT_MANAGEMENT";
        }
        return "OPERATIONS_MANAGEMENT";
    }

    // ======================================================
    // CREATE DEFAULT ROLES
    // ======================================================
    private void createSuperAdminRole() {

        Optional<Role> existing = roleRepository
                .findByRoleNameIgnoreCaseAndCompanyIsNull("ROLE_SUPERADMIN");

        if (existing.isPresent()) {

            Role role = existing.get();

            if (Boolean.TRUE.equals(role.getIsDeleted())) {
                role.setIsDeleted(false);
                role.setActive(true);
                roleRepository.save(role);
            }

        } else {

            Role role = Role.builder()
                    .roleName("ROLE_SUPERADMIN")
                    .description("System Super Administrator")
                    .active(true)
                    .systemDefined(true)
                    .isDeleted(false)
                    .permissions(new HashSet<>())
                    .company(null)
                    .build();

            roleRepository.save(role);
        }
    }



    // ======================================================
    // ASSIGN PERMISSIONS TO ROLES
    // ======================================================
    private void assignPermissionsToSuperAdmin() {

        Role superAdmin = roleRepository
                .findByRoleNameIgnoreCaseAndCompanyIsNull("ROLE_SUPERADMIN")
                .orElseThrow(() -> new RuntimeException("SUPERADMIN role not found!"));

        List<Permission> allPermissions = permissionRepository.findAll();

        superAdmin.setPermissions(new HashSet<>(allPermissions));
        roleRepository.save(superAdmin);
    }
    // ======================================================
    // CREATE DEFAULT USERS
    // ======================================================
    private void createSuperAdminUser(Company defaultCompany) {

        User existingUser = userRepository.findByUserName("superadmin");

        if (existingUser != null) {
            System.out.println("SUPERADMIN already exists, skipping...");
            return;
        }

        Role superAdminRole = roleRepository
                .findByRoleNameIgnoreCaseAndCompanyIsNull("ROLE_SUPERADMIN")
                .orElseThrow(() -> new RuntimeException("SUPERADMIN role not found!"));

        User superAdminUser = User.builder()
                .userName("superadmin")
                .email("superadmin@gmail.com")
                .password(passwordEncoder.encode("SuperAdmin@123"))
                .pwd("SuperAdmin@123")
                .firstName("Super")
                .lastName("Admin")
                .mobNo("9999999999")
                .activeStatus(true)
                .isDeleted(false)
                .isLoggedOut(false)
                .build();

        // ✅ ROLE assign
        superAdminUser.getRoles().add(superAdminRole);
        superAdminUser = userRepository.save(superAdminUser);

        // ✅ 🔥 IMPORTANT FIX: UserCompany assign
        UserCompany uc = UserCompany.builder()
                .user(superAdminUser)
                .company(defaultCompany)
                .role(superAdminRole)
                .active(true)
                .build();

        userCompanyRepository.save(uc);

        System.out.println("SUPERADMIN created WITH company mapping ✅");
    }
    private void validateCompanyRequired(User user, Company company) {

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getRoleName().equalsIgnoreCase("ROLE_SUPERADMIN"));

        if (!isSuperAdmin && company == null) {
            throw new RuntimeException("Company is required for non-superadmin users");
        }
    }
    private Company createDefaultCompany() {

        Company company = companyRepository.findByCompanyName("Default Company");

        if (company == null) {
            company = companyRepository.save(
                    Company.builder()
                            .companyName("Default Company")
                            .build()
            );
        }

        return company;
    }
}