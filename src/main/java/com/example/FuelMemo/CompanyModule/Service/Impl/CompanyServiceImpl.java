package com.example.FuelMemo.CompanyModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.AuthModule.Repository.RoleRepository;
import com.example.FuelMemo.AuthModule.Repository.UserCompanyRepository;
import com.example.FuelMemo.AuthModule.Repository.UserRepository;
import com.example.FuelMemo.CompanyModule.Dto.CompanyDto;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.CompanyModule.Service.CloudinaryImageService;
import com.example.FuelMemo.CompanyModule.Service.CompanyService;
import com.example.FuelMemo.DriverModule.Repository.DriverRepository;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.FuelMemoModule.Repository.FuelMemoRepository;
import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
import com.example.FuelMemo.FuelStationModule.Repository.FuelStationRepository;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import com.example.FuelMemo.RoyaltyModule.Entity.RoyaltyPass;
import com.example.FuelMemo.RoyaltyModule.Repository.RoyaltyPassRepository;
import com.example.FuelMemo.Shared.Exception.*;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import com.example.FuelMemo.SiteModule.Entity.Site;
import com.example.FuelMemo.SiteModule.Repository.SiteRepository;
import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import com.example.FuelMemo.VehicleModule.Repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CloudinaryImageService cloudinaryImageService;
    private final StateRepository stateRepository;
    private final FuelMemoRepository fuelMemoRepository;
    private final DistrictRepository districtRepository;
    private final UserRepository userRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository, CloudinaryImageService cloudinaryImageService, StateRepository stateRepository, FuelMemoRepository fuelMemoRepository, DistrictRepository districtRepository, UserRepository userRepository, RoleRepository roleRepository, UserCompanyRepository userCompanyRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, SiteRepository siteRepository, FuelStationRepository fuelStationRepository, PasswordEncoder passwordEncoder, DataUtil dataUtil, RoyaltyPassRepository royaltyPassRepository) {
        this.companyRepository = companyRepository;
        this.cloudinaryImageService = cloudinaryImageService;
        this.stateRepository = stateRepository;
        this.fuelMemoRepository = fuelMemoRepository;
        this.districtRepository = districtRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.siteRepository = siteRepository;
        this.fuelStationRepository = fuelStationRepository;
        this.passwordEncoder = passwordEncoder;
        this.dataUtil = dataUtil;
        this.royaltyPassRepository = royaltyPassRepository;
    }

    private final RoleRepository roleRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final SiteRepository siteRepository;
    private final FuelStationRepository fuelStationRepository;
    private final PasswordEncoder passwordEncoder;
    private final DataUtil dataUtil;
    private final RoyaltyPassRepository royaltyPassRepository;
    @PersistenceContext
    private EntityManager entityManager;

