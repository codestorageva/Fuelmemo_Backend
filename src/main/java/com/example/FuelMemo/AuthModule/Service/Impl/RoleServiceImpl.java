package com.example.FuelMemo.AuthModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Dto.Request.PermissionDto;
import com.example.FuelMemo.AuthModule.Dto.Request.RoleRequestDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto.UpdateRoleRequestDto;
import com.example.FuelMemo.AuthModule.Dto.Response.PermissionsBasedOnRoleDTO;
import com.example.FuelMemo.AuthModule.Dto.Response.RoleResponseDto;
import com.example.FuelMemo.AuthModule.Entity.Permission;
import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.AuthModule.Repository.PermissionRepository;
import com.example.FuelMemo.AuthModule.Repository.RoleRepository;
import com.example.FuelMemo.AuthModule.Repository.UserCompanyRepository;
import com.example.FuelMemo.AuthModule.Service.RoleService;
import com.example.FuelMemo.Security.jwt.JwtService;
import com.example.FuelMemo.Shared.Exception.DuplicateEntryException;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.ListResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {



    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PermissionRepository permissionRepository;
    private final EntityManager entityManager;
    private final DataUtil dataUtil;

    public RoleServiceImpl(RoleRepository roleRepository, JwtService jwtService, PermissionRepository permissionRepository, EntityManager entityManager, DataUtil dataUtil, UserCompanyRepository userCompanyRepository) {
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.permissionRepository = permissionRepository;
        this.entityManager = entityManager;
        this.dataUtil = dataUtil;
        this.userCompanyRepository = userCompanyRepository;
    }

    private final UserCompanyRepository userCompanyRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "roleName",
            "createdAt",
            "updatedAt"
    );


private static final Set<String> PROTECTED_ROLES = Set.of(
        "ROLE_SUPERADMIN"

);

    private void validateProtectedRole(Role role, String action) {
        if (PROTECTED_ROLES.contains(role.getRoleName().toUpperCase())
                || Boolean.TRUE.equals(role.getSystemDefined())) {
            throw new IllegalStateException(
                    role.getRoleName() + " role cannot be " + action + "."
            );
        }
    }

    private Set<Permission> buildPermissions(
            Boolean allPermissions,
            Map<String, List<Integer>> modulePermissions,
            List<Integer> permissionIds,
            List<PermissionDto> permissionDtos
    ) {

        Set<Permission> permissions = new HashSet<>();

        // Global all
        if (Boolean.TRUE.equals(allPermissions)) {
            return new HashSet<>(permissionRepository.findAll());
        }

        // Module level
        if (modulePermissions != null) {
            modulePermissions.forEach((group, ids) -> {

                if (ids == null || ids.isEmpty()) return;

                List<Permission> fetched;

                if (ids.contains(-1)) {
                    fetched = permissionRepository
                            .findByPermissionGroupIgnoreCase(group);
                } else {
                    fetched = permissionRepository.findAllById(ids);
                }

                permissions.addAll(fetched);
            });
        }

        // Flat IDs
        if (permissionIds != null && !permissionIds.isEmpty()) {
            permissions.addAll(permissionRepository.findAllById(permissionIds));
        }

        // Nested DTO
        if (permissionDtos != null && !permissionDtos.isEmpty()) {
            List<Integer> ids = permissionDtos.stream()
                    .map(PermissionDto::getPermissionId)
                    .toList();

            permissions.addAll(permissionRepository.findAllById(ids));
        }

        if (permissions.isEmpty()) {
            throw new IllegalStateException("At least one permission must be assigned.");
        }

        return permissions;
    }
    @Override
    @Transactional
    public MessageResponse createRole(RoleRequestDto dto, Authentication authentication) {

        // 🔐 AUTH VALIDATION
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized access");
        }

        // 🧾 INPUT VALIDATION
        if (dto == null || dto.getRoleName() == null || dto.getRoleName().isBlank()) {
            throw new IllegalArgumentException("Role name is required");
        }

        // 🔁 NORMALIZE ROLE NAME
        String roleName = "ROLE_" + dto.getRoleName().trim().toUpperCase();

        // 🚫 SYSTEM ROLES BLOCK
