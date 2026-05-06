package com.example.FuelMemo.AuthModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Dto.Request.UpdateDto.UserUpdateDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UserDto;
import com.example.FuelMemo.AuthModule.Dto.Response.UserResponse;
import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.AuthModule.Repository.RoleRepository;
import com.example.FuelMemo.AuthModule.Repository.UserCompanyRepository;
import com.example.FuelMemo.AuthModule.Repository.UserRepository;
import com.example.FuelMemo.AuthModule.Service.UserService;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.Shared.Exception.*;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, CompanyRepository companyRepository, UserCompanyRepository userCompanyRepository, PasswordEncoder passwordEncoder, DataUtil dataUtil, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataUtil = dataUtil;
        this.entityManager = entityManager;
    }

    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataUtil dataUtil;
    private final EntityManager entityManager;
    private static final List<String> ALLOWED_SORT_FIELDS = List.of("userId", "userName", "email", "firstName", "lastName", "mobNo");

    @Override
    @Transactional
    public MessageResponse addUser(UserDto dto, Authentication authentication) {

        // ================= 🔐 AUTH VALIDATION =================
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Unauthorized access");
        }

        User loggedInUser = (User) authentication.getPrincipal();

        // ================= 🔑 PERMISSION CHECK =================
        if (!hasPermission(loggedInUser, "CREATE_USER")) {
            throw new AccessDeniedException("You do not have permission to create users");
        }

        // ================= 📥 VALIDATE REQUEST =================
        validateRequest(dto);

        // ================= 🔁 DUPLICATE CHECK =================
        Optional<User> existingUserOpt =
                userRepository.findByUserNameIgnoreCase(dto.getUserName().trim());

        if (existingUserOpt.isPresent()) {
            throw new DuplicateEntryException("Username already exists: " + dto.getUserName());
        }

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new DuplicateEntryException("Email already exists: " + dto.getEmail());
        }

        // ================= 🎯 FETCH ROLES =================
        List<Role> roles = roleRepository.findAllById(dto.getRoleIds());

        if (roles.isEmpty()) {
            throw new ResourceNotFoundException("No valid roles found for given role IDs");
        }

        // ================= ✅ VALIDATE ROLES =================
        Set<Integer> roleCompanyIds = roles.stream()
                .peek(r -> {
                    if (r.getCompany() == null) {
                        throw new IllegalStateException("Role is not linked to any company");
                    }
                    if (Boolean.TRUE.equals(r.getIsDeleted()) || Boolean.FALSE.equals(r.getActive())) {
                        throw new IllegalStateException("Role is inactive or deleted");
                    }
//                    if (!dto.getCompanyIds().contains(r.getCompany().getCompanyId())) {
//                        throw new IllegalStateException("Role does not belong to selected company");
//                    }
                })
                .map(r -> r.getCompany().getCompanyId())
                .collect(Collectors.toSet());

        if (roleCompanyIds.size() != 1) {
            throw new IllegalStateException("All roles must belong to the same company");
        }

        Integer companyId = roleCompanyIds.iterator().next(); // (optional use)

        // =================  CREATE USER =================
        User user = User.builder()
                .userName(dto.getUserName().trim())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .pwd(dto.getPassword())
                .mobNo(dto.getMobNo())
                .dob(dto.getDob())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .addressLine3(dto.getAddressLine3())
                .activeStatus(true)
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // ================= 🔗 CREATE USER-COMPANY MAPPINGS =================
        List<UserCompany> userCompanies = roles.stream()
                .map(role -> UserCompany.builder()
                        .user(savedUser)
                        .role(role)
                        .company(role.getCompany())
                        .active(true)
                        .assignedAt(LocalDateTime.now())
                        .build())
                .toList();

        userCompanyRepository.saveAll(userCompanies);

        // =================  RESPONSE =================
        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.CREATED)
                .message("User created successfully")
                .build();
    }
    private void validateRequest(UserDto dto) {

        if (dto == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }
    @Override
    @Transactional
    public MessageResponse updateUser(Integer userId, UserUpdateDto dto, Authentication authentication) {

        // ================= 🔐 AUTH =================
        User loggedInUser = getAuthUser(authentication);

        boolean isSelfUpdate = loggedInUser.getUserId().equals(userId);
        boolean isAdminOrSuperAdmin = isAdminOrSuperAdmin(loggedInUser);

        // 🚨 FINAL ACCESS CHECK
        if (!isSelfUpdate) {
            if (!hasPermission(loggedInUser, "UPDATE_USER")) {
                throw new AccessDeniedException("No permission to update user");
            }
        } else {
            if (!isAdminOrSuperAdmin) {
                throw new AccessDeniedException("You cannot update your own profile");
            }
        }

        // ================= FETCH USER =================
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new InactiveStatusException("Cannot update deleted user");
        }

        // ================= 🔥 COMPANY VALIDATION =================
        Set<Integer> allowedCompanyIds = loggedInUser.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .collect(Collectors.toSet());

        boolean belongsToCompany = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getCompany() != null &&
                        allowedCompanyIds.contains(uc.getCompany().getCompanyId()));

        if (!belongsToCompany) {
            throw new AccessDeniedException("You cannot update this user");
        }

        // ================= 🔥 PROTECTED ROLE =================
        boolean restrictedUser = user.getUserCompanies().stream()
                .map(UserCompany::getRole)
                .filter(Objects::nonNull)
                .map(Role::getRoleName)
                .map(String::toUpperCase)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPERADMIN"));

        if (!isSelfUpdate && restrictedUser && !isAdminOrSuperAdmin(loggedInUser)) {
            throw new AccessDeniedException("You cannot update ADMIN/SUPERADMIN user");
        }

        // ================= EMAIL =================
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String email = dto.getEmail().trim().toLowerCase();

            if (!email.equalsIgnoreCase(user.getEmail()) &&
                    userRepository.existsByEmailIgnoreCase(email)) {
                throw new DuplicateEntryException("Email already exists");
            }

            user.setEmail(email);
        }

        // ================= USERNAME =================
        if (dto.getUserName() != null && !dto.getUserName().isBlank()) {
            String username = dto.getUserName().trim();

            if (!username.equalsIgnoreCase(user.getUserName()) &&
                    userRepository.existsByUserNameIgnoreCase(username)) {
                throw new DuplicateEntryException("Username already exists");
            }

            user.setUserName(username);
        }

        // ================= PASSWORD =================
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setPwd(dto.getPassword());
        }

        // ================= OTHER FIELDS =================
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getMobNo() != null) user.setMobNo(dto.getMobNo());
        if (dto.getDob() != null) user.setDob(dto.getDob());
        if (dto.getAddressLine1() != null) user.setAddressLine1(dto.getAddressLine1());
        if (dto.getAddressLine2() != null) user.setAddressLine2(dto.getAddressLine2());
        if (dto.getAddressLine3() != null) user.setAddressLine3(dto.getAddressLine3());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // ================= 🔥 ROLE UPDATE =================
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {

            if (!hasPermission(loggedInUser, "ASSIGN_ROLE")) {
                throw new AccessDeniedException("No permission to assign roles");
            }

            List<Role> roles = roleRepository.findAllById(dto.getRoleIds());

            for (Role role : roles) {
                if (role.getCompany() == null ||
                        !allowedCompanyIds.contains(role.getCompany().getCompanyId())) {
                    throw new AccessDeniedException("Invalid role for your company");
                }
            }

            userCompanyRepository.deleteByUserUserId(userId);

            List<UserCompany> newMappings = roles.stream()
                    .map(role -> UserCompany.builder()
                            .user(user)
                            .role(role)
                            .company(role.getCompany())
                            .active(true)
                            .assignedAt(LocalDateTime.now())
                            .build())
                    .toList();

            userCompanyRepository.saveAll(newMappings);
        }

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("User updated successfully")
                .build();
    }
    private boolean isAdminOrSuperAdmin(User user) {
        return user.getUserCompanies().stream()
                .map(UserCompany::getRole)
                .filter(Objects::nonNull)
                .map(Role::getRoleName)
                .map(String::toUpperCase)
                .anyMatch(role -> role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPERADMIN"));
    }
        @Override
    public DataResponse getUserById(Integer userId, Authentication authentication) {

        User loggedInUser = getAuthUser(authentication);

        if (!hasPermission(loggedInUser, "VIEW_USER")) {
            throw new AccessDeniedException("No permission to view user");
        }

        // ================= FETCH USER WITH COMPANIES =================
        User user = userRepository.findByIdWithCompanies(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new InactiveStatusException("User is deleted");
        }

        // ================= COMPANY ACCESS =================
        Set<Integer> allowedCompanyIds = loggedInUser.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .collect(Collectors.toSet());

        boolean hasAccess = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getCompany() != null &&
                        allowedCompanyIds.contains(uc.getCompany().getCompanyId()));

        if (!hasAccess && !hasPermission(loggedInUser, "VIEW_USER")) {
            throw new AccessDeniedException("You cannot access this user");
        }

        return new DataResponse(true, HttpStatus.OK, dataUtil.toUserResponse(user));
    }

    @Override
    public DataResponse getUsers(Authentication authentication, Integer companyId) {

        User loggedInUser = getAuthUser(authentication);

        if (!hasPermission(loggedInUser, "VIEW_USER")) {
            throw new AccessDeniedException("No permission to view users");
        }

        Set<Integer> allowedCompanyIds = loggedInUser.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .collect(Collectors.toSet());

        if (allowedCompanyIds.isEmpty() && !hasPermission(loggedInUser, "VIEW_USER")) {
            throw new AccessDeniedException("No company assigned");
        }

        List<User> users;

        // ================= GLOBAL ACCESS =================
        if (hasPermission(loggedInUser, "VIEW_USER")) {

            if (companyId != null) {
                users = userRepository
                        .findDistinctByUserCompaniesCompanyCompanyIdInAndIsDeletedFalse(Set.of(companyId));
            } else {
                users = userRepository.findByIsDeletedFalse(); // ✅ DB-level filter
            }

        } else {

            // ================= COMPANY-RESTRICTED =================
            if (companyId != null && !allowedCompanyIds.contains(companyId)) {
                throw new AccessDeniedException("You cannot access this company");
            }

            Set<Integer> filterCompanyIds =
                    (companyId != null) ? Set.of(companyId) : allowedCompanyIds;

            users = userRepository
                    .findDistinctByUserCompaniesCompanyCompanyIdInAndIsDeletedFalse(filterCompanyIds);
        }

        List<UserResponse> userDtos = users.stream()
                .map(dataUtil::toUserDto)
                .toList();

        return new DataResponse(true, HttpStatus.OK, userDtos);
    }
    @Override
    public DataResponse getAllDeletedUsers(Authentication authentication) {

        User loggedInUser = getAuthUser(authentication);

        // ================= 🔐 PERMISSION CHECK =================
        if (!hasPermission(loggedInUser, "READ_DELETED_USERS")) {
            throw new AccessDeniedException("No permission to view deleted users");
        }

        Set<Integer> allowedCompanyIds = loggedInUser.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .collect(Collectors.toSet());

        List<User> users;

        // ================= GLOBAL ACCESS =================
        if (hasPermission(loggedInUser, "READ_ALL_USERS")) {
            users = userRepository.findAllDeletedUsers();
        } else {

            if (allowedCompanyIds.isEmpty()) {
                throw new AccessDeniedException("No company assigned");
            }

            users = userRepository
                    .findDistinctByUserCompaniesCompanyCompanyIdInAndIsDeletedTrue(allowedCompanyIds);
        }

        return new DataResponse(
                true,
                HttpStatus.OK,
                users.stream().map(dataUtil::toUserResponse).toList()
        );
    }
    @Override
    public HttpResponse getAllUsers(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDir,
            Authentication authentication) {

        // ================== DEFAULTS ==================
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) sortBy = "userId";
        pageNumber = (pageNumber == null || pageNumber < 0) ? 0 : pageNumber;
        pageSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;
        sortDir = ("desc".equalsIgnoreCase(sortDir)) ? "desc" : "asc";

        // ================== AUTH ==================
        User loggedInUser = getAuthUser(authentication);
        boolean isSuperAdmin = loggedInUser.isSuperAdmin();

        if (!hasPermission(loggedInUser, "VIEW_USER")) {
            throw new AccessDeniedException("No permission to view users");
        }

        Set<Integer> allowedCompanyIds = loggedInUser.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .collect(Collectors.toSet());

        boolean hasGlobalAccess = isSuperAdmin;

        if (!hasGlobalAccess && allowedCompanyIds.isEmpty()) {
            return emptyPageResponse(pageNumber, pageSize);
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ================== MAIN QUERY ==================
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);
        cq.distinct(true);

        Predicate predicate = buildUserPredicates(cb, root, keyword, false);

        // 🔐 COMPANY FILTER
        if (!hasGlobalAccess) {
            Join<User, UserCompany> ucJoin = root.join("userCompanies", JoinType.LEFT);
            predicate = cb.and(predicate,
                    ucJoin.get("company").get("companyId").in(allowedCompanyIds));
        }

        // 🔥 SUPERADMIN EXCLUDE (BULLETPROOF)
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<UserCompany> subRoot = subquery.from(UserCompany.class);
        Join<UserCompany, Role> subRoleJoin = subRoot.join("role");

        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(subRoot.get("user"), root),
                cb.or(
                        cb.like(cb.upper(cb.trim(subRoleJoin.get("roleName"))), "%SUPERADMIN%"),
                        cb.like(cb.upper(cb.trim(subRoleJoin.get("roleName"))), "%SUPER_ADMIN%"),
                        cb.like(cb.upper(cb.trim(subRoleJoin.get("roleName"))), "%SUPER ADMIN%")
                )
        );

        if (!isSuperAdmin) {
            predicate = cb.and(predicate, cb.not(cb.exists(subquery)));
        }

        cq.where(predicate);

        // 🔃 SORT
        cq.orderBy("asc".equalsIgnoreCase(sortDir)
                ? cb.asc(root.get(sortBy))
                : cb.desc(root.get(sortBy)));

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<User> users = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ================== COUNT QUERY ==================
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);

        countQuery.select(cb.countDistinct(countRoot));

        Predicate countPredicate = buildUserPredicates(cb, countRoot, keyword, false);

        // 🔐 COMPANY FILTER
        if (!hasGlobalAccess) {
            Join<User, UserCompany> countUcJoin = countRoot.join("userCompanies", JoinType.LEFT);
            countPredicate = cb.and(countPredicate,
                    countUcJoin.get("company").get("companyId").in(allowedCompanyIds));
        }

        // 🔥 SUPERADMIN EXCLUDE (COUNT - BULLETPROOF)
        Subquery<Long> countSubquery = countQuery.subquery(Long.class);
        Root<UserCompany> subbRoot = countSubquery.from(UserCompany.class);
        Join<UserCompany, Role> subRolesJoin = subbRoot.join("role");

        countSubquery.select(cb.literal(1L));
        countSubquery.where(
                cb.equal(subbRoot.get("user"), countRoot),
                cb.or(
                        cb.like(cb.upper(cb.trim(subRolesJoin.get("roleName"))), "%SUPERADMIN%"),
                        cb.like(cb.upper(cb.trim(subRolesJoin.get("roleName"))), "%SUPER_ADMIN%"),
                        cb.like(cb.upper(cb.trim(subRolesJoin.get("roleName"))), "%SUPER ADMIN%")
                )
        );

        if (!isSuperAdmin) {
            countPredicate = cb.and(countPredicate, cb.not(cb.exists(countSubquery)));
        }

        countQuery.where(countPredicate);

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        // ================== RESPONSE ==================
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Users fetched successfully")
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / pageSize))
                .isLastPage((pageNumber + 1) * pageSize >= totalCount)
                .data(users.stream().map(dataUtil::toUserResponse).toList())
                .build();
    }
    // ================== Helper for DRY Predicates ==================
    private Predicate buildUserPredicates(
            CriteriaBuilder cb,
            Root<User> root,
            String keyword,
            boolean includeDeleted
    ) {
        Predicate predicate = cb.equal(root.get("isDeleted"), includeDeleted);

        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            Predicate keywordMatch = cb.or(
                    cb.like(cb.lower(root.get("userName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("mobNo")), like)
            );
            predicate = cb.and(predicate, keywordMatch);
        }

        return predicate;
    }
    private HttpResponse emptyPageResponse(int pageNumber, int pageSize) {
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Users fetched successfully")
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(0L)
                .totalPages(0)
                .isLastPage(true)
                .data(List.of())
                .build();
    }
    private User getAuthUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Unauthorized");
        }
        return (User) authentication.getPrincipal();
    }

    private Set<Integer> getAllowedCompanyIds(User user) {
        return user.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> uc.getCompany().getCompanyId())
                .collect(Collectors.toSet());
    }

    private void validateCompanyAccess(User loggedInUser, User targetUser) {
        Set<Integer> allowedCompanyIds = getAllowedCompanyIds(loggedInUser);

        boolean hasAccess = targetUser.getUserCompanies().stream()
                .anyMatch(uc -> uc.getCompany() != null &&
                        allowedCompanyIds.contains(uc.getCompany().getCompanyId()));

        if (!hasAccess && !hasPermission(loggedInUser, "GLOBAL_ACCESS")) {
            throw new AccessDeniedException("Access denied for this user");
        }
    }
    @Override
    @Transactional
    public MessageResponse softDeleteUser(Integer userId, Authentication authentication) {

        User loggedInUser = getAuthUser(authentication);

        if (!hasPermission(loggedInUser, "DELETE_USER")) {
            throw new AccessDeniedException("No permission to delete user");
        }

        User user = userRepository.findByIdWithCompanies(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (loggedInUser.getUserId().equals(user.getUserId())) {
            throw new BadRequestException("You cannot delete your own account");
        }

        validateCompanyAccess(loggedInUser, user);

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new InactiveStatusException("User already deleted");
        }

        // 🔥 Protected roles via permission (NOT ROLE NAME)
        if (isProtectedUser(user) && !hasPermission(loggedInUser, "DELETE_ADMIN")) {
            throw new AccessDeniedException("Cannot delete protected user");
        }

        user.setIsDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);

        return new MessageResponse(true, HttpStatus.OK, "User soft deleted successfully");
    }

    @Override
    @Transactional
    public MessageResponse restoreUser(Integer userId, Authentication authentication) {

        User loggedInUser = getAuthUser(authentication);

        if (!hasPermission(loggedInUser, "RESTORE_USER")) {
            throw new AccessDeniedException("No permission to restore user");
        }

        User user = userRepository.findByIdWithCompanies(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateCompanyAccess(loggedInUser, user);

        if (!Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new InactiveStatusException("User is already active");
        }

        user.setIsDeleted(false);
        user.setDeletedAt(null);

        userRepository.save(user);

        return new MessageResponse(true, HttpStatus.OK, "User restored successfully");
    }

    @Override
    @Transactional
    public MessageResponse deleteUser(Integer userId, Authentication authentication) {

        User loggedInUser = getAuthUser(authentication);

        if (!hasPermission(loggedInUser, "DELETE_USER_PERMANENT")) {
            throw new AccessDeniedException("No permission for permanent delete");
        }

        User user = userRepository.findByIdWithCompanies(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (loggedInUser.getUserId().equals(user.getUserId())) {
            throw new BadRequestException("You cannot delete your own account");
        }

        validateCompanyAccess(loggedInUser, user);

        if (isProtectedUser(user) && !hasPermission(loggedInUser, "DELETE_ADMIN")) {
            throw new AccessDeniedException("Cannot delete protected user");
        }

        userRepository.delete(user);

        return new MessageResponse(true, HttpStatus.OK, "User permanently deleted");
    }
    private boolean hasPermission(User user, String permissionName) {
        return user.getUserCompanies().stream()
                .map(UserCompany::getRole)
                .filter(Objects::nonNull)
                .flatMap(role -> role.getPermissions() != null
                        ? role.getPermissions().stream()
                        : Stream.empty())
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase(permissionName));
    }
    private boolean isProtectedUser(User user) {
        return user.getUserCompanies().stream()
                .map(UserCompany::getRole)
                .filter(Objects::nonNull)
                .flatMap(role -> role.getPermissions() != null
                        ? role.getPermissions().stream()
                        : Stream.empty())
                .anyMatch(p -> p.getPermissionName().equalsIgnoreCase("CRITICAL_ACCESS"));
    }

}