//    @Override
//    @Transactional
//    public MessageResponse addCompany(
//            String companyName,
//            String ownerName,
//            String password,
//            MultipartFile logo,
//            String panNumber,
//            String gstNumber,
//            String email,
//            String mobileNo,
//            String alternateMobileNumber,
//            String cinNumber,
//            String tanNumber,
//            String industry,
//            String addressLine1,
//            String addressLine2,
//            String addressLine3,
//            String serviceDescription,
//            Boolean status,
//            String pinCode,
//            Integer stateId,
//            Integer districtId
//    ) {
//
//        // ================= 🔐 AUTH =================
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//
//        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
//            throw new AccessDeniedException("Unauthorized");
//        }
//
//        // ================= 🔐 PERMISSION =================
//        if (!(user.isSuperAdmin() || user.hasGlobalPermission("CREATE_COMPANY"))) {
//            throw new AccessDeniedException("You don't have permission to create company");
//        }
//
//        // ================= ✅ VALIDATION =================
//        if (companyName == null || companyName.isBlank())
//            throw new BadRequestException("Company name is required");
//
//        if (email == null || email.isBlank())
//            throw new BadRequestException("Email is required");
//
//        if (password == null || password.isBlank())
//            throw new BadRequestException("Password is required");
//
//        // ================= 🔁 DUPLICATE CHECK =================
//        if (companyRepository.existsByEmail(email))
//            throw new DuplicateEntryException("Email already exists");
//
//        if (gstNumber != null && companyRepository.existsByGstNumber(gstNumber))
//            throw new DuplicateEntryException("GST already exists");
//
//        if (panNumber != null && companyRepository.existsByPanNumber(panNumber))
//            throw new DuplicateEntryException("PAN already exists");
//
//        if (cinNumber != null && companyRepository.existsByCinNumber(cinNumber))
//            throw new DuplicateEntryException("CIN already exists");
//
//        // ================= 🌍 STATE & DISTRICT =================
//        State state = stateRepository.findById(stateId)
//                .orElseThrow(() -> new ResourceNotFoundException("State not found"));
//
//        District district = districtRepository.findById(districtId)
//                .orElseThrow(() -> new ResourceNotFoundException("District not found"));
//
//        // ================= 🏢 CREATE COMPANY =================
//        Company company = new Company();
//
//        company.setCompanyName(companyName);
//        company.setOwnerName(ownerName);
//        company.setPassWord(passwordEncoder.encode(password)); // 🔐 secure
//        company.setPwd(password);
//
//        company.setEmail(email);
//        company.setMobileNo(mobileNo);
//        company.setAlternateMobileNumber(alternateMobileNumber);
//
//        company.setPanNumber(panNumber);
//        company.setGstNumber(gstNumber);
//        company.setCinNumber(cinNumber);
//        company.setTanNumber(tanNumber);
//
//        company.setIndustry(industry);
//
//        company.setAddressLine1(addressLine1);
//        company.setAddressLine2(addressLine2);
//        company.setAddressLine3(addressLine3);
//        company.setPinCode(pinCode);
//
//        company.setServiceDescription(serviceDescription);
//
//        company.setActive(Boolean.TRUE.equals(status));
//        company.setIsDeleted(false);
//
//        company.setState(state);
//        company.setDistrict(district);
//
//        // ================= 💾 SAVE COMPANY =================
//        companyRepository.save(company);
//
//        // ================= 🖼️ LOGO UPLOAD =================
//        if (logo != null && !logo.isEmpty()) {
//
//            // ✅ FILE SIZE VALIDATION (50MB)
//            long maxSize = 50 * 1024 * 1024;
//            if (logo.getSize() > maxSize) {
//                throw new FileUploadException("File size exceeds 50MB");
//            }
//
//            try {
//                Map<String, Object> uploadData =
//                        cloudinaryImageService.upload(logo, null);
//
//                if (uploadData.get("url") == null) {
//                    throw new FileUploadException("Image upload failed");
//                }
//
//                company.setLogo(uploadData.get("url").toString());
//                companyRepository.save(company);
//
//            } catch (Exception e) {
//                throw new FileUploadException("Logo upload failed: " + e.getMessage());
//            }
//        }
//
//        // ================= 🔗 IMPORTANT: USER-COMPANY MAPPING =================
//        // 🔥 Role NULL राखી ને mapping kariye → company show thase
//        UserCompany userCompany = UserCompany.builder()
//                .user(user)
//                .company(company)
//                .role(null) // ✅ role later assign karvanu
//                .active(true)
//                .assignedAt(LocalDateTime.now())
//                .build();
//
//        userCompanyRepository.save(userCompany);
//
//        // ================= ✅ RESPONSE =================
//        return MessageResponse.builder()
//                .success(true)
//                .successCode(HttpStatus.CREATED)
//                .message("Company created successfully")
//                .build();
//    }
@Override
@Transactional
public MessageResponse addCompany(
        String companyName,
        String ownerName,
        String password,
        MultipartFile logo,
        String panNumber,
        String gstNumber,
        String email,
        String mobileNo,
        String alternateMobileNumber,
        String cinNumber,
        String tanNumber,
        String industry,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        String serviceDescription,
        Boolean status,
        String pinCode,
        Integer stateId,
        Integer districtId
) {

    // 🔐 AUTH USER
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null || !(auth.getPrincipal() instanceof User user)) {
        throw new AccessDeniedException("Unauthorized");
    }

    // 🔐 PERMISSION CHECK
    boolean canCreateCompany = user.isSuperAdmin() ||
            user.getUserCompanies().stream()
                    .map(UserCompany::getRole)
                    .filter(Objects::nonNull)
                    .flatMap(role -> role.getPermissions() != null
                            ? role.getPermissions().stream()
                            : Stream.empty())
                    .anyMatch(p -> p.getPermissionName()
                            .equalsIgnoreCase("CREATE_COMPANY"));

    if (!canCreateCompany) {
        throw new AccessDeniedException("You don't have permission to create company");
    }


    // 🔁 VALIDATION
    if (companyName == null || companyName.isBlank())
        throw new BadRequestException("Company name is required");

    if (email == null || email.isBlank())
        throw new BadRequestException("Email is required");

    if (password == null || password.isBlank())
        throw new BadRequestException("Password is required");

    if (companyRepository.existsByEmail(email))
        throw new DuplicateEntryException("Email already exists");

    // 📍 STATE & DISTRICT
    State state = stateRepository.findById(stateId)
            .orElseThrow(() -> new ResourceNotFoundException("State not found"));

    District district = districtRepository.findById(districtId)
            .orElseThrow(() -> new ResourceNotFoundException("District not found"));

    // 🏢 CREATE COMPANY
    Company company = new Company();
    company.setCompanyName(companyName);
    company.setOwnerName(ownerName);
    company.setPassWord(passwordEncoder.encode(password));
    company.setPwd(password);
    company.setEmail(email);
    company.setMobileNo(mobileNo);
    company.setAlternateMobileNumber(alternateMobileNumber);
    company.setPanNumber(panNumber);
    company.setGstNumber(gstNumber);
    company.setCinNumber(cinNumber);
    company.setTanNumber(tanNumber);
    company.setIndustry(industry);
    company.setAddressLine1(addressLine1);
    company.setAddressLine2(addressLine2);
    company.setAddressLine3(addressLine3);
    company.setPinCode(pinCode);
    company.setServiceDescription(serviceDescription);
    company.setActive(Boolean.TRUE.equals(status));
    company.setIsDeleted(false);
    company.setState(state);
    company.setDistrict(district);