//        if (roleName.equals("ROLE_SUPERADMIN") || roleName.equals("ROLE_ADMIN")) {
//            throw new IllegalArgumentException("This role is system-defined and cannot be created");
//        }

        // 🔍 CHECK SUPERADMIN
        boolean isSuperAdmin = user.isSuperAdmin();

        // 🏢 DETERMINE COMPANY
        Integer companyId;

        if (isSuperAdmin) {
            if (dto.getCompanyId() == null) {
                throw new IllegalArgumentException("CompanyId is required for superadmin");
            }
            companyId = dto.getCompanyId();
        } else {
            companyId = user.getUserCompanies().stream()
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
                    .map(uc -> uc.getCompany().getCompanyId())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active company found"));
        }

        // 🔑 PERMISSION CHECK
        if (!isSuperAdmin && !user.hasPermission("CREATE_ROLE", companyId)) {
            throw new AccessDeniedException("No permission to create role for this company");
        }

        // 🚫 DUPLICATE CHECK (PER COMPANY)
        boolean exists = roleRepository
                .existsByRoleNameIgnoreCaseAndCompanyCompanyId(roleName, companyId);

        if (exists) {
            throw new DuplicateEntryException("Role '" + roleName + "' already exists for this company");
        }

        // 🏢 FETCH COMPANY (REFERENCE)
        Company companyRef = Company.builder()
                .companyId(companyId)
                .build();

        // 🔐 BUILD PERMISSIONS
        Set<Permission> permissions = new HashSet<>(buildPermissions(
                dto.getAllPermissions(),
                dto.getModulePermissions(),
                dto.getPermissionIds(),
                dto.getPermissions()
        ));

        // 🏗️ BUILD ROLE
        Role role = Role.builder()
                .roleName(roleName)
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .isDeleted(false)
                .systemDefined(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .company(companyRef)
                .permissions(permissions)
                .build();

        // 💾 SAVE
        roleRepository.save(role);

        return new MessageResponse(
                true,
                HttpStatus.CREATED,
                "Role created successfully"
        );
    }
       @Override
    @Transactional
    public MessageResponse updateRole(Integer roleId, UpdateRoleRequestDto dto, Authentication authentication) {

        // ---------------- 🔐 AUTH VALIDATION ----------------
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized access");
        }

        if (roleId == null) throw new IllegalArgumentException("Role ID is required.");
        if (dto == null) throw new IllegalArgumentException("Update request cannot be null.");
        if (dto.getCompanyId() == null) throw new IllegalArgumentException("Company ID is required.");

        Integer companyId = dto.getCompanyId();

        // ---------------- 🏢 ROLE & PERMISSION CHECK ----------------
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        if (role.getCompany() == null || !role.getCompany().getCompanyId().equals(companyId)) {
            throw new AccessDeniedException("Unauthorized: You cannot update this role.");
        }

        // Check if user is SUPERADMIN
        boolean isSuperAdmin = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

        // Permission check: SUPERADMIN or user with UPDATE_ROLE permission
        if (!isSuperAdmin) {

            // Debug
            System.out.println("Current user companies: " +
                    user.getUserCompanies().stream()
                            .map(uc -> "companyId=" + (uc.getCompany() != null ? uc.getCompany().getCompanyId() : "null") +
                                    ", roleId=" + (uc.getRole() != null ? uc.getRole().getRoleId() : "null"))
                            .collect(Collectors.joining(", ")));
            System.out.println("Checking roleId: " + roleId + ", companyId: " + companyId);

            // પોતાનો role update ન કરી શકે
            boolean isOwnRole = user.getUserCompanies().stream()
                    .anyMatch(uc -> uc.getCompany() != null &&
                            uc.getCompany().getCompanyId().equals(companyId) &&
                            uc.getRole() != null &&
                            uc.getRole().getRoleId().equals(roleId));

            System.out.println("isOwnRole: " + isOwnRole);

            if (isOwnRole) {
                throw new AccessDeniedException("You cannot update your own role.");
            }

            // User ની permissions count
            // Current user permissions
            Set<String> currentUserPermissions = user.getUserCompanies().stream()
                    .filter(uc -> uc.getCompany() != null &&
                            uc.getCompany().getCompanyId().equals(companyId))
                    .map(UserCompany::getRole)
                    .filter(Objects::nonNull)
                    .flatMap(r -> r.getPermissions().stream())
                    .map(Permission::getPermissionName)
                    .collect(Collectors.toSet());

            // Target role ની permissions count
            Set<String> targetRolePermissions = role.getPermissions().stream()
                    .map(Permission::getPermissionName)
                    .collect(Collectors.toSet());

            System.out.println("Current User Permissions: " + currentUserPermissions);
            System.out.println("Target Role Permissions: " + targetRolePermissions);

// User can only manage roles having permissions subset of their own
            boolean canManageRole =
                    currentUserPermissions.containsAll(targetRolePermissions);

            if (!canManageRole) {
                throw new AccessDeniedException(
                        "You cannot update a role with permissions higher than yours.");
            }
        }

        // ---------------- 🔒 PROTECTED ROLES ----------------
        if ("ROLE_SUPERADMIN".equalsIgnoreCase(role.getRoleName()) || Boolean.TRUE.equals(role.getSystemDefined())) {
            throw new IllegalStateException("This role cannot be updated.");
        }

        // ---------------- 🔁 UPDATE ROLE NAME ----------------
        if (dto.getRoleName() != null && !dto.getRoleName().isBlank()) {
            String newRoleName = "ROLE_" + dto.getRoleName().trim().toUpperCase();

            if (!role.getRoleName().equalsIgnoreCase(newRoleName) &&
                    roleRepository.existsByRoleNameIgnoreCaseAndCompanyCompanyId(newRoleName, companyId)) {
                throw new DuplicateEntryException("Role name already exists in this company.");
            }

            role.setRoleName(newRoleName);
        }

        // ---------------- 📝 UPDATE DESCRIPTION & STATUS ----------------
        Optional.ofNullable(dto.getDescription()).ifPresent(role::setDescription);
        Optional.ofNullable(dto.getActive()).ifPresent(role::setActive);

        role.setUpdatedAt(LocalDateTime.now());

        // ---------------- 🔐 UPDATE PERMISSIONS ----------------
        Set<Permission> permissions = buildPermissions(
                dto.getAllPermissions(),
                dto.getModulePermissions(),
                dto.getPermissionIds(),
                dto.getPermissions()
        );

        if (permissions != null && !permissions.isEmpty()) {
            role.setPermissions(new HashSet<>(permissions));
        }

        // ---------------- 💾 SAVE ----------------
        roleRepository.save(role);

        return new MessageResponse(true, HttpStatus.OK, "Role updated successfully.");
    }
    @Override
    @Transactional
    public MessageResponse deleteRole(Integer roleId, Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Invalid authenticated user.");
        }

        User user = (User) authentication.getPrincipal();

        // ✅ Get company from token / active company
        Integer companyId = user.getUserCompanies().stream()
                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
                .map(uc -> uc.getCompany().getCompanyId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active company found"));

        // ✅ Permission check
        if (!user.hasPermission("DELETE_ROLE", companyId) && !user.isSuperAdmin()) {
            throw new AccessDeniedException("You do not have permission to delete roles in this company");
        }

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        // ✅ Company ownership check
        if (!user.isSuperAdmin() && !role.getCompany().getCompanyId().equals(companyId)) {
            throw new AccessDeniedException("Cannot delete role from another company");
        }

        // ✅ Protected roles check
        validateProtectedRole(role, "deleted");

        // ✅ Already deleted check
        if (Boolean.TRUE.equals(role.getIsDeleted())) {
            return MessageResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.BAD_REQUEST)
                    .message("Role already deleted")
                    .build();
        }

        // ✅ Soft delete
        role.setIsDeleted(true);
        role.setDeletedAt(LocalDateTime.now());
        role.setActive(false);
        role.setUpdatedAt(LocalDateTime.now());

        roleRepository.save(role);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Role deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public MessageResponse restoreRole(Integer roleId, Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Invalid authenticated user.");
        }

        User user = (User) authentication.getPrincipal();

        // ---------------- Fetch role ----------------
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        // ---------------- Super Admin check ----------------
        boolean isSuperAdmin = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

        // ---------------- Company validation ----------------
        Integer companyId = role.getCompany() != null ? role.getCompany().getCompanyId() : null;
        if (!isSuperAdmin && (companyId == null || !user.getUserCompanies().stream()
                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
                .map(uc -> uc.getCompany().getCompanyId())
                .toList()
                .contains(companyId))) {
            throw new AccessDeniedException("Unauthorized: You cannot restore this role.");
        }

        // ---------------- Permission check ----------------
        if (!user.hasPermission("RESTORE_ROLE", companyId) && !isSuperAdmin) {
            throw new AccessDeniedException("You do not have permission to restore this role.");
        }

        // ---------------- Protected role check ----------------
        validateProtectedRole(role, "restored");

        // ---------------- Already active check ----------------
        if (Boolean.FALSE.equals(role.getIsDeleted())) {
            throw new IllegalStateException("Role is not deleted.");
        }

        // ---------------- Restore ----------------
        role.setIsDeleted(false);
        role.setDeletedAt(null);
        role.setActive(true);
        role.setUpdatedAt(LocalDateTime.now());

        roleRepository.save(role);

        return new MessageResponse(true, HttpStatus.OK, "Role restored successfully.");
    }
    @Override
    public HttpResponse getAllRoles(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new RuntimeException("Invalid authenticated user.");
        }

        User user = (User) authentication.getPrincipal();

        // ---------------- Super Admin check ----------------
        boolean isSuperAdmin = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

        // ---------------- Company access ----------------
        List<Integer> accessibleCompanyIds = null;
        if (!isSuperAdmin) {
            accessibleCompanyIds = user.getUserCompanies().stream()
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
                    .map(uc -> uc.getCompany().getCompanyId())
                    .toList();

            // Permission check (company-aware)
            boolean hasViewPermission = accessibleCompanyIds.stream()
                    .anyMatch(c -> user.hasPermission("VIEW_ROLE", c));
            if (!hasViewPermission) {
                throw new AccessDeniedException("No permission to view roles in accessible companies");
            }
        }

        // ---------------- Sorting ----------------
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) sortBy = "createdAt";
        if (!"asc".equalsIgnoreCase(sortDirection) && !"desc".equalsIgnoreCase(sortDirection)) {
            sortDirection = "asc";
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ---------------- MAIN QUERY ----------------
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> root = cq.from(Role.class);

        cq.where(buildRolePredicates(cb, root, keyword, isDeleted, isSuperAdmin, accessibleCompanyIds)
                .toArray(new Predicate[0]));

        Order order = "desc".equalsIgnoreCase(sortDirection)
                ? cb.desc(root.get(sortBy))
                : cb.asc(root.get(sortBy));

        cq.orderBy(order);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Role> roles = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ---------------- COUNT QUERY ----------------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Role> countRoot = countQuery.from(Role.class);
        countQuery.select(cb.count(countRoot))
                .where(buildRolePredicates(cb, countRoot, keyword, isDeleted, isSuperAdmin, accessibleCompanyIds)
                        .toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLastPage = pageNumber + 1 >= totalPages;

        List<RoleResponseDto> dtoList = roles.stream()
                .map(DataUtil::mapToRoleResponse)
                .toList();

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLastPage(isLastPage)
                .data(dtoList)
                .build();
    }
//    private List<Predicate> buildRolePredicates(
//            CriteriaBuilder cb,
//            Root<Role> root,
//            String keyword,
//            Boolean isDeleted,
//            boolean isSuperAdmin,
//            List<Integer> accessibleCompanyIds
//    ) {
//        List<Predicate> predicates = new ArrayList<>();
//
//        // Company filter for non-superadmin
//        if (!isSuperAdmin && accessibleCompanyIds != null && !accessibleCompanyIds.isEmpty()) {
//            predicates.add(cb.and(
//                    cb.isNotNull(root.get("company")),
//                    root.get("company").get("companyId").in(accessibleCompanyIds)
//            ));
//        }
//
//        // isDeleted filter
//        if (isDeleted != null) {
//            predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
//        }
//
//        // Keyword search (roleName and description)
//        if (keyword != null && !keyword.isBlank()) {
//            String pattern = "%" + keyword.trim().toLowerCase() + "%";
//            predicates.add(cb.or(
//                    cb.like(cb.lower(root.get("roleName")), pattern),
//                    cb.like(cb.lower(root.get("description")), pattern)
//            ));
//        }
//
//        // Hide protected roles
//        predicates.add(cb.not(root.get("roleName").in("ROLE_SUPERADMIN")));
//
//        // Optional: hide other system-defined roles
////        predicates.add(cb.equal(root.get("systemDefined"), false));
//
//        return predicates;
//    }
private List<Predicate> buildRolePredicates(
        CriteriaBuilder cb,
        Root<Role> root,
        String keyword,
        Boolean isDeleted,
        boolean isSuperAdmin,
        List<Integer> accessibleCompanyIds
) {
    List<Predicate> predicates = new ArrayList<>();

    // Company filter for non-SuperAdmin
    if (!isSuperAdmin) {
        if (accessibleCompanyIds != null && !accessibleCompanyIds.isEmpty()) {
            predicates.add(cb.and(
                    cb.isNotNull(root.get("company")),
                    root.get("company").get("companyId").in(accessibleCompanyIds)
            ));
        } else {
            // No accessible companies → always false predicate
            predicates.add(cb.disjunction());
        }
    }

    // isDeleted filter
    if (isDeleted != null) {
        predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
    }

    // Keyword search (roleName and description)
    if (keyword != null && !keyword.isBlank()) {
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        predicates.add(cb.or(
                cb.like(cb.lower(root.get("roleName")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
        ));
    }

    // Hide protected roles
    predicates.add(cb.not(root.get("roleName").in("ROLE_SUPERADMIN")));

    // Hide other system-defined roles (optional)
    predicates.add(cb.equal(root.get("systemDefined"), false));

    return predicates;
}
    @Override
    public HttpResponse getRolesByCompany(Integer companyId, Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Invalid authenticated user.");
        }

        User user = (User) authentication.getPrincipal();

        boolean isSuperAdmin = user.getUserCompanies()
                .stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

        List<Integer> accessibleCompanyIds = user.getUserCompanies().stream()
                .filter(uc -> Boolean.TRUE.equals(uc.getActive()) && uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .toList();

        if (!isSuperAdmin && !accessibleCompanyIds.contains(companyId)) {
            throw new AccessDeniedException("Unauthorized access to this company");
        }

        if (!isSuperAdmin && !user.hasPermission("VIEW_ROLE", companyId)) {
            throw new AccessDeniedException("No permission to view roles in this company");
        }

        List<Role> roles = roleRepository
                .findByCompanyCompanyIdAndIsDeletedFalseAndActiveTrue(companyId)
                .stream()
                .filter(r -> !"ROLE_SUPERADMIN".equalsIgnoreCase(r.getRoleName()) && Boolean.FALSE.equals(r.getSystemDefined()))
                .toList();

        List<RoleResponseDto> dtoList = roles.stream()
                .map(DataUtil::mapToRoleResponse)
                .toList();

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dtoList)
                .build();
    }
    @Override
    public ListResponse getAllRoleByNonSoftDeletedAndCurrentUserRole(User loggedInUser) {

        if (loggedInUser == null) {
            throw new AccessDeniedException("Logged in user not found");
        }

        boolean isSuperAdmin = loggedInUser.getUserCompanies()
                .stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Role> cq = cb.createQuery(Role.class);
        Root<Role> root = cq.from(Role.class);

        Predicate notDeleted = cb.isFalse(root.get("isDeleted"));

        if (isSuperAdmin) {
            cq.select(root)
                    .where(notDeleted)
                    .orderBy(cb.asc(root.get("roleName")));
        } else {
            List<Integer> accessibleRoleIds = loggedInUser.getUserCompanies().stream()
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive()) && uc.getRole() != null)
                    .map(uc -> uc.getRole().getRoleId())
                    .toList();
            if (accessibleRoleIds.isEmpty()) accessibleRoleIds = List.of(-1); // prevent empty IN

            List<Integer> accessibleCompanyIds = loggedInUser.getUserCompanies().stream()
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive()) && uc.getCompany() != null)
                    .map(uc -> uc.getCompany().getCompanyId())
                    .toList();
            if (accessibleCompanyIds.isEmpty()) accessibleCompanyIds = List.of(-1);

            Predicate rolePredicate = root.get("roleId").in(accessibleRoleIds);
            Predicate companyPredicate = root.get("company").get("companyId").in(accessibleCompanyIds);
            Predicate protectedRoles = cb.not(root.get("roleName").in("ROLE_SUPERADMIN"));
            Predicate systemRoles = cb.equal(root.get("systemDefined"), false);

            cq.select(root)
                    .where(cb.and(notDeleted, rolePredicate, companyPredicate, protectedRoles, systemRoles))
                    .orderBy(cb.asc(root.get("roleName")));
        }

        List<Role> resultList = entityManager.createQuery(cq).getResultList();
        List<PermissionsBasedOnRoleDTO> roles = dataUtil.rolesPermissionToDtos(resultList);

        return new ListResponse(true, HttpStatus.OK, roles);
    }
    @Override
    public HttpResponse getRoleById(Integer id, Authentication authentication) {

        // 🔐 AUTH VALIDATION
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Invalid authenticated user.");
        }

        User user = (User) authentication.getPrincipal();

        // 🔍 FETCH ROLE
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // 🏢 COMPANY ID
        Integer companyId = Optional.ofNullable(role.getCompany())
                .map(Company::getCompanyId)
                .orElse(null);

        // 👑 SUPER ADMIN CHECK
        boolean isSuperAdmin = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getRole() != null &&
                        "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

        // 🏢 ACCESSIBLE COMPANIES
        List<Integer> accessibleCompanyIds = user.getUserCompanies().stream()
                .filter(uc -> Boolean.TRUE.equals(uc.getActive()) && uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .toList();

        // 🚫 COMPANY ACCESS CHECK
        if (!isSuperAdmin && (companyId == null || !accessibleCompanyIds.contains(companyId))) {
            throw new AccessDeniedException("Unauthorized: You cannot access this role.");
        }

        // 🔑 PERMISSION CHECK
        if (!isSuperAdmin && !user.hasPermission("VIEW_ROLE", companyId)) {
            throw new AccessDeniedException("No permission to view role in this company");
        }

        // 🔒 PROTECTED ROLES
        String roleName = role.getRoleName().toUpperCase();

// ❌ Only SUPERADMIN role protected
        if (!isSuperAdmin && roleName.equals("ROLE_SUPERADMIN")) {
            throw new AccessDeniedException("Cannot access SUPERADMIN role");
        }

// ❌ System roles (optional - tame allow pan kari shako)
        if (!isSuperAdmin && Boolean.TRUE.equals(role.getSystemDefined())) {
            throw new AccessDeniedException("Cannot access system role");
        }

        // 🗑️ DELETED CHECK
        if (Boolean.TRUE.equals(role.getIsDeleted())) {
            throw new AccessDeniedException("Role is deleted.");
        }

        // 📦 DTO
        RoleResponseDto dto = DataUtil.mapToRoleResponse(role);

        // ✅ RESPONSE
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dto)
                .build();
    }

        private void validatePermission (User user, String permission, Integer companyId){

            // ---------------- 🔐 BASIC VALIDATION ----------------
            if (user == null) {
                throw new AccessDeniedException("Invalid user");
            }

            if (companyId == null) {
                throw new AccessDeniedException("Company ID is required");
            }

            if (user.getUserCompanies() == null || user.getUserCompanies().isEmpty()) {
                throw new AccessDeniedException("User is not assigned to any company");
            }

            // ---------------- 👑 SUPER ADMIN BYPASS ----------------
            boolean isSuperAdmin = user.getUserCompanies().stream()
                    .anyMatch(uc -> uc.getRole() != null &&
                            "ROLE_SUPERADMIN".equalsIgnoreCase(uc.getRole().getRoleName()));

            if (isSuperAdmin) return;

            // ---------------- 🔑 PERMISSION CHECK ----------------
            boolean hasPermission = user.getUserCompanies().stream()

                    // Only active mappings
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive()))

                    // Company match
                    .filter(uc -> uc.getCompany() != null &&
                            companyId.equals(uc.getCompany().getCompanyId()))

                    // Role active
                    .map(UserCompany::getRole)
                    .filter(role -> role != null && Boolean.TRUE.equals(role.getActive()))

                    // Permissions
                    .flatMap(role -> role.getPermissions() == null
                            ? Stream.empty()
                            : role.getPermissions().stream())

                    // Permission active & not deleted
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))

                    // Match permission
                    .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permission));

            if (!hasPermission) {
                throw new AccessDeniedException(
                        "No permission [" + permission + "] for companyId: " + companyId
                );
            }
        }
        private Integer validateAndGetCompany (User user, Integer companyId, String permission){

            // ---------------- 🔐 BASIC VALIDATION ----------------
            if (user == null) {
                throw new AccessDeniedException("Invalid user");
            }

            if (companyId == null) {
                throw new IllegalArgumentException("CompanyId is required");
            }

            if (user.getUserCompanies() == null || user.getUserCompanies().isEmpty()) {
                throw new AccessDeniedException("User is not assigned to any company");
            }

            // ---------------- 👑 SUPER ADMIN BYPASS ----------------
            if (user.isSuperAdmin()) {
                return companyId;
            }

            // ---------------- 🏢 COMPANY ACCESS CHECK ----------------
            boolean belongsToCompany = user.getUserCompanies().stream()
                    .filter(uc -> Boolean.TRUE.equals(uc.getActive())) // ✅ only active mapping
                    .anyMatch(uc ->
                            uc.getCompany() != null &&
                                    companyId.equals(uc.getCompany().getCompanyId())
                    );

            if (!belongsToCompany) {
                throw new AccessDeniedException("Access denied for this company");
            }

            // ---------------- 🔑 PERMISSION CHECK ----------------
            if (permission != null && !permission.isBlank()) {
                validatePermission(user, permission, companyId);
            }

            return companyId;
        }
}