//    companyRepository.save(company);
//    Role adminRole = roleRepository
//            .findByRoleNameIgnoreCaseAndCompanyCompanyId("ROLE_ADMIN", company.getCompanyId())
//            .orElseGet(() -> {
//                Role role = new Role();
//                role.setRoleName("ROLE_ADMIN");
//                role.setCompany(company);
//                role.setSystemDefined(true);
//                role.setActive(true);
//                role.setCreatedAt(LocalDateTime.now());
//                role.setUpdatedAt(LocalDateTime.now());
//                return roleRepository.save(role);
//            });

    // 🖼️ LOGO UPLOAD
    if (logo != null && !logo.isEmpty()) {
        try {
            Map<String, Object> upload = cloudinaryImageService.upload(logo, null);
            company.setLogo(upload.get("url").toString());
            companyRepository.save(company);
        } catch (Exception e) {
            throw new FileUploadException("Logo upload failed");
        }
    }

    // 🔥 ROLE ASSIGN (FIXED)
    Role userRole;

    if (user.isSuperAdmin()) {
        userRole = roleRepository.findByRoleName("ROLE_SUPERADMIN");
    } else {
        userRole = roleRepository.findByRoleName("ROLE_ADMIN");
    }

    if (userRole == null) {
        throw new RuntimeException("Role not found. Please create ROLE_ADMIN first.");
    }

    // 🔗 MAP USER → COMPANY
    UserCompany uc = UserCompany.builder()
            .user(user)
            .company(company)
            .role(userRole) // ✅ NEVER NULL
            .active(true)
            .assignedAt(LocalDateTime.now())
            .build();

    userCompanyRepository.save(uc);

    return new MessageResponse(
            true,
            HttpStatus.CREATED,
            "Company created successfully"
    );
}
    @Override
    @Transactional
    public HttpResponse getAllCompanies(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted,
            Authentication authentication
    ) {

        // ================= 🔐 AUTH VALIDATION =================
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        boolean isSuperAdmin = user.isSuperAdmin();

        // ================= 📄 PAGINATION VALIDATION =================
        if (pageNumber < 0) pageNumber = 0;
        if (pageSize <= 0) pageSize = 10;

        // ================= 🔽 SORT VALIDATION =================
        List<String> allowedSortFields = List.of("companyName", "ownerName", "email", "mobileNo", "industry");
        if (sortBy == null || !allowedSortFields.contains(sortBy)) {
            sortBy = "companyName";
        }
        if (sortDirection == null) sortDirection = "asc";

        // ================= 🏢 ACCESSIBLE COMPANIES =================
        List<Integer> accessibleCompanyIds = null;
        if (!isSuperAdmin) {
            accessibleCompanyIds = user.getUserCompanies()
                    .stream()
                    .filter(UserCompany::getActive)
                    .map(uc -> uc.getCompany().getCompanyId())
                    .toList();

            if (accessibleCompanyIds.isEmpty()) {
                return HttpResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("No companies accessible")
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalElements(0L)
                        .totalPages(0)
                        .isLastPage(true)
                        .data(Collections.emptyList())
                        .build();
            }
        }

        // ================= CRITERIA QUERY =================
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Company> cq = cb.createQuery(Company.class);
        Root<Company> root = cq.from(Company.class);

        List<Predicate> predicates = new ArrayList<>();

        // Accessible companies filter
        if (!isSuperAdmin) {
            predicates.add(root.get("companyId").in(accessibleCompanyIds));
        }

        // Keyword search only on text columns
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("companyName")), like),
                    cb.like(cb.lower(root.get("ownerName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("mobileNo")), like),
                    cb.like(cb.lower(root.get("industry")), like)
            ));
        }

        // Deleted filter
        if (isDeleted == null) {
            predicates.add(cb.equal(root.get("isDeleted"), false));
        } else {
            predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // Sorting
        Order order = "desc".equalsIgnoreCase(sortDirection)
                ? cb.desc(root.get(sortBy))
                : cb.asc(root.get(sortBy));
        cq.orderBy(order);

        // Pagination
        List<Company> companies = entityManager.createQuery(cq)
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        // ================= COUNT QUERY =================
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Company> countRoot = countQuery.from(Company.class);
        List<Predicate> countPredicates = new ArrayList<>();

        if (!isSuperAdmin) {
            countPredicates.add(countRoot.get("companyId").in(accessibleCompanyIds));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.toLowerCase() + "%";
            countPredicates.add(cb.or(
                    cb.like(cb.lower(countRoot.get("companyName")), like),
                    cb.like(cb.lower(countRoot.get("ownerName")), like),
                    cb.like(cb.lower(countRoot.get("email")), like),
                    cb.like(cb.lower(countRoot.get("mobileNo")), like),
                    cb.like(cb.lower(countRoot.get("industry")), like)
            ));
        }
        if (isDeleted == null) {
            countPredicates.add(cb.equal(countRoot.get("isDeleted"), false));
        } else {
            countPredicates.add(cb.equal(countRoot.get("isDeleted"), isDeleted));
        }

        countQuery.select(cb.count(countRoot))
                .where(countPredicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // ================= DTO MAPPING =================
        List<CompanyDto> dtoList = companies.stream()
                .map(dataUtil::toCompanyDto)
                .toList();

        // ================= RESPONSE =================
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Companies fetched successfully")
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / pageSize))
                .isLastPage((pageNumber + 1) * pageSize >= totalElements)
                .data(dtoList)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse updateCompany(
            String companyName,
            String ownerName,
            String password,
            MultipartFile logo,
            String panNumber,
            String gstNumber,
            String email,
            String alternateMobileNumber,
            String mobileNo,
            String cinNumber,
            String tanNumber,
            String industry,
            String addressLine1,
            String addressLine2,
            String addressLine3,
            String serviceDescription,
            Boolean status,
            String pinCode,
            Integer stateId,
            Integer districtId,
            Integer companyId,
            Map<String, String> headers) {

        // ================= 🔐 AUTH CHECK =================
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        // ================= 🏢 FETCH COMPANY =================
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // ================= 🔐 PERMISSION CHECK =================
        if (!(user.isSuperAdmin() || user.hasPermission("UPDATE_COMPANY", companyId))) {
            throw new AccessDeniedException("You don't have permission to update company");
        }

        // ================= 🔁 DUPLICATE CHECK =================
        if (email != null && !email.equals(company.getEmail())
                && companyRepository.existsByEmail(email)) {
            throw new DuplicateEntryException("Email already exists");
        }

        if (gstNumber != null && !gstNumber.equals(company.getGstNumber())
                && companyRepository.existsByGstNumber(gstNumber)) {
            throw new DuplicateEntryException("GST already exists");
        }

        if (panNumber != null && !panNumber.equals(company.getPanNumber())
                && companyRepository.existsByPanNumber(panNumber)) {
            throw new DuplicateEntryException("PAN already exists");
        }

        if (cinNumber != null && !cinNumber.equals(company.getCinNumber())
                && companyRepository.existsByCinNumber(cinNumber)) {
            throw new DuplicateEntryException("CIN already exists");
        }

        // ================= 🌍 STATE =================
        if (stateId != null) {
            State state = stateRepository.findById(stateId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid State ID"));
            company.setState(state);
        }

        // ================= 📍 DISTRICT =================
        if (districtId != null) {
            District district = districtRepository.findById(districtId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid District ID"));
            company.setDistrict(district);
        }

        // ================= 🔁 SAFE UPDATE =================
        if (companyName != null && !companyName.trim().isEmpty())
            company.setCompanyName(companyName);

        if (ownerName != null)
            company.setOwnerName(ownerName);

        if (password != null && !password.isBlank()){
            company.setPassWord(passwordEncoder.encode(password));
            company.setPwd(password);}
        if (email != null)
            company.setEmail(email);

        if (mobileNo != null)
            company.setMobileNo(mobileNo);

        if (alternateMobileNumber != null)
            company.setAlternateMobileNumber(alternateMobileNumber);

        if (panNumber != null)
            company.setPanNumber(panNumber);

        if (gstNumber != null)
            company.setGstNumber(gstNumber);

        if (cinNumber != null)
            company.setCinNumber(cinNumber);

        if (tanNumber != null)
            company.setTanNumber(tanNumber);

        if (industry != null)
            company.setIndustry(industry);

        if (addressLine1 != null)
            company.setAddressLine1(addressLine1);

        if (addressLine2 != null)
            company.setAddressLine2(addressLine2);

        if (addressLine3 != null)
            company.setAddressLine3(addressLine3);

        if (serviceDescription != null)
            company.setServiceDescription(serviceDescription);

        if (pinCode != null)
            company.setPinCode(pinCode);

        if (status != null)
            company.setActive(status);

        // ================= 🖼️ LOGO UPDATE =================
        if (logo != null && !logo.isEmpty()) {
            try {
                Map<String, Object> uploadData =
                        cloudinaryImageService.upload(logo, headers);

                company.setLogo((String) uploadData.get("url"));

            } catch (Exception e) {
                throw new FileUploadException("Logo upload failed: " + e.getMessage());
            }
        }

        // ================= 💾 SAVE =================
        companyRepository.save(company);

        // ================= ✅ RESPONSE =================
        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Company updated successfully")
                .build();
    }
    @Override
    public DataResponse getCompanyById(Integer companyId) {

        // 🔐 AUTH
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        // 🏢 FETCH
        Company company = Optional.ofNullable(
                companyRepository.findByCompanyIdAndIsDeletedFalse(companyId)
        ).orElseThrow(() ->
                new ResourceNotFoundException("Active company not found with id: " + companyId)
        );

        // 🔐 PERMISSION
        if (!(user.isSuperAdmin() || user.hasPermission("VIEW_COMPANY", companyId))) {
            throw new AccessDeniedException("You don't have permission to view this company");
        }

        // ✅ RESPONSE
        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dataUtil.toCompanyDto(company))
                .build();
    }

    @Override
    public DataResponse getAllDeletedCompanies() {

        // 🔐 AUTH
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        // 🔐 PERMISSION
        if (!(user.isSuperAdmin() || user.hasGlobalPermission("VIEW_COMPANY"))) {
            throw new AccessDeniedException("You don't have permission to view deleted companies");
        }

        // 📦 FETCH
        List<Company> companies = companyRepository.findByIsDeletedTrue();

        // 🔄 MAP
        List<CompanyDto> dtoList = companies.stream()
                .map(dataUtil::toCompanyDto)
                .toList();

        // ✅ RESPONSE
        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dtoList)
                .build();
    }



    @Override
    @Transactional
    public MessageResponse softDeleteCompany(Integer companyId) {

        // 🔐 AUTH
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        // 🔐 PERMISSION
        if (!(user.isSuperAdmin() || user.hasGlobalPermission("DELETE_COMPANY"))) {
            throw new AccessDeniedException("You don't have permission to delete company");
        }

        // 📦 FETCH
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // 🔐 ACCESS CHECK
        boolean hasAccess = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getCompany().getCompanyId().equals(companyId));

        if (!user.isSuperAdmin() && !hasAccess) {
            throw new AccessDeniedException("You don't have access to this company");
        }

        // ❌ Already deleted
        if (Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new IllegalArgumentException("Company already deleted");
        }

        // ⚠️ VALIDATION
        validateCompanyCanBeDeleted(companyId);

        // 🗑️ DELETE
        company.softDelete();
        companyRepository.save(company);

        return new MessageResponse(true, HttpStatus.OK,
                "Company soft deleted successfully");
    }
    private void validateCompanyCanBeDeleted(Integer companyId) {

        if (userCompanyRepository.existsByCompanyCompanyId(companyId))
            throw new IllegalStateException("Users are assigned");

        if (roleRepository.existsByCompanyCompanyId(companyId))
            throw new IllegalStateException("Roles exist");

        if (vehicleRepository.existsByCompanyCompanyId(companyId))
            throw new IllegalStateException("Vehicles exist");

        if (driverRepository.existsByCompaniesCompanyId(companyId))
            throw new IllegalStateException("Drivers exist");

        if (siteRepository.existsByCompanyCompanyId(companyId))
            throw new IllegalStateException("Sites exist");

        if (fuelStationRepository.existsByCompanyCompanyId(companyId))
            throw new IllegalStateException("Fuel stations exist");

        if (fuelMemoRepository.existsByCompanyCompanyId(companyId))
            throw new IllegalStateException("Fuel memos exist");
    }
    private void restoreCompanyDependencies(Integer companyId) {

        // 🔹 Royalty Pass
        List<RoyaltyPass> passes =
                royaltyPassRepository.findByCompanyCompanyIdAndDeletedTrue(companyId);
        passes.forEach(p -> p.setDeleted(false));
        royaltyPassRepository.saveAll(passes);

        // 🔹 Roles
        List<Role> roles = roleRepository.findByCompanyCompanyIdAndIsDeletedTrue(companyId);
        roles.forEach(r -> r.setIsDeleted(false));
        roleRepository.saveAll(roles);

        // 🔹 Vehicles
        List<Vehicle> vehicles = vehicleRepository.findByCompanyCompanyIdAndDeletedTrue(companyId);
        vehicles.forEach(v -> v.setDeleted(false));
        vehicleRepository.saveAll(vehicles);

        // 🔹 Sites
        List<Site> sites = siteRepository.findByCompanyCompanyIdAndDeletedTrue(companyId);
        sites.forEach(s -> s.setDeleted(false));
        siteRepository.saveAll(sites);

        // 🔹 Fuel Stations
        List<FuelStation> stations =
                fuelStationRepository.findByCompanyCompanyIdAndDeletedTrue(companyId);
        stations.forEach(fs -> fs.setDeleted(false));
        fuelStationRepository.saveAll(stations);

        // 🔹 Fuel Memos
        List<FuelMemo> memos =
                fuelMemoRepository.findByCompanyCompanyIdAndDeletedTrue(companyId);
        memos.forEach(fm -> fm.setDeleted(false));
        fuelMemoRepository.saveAll(memos);
    }
    @Override
    @Transactional
    public MessageResponse restoreCompany(Integer companyId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        if (!(user.isSuperAdmin() || user.hasGlobalPermission("RESTORE_COMPANY"))) {
            throw new AccessDeniedException("No permission");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        boolean hasAccess = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getCompany().getCompanyId().equals(companyId));

        if (!user.isSuperAdmin() && !hasAccess) {
            throw new AccessDeniedException("No access");
        }

        if (!Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new IllegalArgumentException("Company already active");
        }

        // ✅ Restore company
        company.setIsDeleted(false);
        company.setActive(true);
        company.setDeletedAt(null);
        companyRepository.save(company);

        // 🔥 Restore ALL dependencies
        restoreCompanyDependencies(companyId);

        return new MessageResponse(true, HttpStatus.OK,
                "Company restored successfully");
    }

    @Override
    @Transactional
    public MessageResponse deleteCompany(Integer companyId) {

        // 🔐 AUTH
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Unauthorized");
        }

        // 🔐 PERMISSION
        if (!(user.isSuperAdmin() || user.hasGlobalPermission("DELETE_COMPANY"))) {
            throw new AccessDeniedException("You don't have permission to permanently delete company");
        }

        // 📦 FETCH
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // 🔐 ACCESS CHECK
        boolean hasAccess = user.getUserCompanies().stream()
                .anyMatch(uc -> uc.getCompany().getCompanyId().equals(companyId));

        if (!user.isSuperAdmin() && !hasAccess) {
            throw new AccessDeniedException("You don't have access to this company");
        }

        // ❗ Must be soft deleted first
        if (!Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new IllegalStateException("Please soft delete company first");
        }

        // ⚠️ Optional: validate dependencies before hard delete

        // 🗑️ HARD DELETE
        companyRepository.delete(company);

        return new MessageResponse(true, HttpStatus.OK,
                "Company permanently deleted successfully");
    }
}