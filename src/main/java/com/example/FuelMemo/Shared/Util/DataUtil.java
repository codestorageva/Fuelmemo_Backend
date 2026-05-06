package com.example.FuelMemo.Shared.Util;

import com.example.FuelMemo.AuthModule.Dto.Request.PermissionDto;
import com.example.FuelMemo.AuthModule.Dto.Request.UserDto;
import com.example.FuelMemo.AuthModule.Dto.Response.*;
import com.example.FuelMemo.AuthModule.Dto.RolesDTO;
import com.example.FuelMemo.AuthModule.Entity.Permission;
import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.CompanyModule.Dto.CompanyDto;
import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.DriverModule.Dto.DriverResponseDto;
import com.example.FuelMemo.DriverModule.Entity.Driver;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoResponseDto;
import com.example.FuelMemo.FuelMemoModule.Entity.EntryType;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.FuelStationModule.Dto.Response.FuelStationResponse;
import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
import com.example.FuelMemo.MasterData.Dto.*;
import com.example.FuelMemo.MasterData.Entity.*;
import com.example.FuelMemo.RoyaltyModule.Dto.Response.RoyaltyPassResponse;
import com.example.FuelMemo.RoyaltyModule.Entity.RoyaltyPass;
import com.example.FuelMemo.SiteModule.Dto.SiteResponseDTO;
import com.example.FuelMemo.SiteModule.Entity.Site;
import com.example.FuelMemo.VehicleModule.Dto.CompanyVehicleResponseDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleResponseDTO;
import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataUtil {

    private final ModelMapper modelMapper;

    public DataUtil(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // Entity → DTO
    public CountryDto countryToDto(Country country) {
        return modelMapper.map(country, CountryDto.class);
    }

    // DTO → Entity
    public Country dtoToCountry(CountryDto dto) {
        return modelMapper.map(dto, Country.class);
    }

    // List Entity → List DTO
    public List<CountryDto> countriesToDtos(List<Country> countries) {
        return countries.stream()
                .map(this::countryToDto)
                .collect(Collectors.toList());
    }

    // Export DTO mapping
    public List<CountryExportDTO> countriesToExportDtos(List<Country> countries) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");

        return countries.stream()
                .map(c -> new CountryExportDTO(
                        c.getCountryId(),
                        c.getCountryName(),
                        c.getUpdatedAt() != null
                                ? c.getUpdatedAt().format(formatter)
                                : null
                ))
                .collect(Collectors.toList());
    }

    // List DTO → List Entity
    public List<Country> dtosToCountries(List<CountryDto> dtos) {
        Type listType = new TypeToken<List<Country>>() {
        }.getType();
        return modelMapper.map(dtos, listType);
    }

    //---------------------------------------------------STATE UTILS----------------------------------------------------

    public StateDto stateToDto(State s) {
        return new StateDto(s.getStateId(), s.getStateName(), s.getCountry().getCountryId(), s.getCountry().getCountryName(), s.getStatus(), s.getIsDeleted(), s.getCreatedAt(), s.getUpdatedAt(), s.getDeletedAt());
    }

    public List<StateDto> statesToDtos(List<State> states) {

        List<StateDto> dtos = new ArrayList<>();

        for (State s : states) {
            dtos.add(new StateDto(s.getStateId(), s.getStateName(), s.getCountry().getCountryId(), s.getCountry().getCountryName(), s.getStatus(), s.getIsDeleted(), s.getCreatedAt(), s.getUpdatedAt(), s.getDeletedAt()));
        }
        return dtos;
    }

    public List<State> dtosToStates(List<StateDto> dtos) {
        java.lang.reflect.Type targetListType = new TypeToken<List<State>>() {
        }.getType();
        return modelMapper.map(dtos, targetListType);
    }

    public List<StateExportDTO> stateExportDTOStoStates(List<State> states) {
        List<StateExportDTO> dtos = new ArrayList<>();
        for (State s : states) {
            dtos.add(new StateExportDTO(
                    s.getStateId(), s.getStateName(), s.getCountry().getCountryName(), s.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }

    //---------------------------------------------------DISTRICT UTILS-------------------------------------------------

    public DistrictDto districtToDto(District d) {

        if (d == null) {
            return null;
        }

        return new DistrictDto(
                d.getDistrictId(),
                d.getCityName(),
                d.getApiCityName(),   // 🔥 ADD THIS

                d.getState() != null ? d.getState().getStateId() : null,
                d.getState() != null ? d.getState().getStateName() : null,

                d.getCountry() != null ? d.getCountry().getCountryId() : null,
                d.getCountry() != null ? d.getCountry().getCountryName() : null,

                d.getStatus()
        );
    }

    public List<DistrictDto> districtsToDtos(List<District> districts) {

        List<DistrictDto> dtos = new ArrayList<>();

        for (District d : districts) {

            dtos.add(new DistrictDto(
                    d.getDistrictId(),
                    d.getCityName(),
                    d.getApiCityName(),   // 🔥 ADD

                    d.getState() != null ? d.getState().getStateId() : null,
                    d.getState() != null ? d.getState().getStateName() : null,

                    d.getCountry() != null ? d.getCountry().getCountryId() : null,
                    d.getCountry() != null ? d.getCountry().getCountryName() : null,

                    d.getStatus()
            ));
        }

        return dtos;
    }

    public List<DistrictExportDTO> districtExportDTOStoDistricts(List<District> districts) {
        List<DistrictExportDTO> dtos = new ArrayList<>();
        for (District d : districts) {
            dtos.add(new DistrictExportDTO(
                    d.getDistrictId(),
                    d.getCityName(),
                    d.getApiCityName(),   // 🔥 ADD
                    d.getState().getStateName(),
                    d.getCountry().getCountryName(),
                    d.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))
            ));   }
        return dtos;
    }

    //---------------------------------------------------SUB-DISTRICT UTILS-------------------------------------------------

    public SubDistrictDto subDistrictToDto(SubDistrict s) {
        return new SubDistrictDto(s.getSubDistrictId() != null ? s.getSubDistrictId() : null, s.getSubDistrictName() != null ? s.getSubDistrictName() : null,
                s.getDistrict() != null ? s.getDistrict().getDistrictId() : null, s.getDistrict() != null ? s.getDistrict().getCityName() : null,
                s.getState() != null ? s.getState().getStateId() : null, s.getState() != null ? s.getState().getStateName() : null,
                s.getCountry() != null ? s.getCountry().getCountryId() : null, s.getCountry() != null ? s.getCountry().getCountryName() : null,
                s.getStatus() != null ? s.getStatus() : null, s.getIsDeleted() != null ? s.getIsDeleted() : null, s.getCreatedAt() != null ? s.getCreatedAt() : null, s.getUpdatedAt() != null ? s.getUpdatedAt() : null, s.getDeletedAt() != null ? s.getDeletedAt() : null);
    }

    public List<SubDistrictDto> subDistrictsToDtos(List<SubDistrict> subDistricts) {
        List<SubDistrictDto> dtos = new ArrayList<>();

        for (SubDistrict s : subDistricts) {
            dtos.add(new SubDistrictDto(s.getSubDistrictId() != null ? s.getSubDistrictId() : null, s.getSubDistrictName() != null ? s.getSubDistrictName() : null,
                    s.getDistrict() != null ? s.getDistrict().getDistrictId() : null, s.getDistrict() != null ? s.getDistrict().getCityName() : null,
                    s.getState() != null ? s.getState().getStateId() : null, s.getState() != null ? s.getState().getStateName() : null,
                    s.getCountry() != null ? s.getCountry().getCountryId() : null, s.getCountry() != null ? s.getCountry().getCountryName() : null,
                    s.getStatus() != null ? s.getStatus() : null, s.getIsDeleted() != null ? s.getIsDeleted() : null, s.getCreatedAt() != null ? s.getCreatedAt() : null, s.getUpdatedAt() != null ? s.getUpdatedAt() : null, s.getDeletedAt() != null ? s.getDeletedAt() : null));
        }
        return dtos;
    }

    public List<SubDistrictExportDTO> subDistrictExportDTOStoSubDistricts(List<SubDistrict> subdistricts) {
        List<SubDistrictExportDTO> dtos = new ArrayList<>();
        for (SubDistrict d : subdistricts) {
            dtos.add(new SubDistrictExportDTO(
                    d.getSubDistrictId(), d.getSubDistrictName(), d.getDistrict().getCityName(), d.getState().getStateName(), d.getCountry().getCountryName(), d.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }

    //---------------------------------------------------VILLAGE UTILS-------------------------------------------------

    public VillageDto villageToDto(Village v) {
        return new VillageDto(v.getVillageId() != null ? v.getVillageId() : null, v.getVillageName() != null ? v.getVillageName() : null,
                v.getSubDistrict() != null ? v.getSubDistrict().getSubDistrictId() : null, v.getSubDistrict() != null ? v.getSubDistrict().getSubDistrictName() : null,
                v.getDistrict() != null ? v.getDistrict().getDistrictId() : null, v.getDistrict() != null ? v.getDistrict().getCityName() : null,
                v.getState() != null ? v.getState().getStateId() : null, v.getState() != null ? v.getState().getStateName() : null,
                v.getCountry() != null ? v.getCountry().getCountryId() : null, v.getCountry() != null ? v.getCountry().getCountryName() : null,
                v.getStatus() != null ? v.getStatus() : null, v.getIsDeleted() != null ? v.getIsDeleted() : null, v.getCreatedAt() != null ? v.getCreatedAt() : null, v.getUpdatedAt() != null ? v.getUpdatedAt() : null, v.getDeletedAt() != null ? v.getDeletedAt() : null);
    }

    public List<VillageDto> villagesToDtos(List<Village> villages) {

        List<VillageDto> dtos = new ArrayList<>();

        for (Village v : villages) {
            dtos.add(new VillageDto(v.getVillageId() != null ? v.getVillageId() : null, v.getVillageName() != null ? v.getVillageName() : null,
                    v.getSubDistrict() != null ? v.getSubDistrict().getSubDistrictId() : null, v.getSubDistrict() != null ? v.getSubDistrict().getSubDistrictName() : null,
                    v.getDistrict() != null ? v.getDistrict().getDistrictId() : null, v.getDistrict() != null ? v.getDistrict().getCityName() : null,
                    v.getState() != null ? v.getState().getStateId() : null, v.getState() != null ? v.getState().getStateName() : null,
                    v.getCountry() != null ? v.getCountry().getCountryId() : null, v.getCountry() != null ? v.getCountry().getCountryName() : null,
                    v.getStatus() != null ? v.getStatus() : null, v.getIsDeleted() != null ? v.getIsDeleted() : null, v.getCreatedAt() != null ? v.getCreatedAt() : null, v.getUpdatedAt() != null ? v.getUpdatedAt() : null, v.getDeletedAt() != null ? v.getDeletedAt() : null));
        }
        return dtos;
    }

    public List<VillageExportDTO> villageExportDTOStoVillages(List<Village> villages) {
        List<VillageExportDTO> dtos = new ArrayList<>();
        for (Village v : villages) {
            dtos.add(new VillageExportDTO(
                    v.getVillageId(), v.getVillageName(), v.getSubDistrict().getSubDistrictName(), v.getDistrict().getCityName(), v.getState().getStateName(), v.getCountry().getCountryName(), v.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a"))));
        }
        return dtos;
    }


    //---------------------------------------------------FILE TYPE UTILS-------------------------------------------------
    public boolean isSupportedExtension(String ext) {
        int i = ext.lastIndexOf(".");

        String extension = "";

        if (i != -1) {
            extension = ext.substring(i + 1);
        }

        return extension != null && (
                extension.equals("png")
                        || extension.equals("jpg")
                        || extension.equals("jpeg")
                        || extension.equals("pdf"))
                || extension.equals("JPG")
                || extension.equals("JPEG")
                || extension.equals("PDF")
                || extension.equals("PNG");
    }

    public boolean isSupportedExtensionBatch(String ext) {
        int i = ext.lastIndexOf(".");

        String extension = "";

        if (i != -1) {
            extension = ext.substring(i + 1);
        }

        return extension != null && (
                extension.equals("csv")
                        || extension.equals("excel"))
                || extension.equals("CSV");
    }

    public List<CountryExportDTO> countryExportDTOStoCountries(List<Country> resultList) {
        return null;
    }

    // ---------------------------------- Company Mapping ----------------------------------------

    public CompanyDto toCompanyDto(Company company) {

        if (company == null) return null;

        CompanyDto dto = new CompanyDto();

        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setOwnerName(company.getOwnerName());
        dto.setEmail(company.getEmail());
        dto.setMobileNo(company.getMobileNo());
        dto.setAlternateMobileNumber(company.getAlternateMobileNumber());
        dto.setPwd(company.getPwd());

        dto.setPanNumber(company.getPanNumber());
        dto.setGstNumber(company.getGstNumber());
        dto.setCinNumber(company.getCinNumber());
        dto.setTanNumber(company.getTanNumber());

        dto.setIndustry(company.getIndustry());
        dto.setAddressLine1(company.getAddressLine1());
        dto.setAddressLine2(company.getAddressLine2());
        dto.setAddressLine3(company.getAddressLine3());
        dto.setServiceDescription(company.getServiceDescription());

        // ✅ STATE
        if (company.getState() != null) {
            dto.setStateId(company.getState().getStateId());
            dto.setStateName(company.getState().getStateName());
        }

        // ✅ DISTRICT
        if (company.getDistrict() != null) {
            dto.setDistrictId(company.getDistrict().getDistrictId());
            dto.setCityName(company.getDistrict().getCityName());
        }

        dto.setPinCode(company.getPinCode());
        dto.setActive(company.getActive());
        dto.setIsDeleted(company.getIsDeleted());
        dto.setLogo(company.getLogo());

        // ================= ROLE → USERS MAPPING =================
        // ================= ROLE → USERS (ONLY ADMIN) =================
        if (company.getRoles() != null && company.getUserCompanies() != null) {

            List<RolesDTO> roleDtos = new ArrayList<>();

            for (Role role : company.getRoles()) {

                // 🔥 ONLY ADMIN ROLE
                if (!"ROLE_ADMIN".equalsIgnoreCase(role.getRoleName())) {
                    continue;
                }

                List<UserDto> users = company.getUserCompanies().stream()
                        .filter(uc -> uc.getRole() != null
                                && uc.getRole().getRoleId().equals(role.getRoleId())
                                && uc.getUser() != null)
                        .map(uc -> {
                            User user = uc.getUser();

                            return UserDto.builder()
                                    .userId(user.getUserId())
                                    .userName(user.getUserName())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .email(user.getEmail())

                                    // 🔥 FAST & CLEAN (only current role)
                                    .roleIds(List.of(uc.getRole().getRoleId()))
                                    .build();
                        })
                        // 🔥 REMOVE DUPLICATE USERS
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(UserDto::getUserId, u -> u, (a, b) -> a),
                                m -> new ArrayList<>(m.values())
                        ));

                // 🔥 skip empty
                if (!users.isEmpty()) {
                    roleDtos.add(
                            RolesDTO.builder()
                                    .roleName(role.getRoleName())
                                    .users(users)
                                    .build()
                    );
                }
            }

            dto.setRoles(roleDtos);
        }


        return dto;
    }
    public Company toCompanyEntity(CompanyDto dto, State state, District district) {

        if (dto == null) return null;

        Company company = new Company();

        company.setCompanyId(dto.getCompanyId());
        company.setCompanyName(dto.getCompanyName());
        company.setOwnerName(dto.getOwnerName());
        company.setEmail(dto.getEmail());
        company.setMobileNo(dto.getMobileNo());

        company.setPanNumber(dto.getPanNumber());
        company.setGstNumber(dto.getGstNumber());
        company.setCinNumber(dto.getCinNumber());
        company.setTanNumber(dto.getTanNumber());

        company.setIndustry(dto.getIndustry());
        company.setAddressLine1(dto.getAddressLine1());
        company.setAddressLine2(dto.getAddressLine2());
        company.setAddressLine3(dto.getAddressLine3());
        company.setServiceDescription(dto.getServiceDescription());

        company.setState(state);
        company.setDistrict(district);

        company.setPinCode(dto.getPinCode());
        company.setActive(dto.getActive() != null ? dto.getActive() : true);
        company.setIsDeleted(dto.getIsDeleted() != null ? dto.getIsDeleted() : false);

        company.setLogo(dto.getLogo());

        return company;
    }

    // ---------------------------------- User Mapping ----------------------------------------

//    public UserResponse toUserDto(User user) {
//        UserResponse dto = new UserResponse();
//        dto.setUserId(user.getUserId());
//        dto.setUserName(user.getUserName());
//        dto.setEmail(user.getEmail());
//        dto.setPwd(user.getPwd());
//        dto.setFirstName(user.getFirstName());
//        dto.setLastName(user.getLastName());
//        dto.setDob(user.getDob());
//        dto.setMobNo(user.getMobNo());
//        dto.setAddressLine1(user.getAddressLine1());
//        dto.setAddressLine2(user.getAddressLine2());
//        dto.setAddressLine3(user.getAddressLine3());
//
//        // ---------------- COMPANY & ROLE MAPPING ----------------
//        if (user.getUserCompanies() != null && !user.getUserCompanies().isEmpty()) {
//
//            // Collect all company IDs for the user
//            List<Integer> companyIds = user.getUserCompanies().stream()
//                    .filter(uc -> uc.getCompany() != null)
//                    .map(uc -> uc.getCompany().getCompanyId())
//                    .distinct()
//                    .toList();
//            dto.setCompanyIds(companyIds);
//
//            // Collect all role IDs for the user
//            List<Integer> roleIds = user.getUserCompanies().stream()
//                    .filter(uc -> uc.getRole() != null)
//                    .map(uc -> uc.getRole().getRoleId())
//                    .distinct()
//                    .toList();
//            dto.setRoleIds(roleIds);
//        } else {
//            dto.setCompanyIds(Collections.emptyList());
//            dto.setRoleIds(Collections.emptyList());
//        }
//
//        return dto;
//    }

    public UserResponse toUserDto(User user) {

        List<CompanyResponseDto> companies = user.getUserCompanies().stream()
                .filter(uc -> uc.getCompany() != null)
                .map(uc -> CompanyResponseDto.builder()
                        .companyId(uc.getCompany().getCompanyId())
                        .companyName(uc.getCompany().getCompanyName())
                        .build()
                )
                .distinct()
                .toList();

        List<RoleResponseDto> roles = user.getUserCompanies().stream()
                .filter(uc -> uc.getRole() != null)
                .map(uc -> RoleResponseDto.builder()
                        .roleId(uc.getRole().getRoleId())
                        .roleName(uc.getRole().getRoleName())
                        .build()
                )
                .distinct()
                .toList();

        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobNo(user.getMobNo())
                .companies(companies)
                .roles(roles)
                .build();
    }
//    public User toUserEntity(UserResponse dto) {
//
//        User user = new User();
//        user.setUserId(dto.getUserId());
//        user.setUserName(dto.getUserName());
//        user.setEmail(dto.getEmail());
//        user.setPwd(dto.getPwd());
//        user.setFirstName(dto.getFirstName());
//        user.setLastName(dto.getLastName());
//        user.setDob(dto.getDob());
//        user.setMobNo(dto.getMobNo());
//        user.setAddressLine1(dto.getAddressLine1());
//        user.setAddressLine2(dto.getAddressLine2());
//        user.setAddressLine3(dto.getAddressLine3());
//
//        // ---------------- ROLE + COMPANY MAPPING ----------------
//        if (dto.getCompanyIds() != null && !dto.getCompanyIds().isEmpty()) {
//            for (Integer companyId : dto.getCompanyIds()) {
//
//                UserCompany userCompany = new UserCompany();
//                userCompany.setUser(user);
//
//                Company company = new Company();
//                company.setCompanyId(companyId);
//                userCompany.setCompany(company);
//
//                // Assign all roles to each company
//                if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
//                    for (Integer roleId : dto.getRoleIds()) {
//                        Role role = new Role();
//                        role.setRoleId(roleId);
//                        userCompany.setRole(role); // assuming one role per UserCompany
//                        user.getUserCompanies().add(userCompany);
//                    }
//                } else {
//                    user.getUserCompanies().add(userCompany);
//                }
//            }
//        }
//
//        return user;
//    }

    public User toUserEntity(UserResponse dto) {
        User user = new User();

        // Basic fields
        user.setUserId(dto.getUserId());
        user.setUserName(dto.getUserName());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDob(dto.getDob());
        user.setMobNo(dto.getMobNo());
        user.setAddressLine1(dto.getAddressLine1());
        user.setAddressLine2(dto.getAddressLine2());
        user.setAddressLine3(dto.getAddressLine3());

        // Build new UserCompany set
        Set<UserCompany> newUserCompanies = new HashSet<>();

        if (dto.getCompanies() != null) {
            for (CompanyResponseDto companyDto : dto.getCompanies()) {
                if (companyDto.getCompanyId() == null || companyDto.getRoles() == null) continue;

                for (RoleResponseDto roleDto : companyDto.getRoles()) {
                    if (roleDto.getRoleId() == null) continue;

                    UserCompany uc = new UserCompany();
                    uc.setUser(user);

                    Company company = new Company();
                    company.setCompanyId(companyDto.getCompanyId());
                    uc.setCompany(company);

                    Role role = new Role();
                    role.setRoleId(roleDto.getRoleId());
                    uc.setRole(role);

                    uc.setActive(true);
                    uc.setAssignedAt(LocalDateTime.now());

                    newUserCompanies.add(uc);
                }
            }
        }

        // ✅ Instead of setUserCompanies(), modify the existing final set
        user.getUserCompanies().clear();
        user.getUserCompanies().addAll(newUserCompanies);

        return user;
    }
    public DriverResponseDto mapToDriverResponseDTO(Driver driver) {

        if (driver == null) {
            return null;
        }

        Set<CompanyResponseDto> companyDtos = null;

        if (driver.getCompanies() != null) {
            companyDtos = driver.getCompanies()
                    .stream()
                    .map(this::mapToCompanyResponseDTO)
                    .collect(Collectors.toSet());
        }

        return DriverResponseDto.builder()
                .driverId(driver.getDriverId())
                .driverName(driver.getDriverName())
                .companies(companyDtos)
                .build();
    }

    public static FuelMemoResponseDto mapToFuelMemoDto(FuelMemo memo) {

        if (memo == null) return null;

        // =========================
        // ✅ MONTH STATUS LOGIC
        // =========================

        boolean isClosed = false;
        boolean isReopened = false;

        if (memo.getFuelMonth() != null) {
            isClosed = Boolean.TRUE.equals(memo.getFuelMonth().getClosed());
            isReopened = memo.getFuelMonth().getReopenedAt() != null;
        }

        // =========================
        // ✅ EDITABLE LOGIC
        // =========================

        boolean editable =
                memo.getEntryType() == EntryType.NORMAL &&
                        (!isClosed || isReopened);

        // =========================
        // (OPTIONAL) MONTH STATUS
        // =========================

        String monthStatus = "OPEN";

        if (isClosed && !isReopened) {
            monthStatus = "CLOSED";
        } else if (isClosed && isReopened) {
            monthStatus = "REOPEN";
        }

        // =========================
        // DTO BUILD
        // =========================

        FuelMemoResponseDto dto = FuelMemoResponseDto.builder()
                .id(memo.getFuelMemoId())
                .srNo(memo.getSrNo())
                .date(memo.getDate())
                .quantity(memo.getQuantity())

                // ✅ FIXED HERE
                .preparedBy(
                        memo.getUser() != null
                                ? memo.getUser().getUserName()
                                : null
                )

                // VEHICLE
                .vehicleId(
                        memo.getVehicle() != null ? memo.getVehicle().getVehicleId() : null
                )
                .vehicleName(
                        memo.getVehicle() != null ? memo.getVehicle().getVehicleNumber() : null
                )

                // DRIVER
                .driverId(
                        memo.getDriver() != null ? memo.getDriver().getDriverId() : null
                )
                .driverName(
                        memo.getDriver() != null ? memo.getDriver().getDriverName() : null
                )

                // SITE
                .siteId(
                        memo.getSite() != null ? memo.getSite().getSiteId() : null
                )
                .siteName(
                        memo.getSite() != null ? memo.getSite().getSiteName() : null
                )

                // ROYALTY
                .royaltyId(
                        memo.getRoyaltyPass() != null ? memo.getRoyaltyPass().getRoyaltyId() : null
                )
                .royaltyNumber(
                        memo.getRoyaltyPass() != null ? memo.getRoyaltyPass().getRoyaltyNumber() : null
                )

                // FUEL STATION
                .fuelStationId(
                        memo.getFuelStation() != null ? memo.getFuelStation().getFuelStationId() : null
                )
                .fuelStationName(
                        memo.getFuelStation() != null ? memo.getFuelStation().getFuelStationName() : null
                )

                // COMPANY
                .companyId(
                        memo.getCompany() != null ? memo.getCompany().getCompanyId() : null
                )
                .companyName(
                        memo.getCompany() != null ? memo.getCompany().getCompanyName() : null
                )
                .entryType(memo.getEntryType())
                .editable(editable)
                .monthStatus(monthStatus)
                .isClosed(isClosed)
                .isReopened(isReopened)

                .build();

        return dto;
    }
    public static List<FuelMemoResponseDto> mapToFuelMemoDtoList(List<FuelMemo> memos) {
        if (memos == null || memos.isEmpty()) return List.of();
        return memos.stream().map(DataUtil::mapToFuelMemoDto).collect(Collectors.toList());
    }

    // ================= COMPANY → COMPANY RESPONSE DTO =================
    public CompanyResponseDto mapToCompanyResponseDTO(Company company) {

        if (company == null) {
            return null;
        }

        return CompanyResponseDto.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .build();
    }
    private SiteResponseDTO mapToDTO(Site site) {

        return SiteResponseDTO.builder()
                .siteId(site.getSiteId())
                .siteName(site.getSiteName())
                .siteCode(site.getSiteCode())
                .siteOwnerName(site.getSiteOwnerName())
                .address(site.getAddress())
                .pincode(site.getPincode())
                .stateId(site.getState() != null ? site.getState().getStateId() : null)
                .stateName(site.getState() != null ? site.getState().getStateName() : null)
                .districtId(site.getDistrict() != null ? site.getDistrict().getDistrictId() : null)
                .cityName(site.getDistrict() != null ? site.getDistrict().getCityName() : null)
                .companyId(site.getCompany() != null ? site.getCompany().getCompanyId() : null)
                .companyName(site.getCompany() != null ? site.getCompany().getCompanyName() : null)
                .active(site.getActive())
                .build();
    }
    public static SiteResponseDTO mapToSiteResponse(Site site) {

        return SiteResponseDTO.builder()
                .siteId(site.getSiteId())
                .siteName(site.getSiteName())
                .siteCode(site.getSiteCode())
                .siteOwnerName(site.getSiteOwnerName())
                .address(site.getAddress())
                .pincode(site.getPincode())

                // Direct simple mapping
                .companyId(site.getCompany() != null ? site.getCompany().getCompanyId() : null)
                .companyName(site.getCompany() != null ? site.getCompany().getCompanyName() : null)

                .stateId(site.getState() != null ? site.getState().getStateId() : null)
                .stateName(site.getState() != null ? site.getState().getStateName() : null)

                .districtId(site.getDistrict() != null ? site.getDistrict().getDistrictId() : null)
                .cityName(site.getDistrict() != null ? site.getDistrict().getCityName() : null)

                .active(site.getActive())
                .build();
    }
    public static DriverResponseDto mapToDriverResponse(Driver driver) {

        return DriverResponseDto.builder()
                .driverId(driver.getDriverId())
                .driverName(driver.getDriverName())
                .companies(
                        driver.getCompanies()
                                .stream()
                                .map(company ->
                                        CompanyResponseDto.builder()
                                                .companyId(company.getCompanyId())
                                                .companyName(company.getCompanyName())
                                                .build()
                                )
                                .collect(Collectors.toSet())
                )
                .build();
    }

    public static VehicleResponseDTO mapToVehicleResponse(Vehicle vehicle) {

        Company company = vehicle.getCompany();

        CompanyResponseDto companyDto = null;

        if (company != null) {
            companyDto = CompanyResponseDto.builder()
                    .companyId(company.getCompanyId())
                    .companyName(company.getCompanyName())
                    .build();
        }

        return VehicleResponseDTO.builder()
                .vehicleId(vehicle.getVehicleId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .ownerName(vehicle.getOwnerName())
                .ownerPhone(vehicle.getOwnerPhone())
                .active(vehicle.getActive())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .company(companyDto) // 🔥 change
                .build();
    }
    public static CompanyVehicleResponseDTO mapToCompanyVehicleResponse(
            Company company,
            List<Vehicle> vehicles
    ) {

        List<VehicleResponseDTO> vehicleDTOList =
                Optional.ofNullable(vehicles)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(DataUtil::mapToVehicleResponse)
                        .toList();

        return CompanyVehicleResponseDTO.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .vehicles(vehicleDTOList)
                .build();
    }

    public static RoleResponseDto mapToRoleResponse(Role role) {

        if (role == null) {
            return null;
        }

        List<PermissionSuperAdminResponseDto> structuredPermissions =
                role.getPermissions() != null
                        ? mapToStructuredPermissions(new ArrayList<>(role.getPermissions()))
                        : Collections.emptyList();

        return RoleResponseDto.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .isDeleted(role.getIsDeleted())
                .active(role.getActive()) // ✅ add
                .createdAt(role.getCreatedAt())// ✅ add
                .companyId(
                        role.getCompany() != null
                                ? role.getCompany().getCompanyId()
                                : null
                ) // ✅ add
                .permissions(structuredPermissions)
                .build();
    }



    public static Set<PermissionResponse> flattenStructuredPermissions(
            List<PermissionSuperAdminResponseDto> structured) {

        return structured.stream()
                .flatMap(superGroup -> Optional.ofNullable(superGroup.getPermissionGroups()).orElse(Collections.emptyList()).stream())
                .flatMap(group -> Optional.ofNullable(group.getPermissionData()).orElse(Collections.emptyList()).stream())
                .map(p -> new PermissionResponse(p.getPermissionId(), p.getPermissionName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<PermissionResponseDto> mapToPermissionResponse(
            Collection<Permission> permissions
    ) {
        if (permissions == null) return Collections.emptySet();

        return permissions.stream()
                .map(permission -> PermissionResponseDto.builder()
                        .permissionId(permission.getPermissionId())
                        .permissionName(permission.getPermissionName())
                        .permissionGroup(permission.getPermissionGroup())
                        .permissionSuperGroup(permission.getPermissionSuperGroup())
                        .build()
                )
                .collect(Collectors.toSet());
    }

    public PermissionResponseDto permissionToDto(Permission permission) {

        return PermissionResponseDto.builder()
                .permissionId(permission.getPermissionId())
                .permissionName(permission.getPermissionName())
                .permissionSuperGroup(permission.getPermissionSuperGroup())
                .permissionGroup(permission.getPermissionGroup())
                .permissionSuperGroup(permission.getPermissionSuperGroup())
//                .actionName(permission.getActionName())
//                .active(permission.getActive())
//                .isDeleted(permission.getIsDeleted())
//                .createdAt(permission.getCreatedAt())
//                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    public List<PermissionResponseDto> permissionsToDtos(List<Permission> permissions) {
        return permissions.stream()
                .map(this::permissionToDto)
                .collect(Collectors.toList());
    }

    public RoyaltyPassResponse toRoyaltyPassResponse(RoyaltyPass royaltyPass) {

        RoyaltyPassResponse dto = new RoyaltyPassResponse();

        dto.setRoyaltyId(royaltyPass.getRoyaltyId());
        dto.setRoyaltyNumber(royaltyPass.getRoyaltyNumber());
        dto.setStatus(royaltyPass.getStatus());
        dto.setDeleted(royaltyPass.getDeleted());

        // ---------------- COMPANY MAPPING ----------------
        Company company = royaltyPass.getCompany();
        if (company != null) {
            dto.setCompanyId(company.getCompanyId());
        }
        return dto;
    }
//    public UserResponse toUserResponse(User user) {
//
//        UserResponse dto = new UserResponse();
//        dto.setUserId(user.getUserId());
//        dto.setPwd(user.getPwd());
//        dto.setUserName(user.getUserName());
//        dto.setEmail(user.getEmail());
//        dto.setFirstName(user.getFirstName());
//        dto.setLastName(user.getLastName());
//        dto.setDob(user.getDob());
//        dto.setMobNo(user.getMobNo());
//        dto.setAddressLine1(user.getAddressLine1());
//        dto.setAddressLine2(user.getAddressLine2());
//        dto.setAddressLine3(user.getAddressLine3());
//
//        // ---------------- COMPANY & ROLE MAPPING ----------------
//        if (user.getUserCompanies() != null && !user.getUserCompanies().isEmpty()) {
//
//            // Collect all company IDs for the user
//            List<Integer> companyIds = user.getUserCompanies().stream()
//                    .filter(uc -> uc.getCompany() != null)
//                    .map(uc -> uc.getCompany().getCompanyId())
//                    .distinct()
//                    .toList();
//            dto.setCompanyIds(companyIds);
//
//            // Collect all role IDs for the user
//            List<Integer> roleIds = user.getUserCompanies().stream()
//                    .filter(uc -> uc.getRole() != null)
//                    .map(uc -> uc.getRole().getRoleId())
//                    .distinct()
//                    .toList();
//            dto.setRoleIds(roleIds);
//        } else {
//            dto.setCompanyIds(Collections.emptyList());
//            dto.setRoleIds(Collections.emptyList());
//        }
//
//        return dto;
//    }

    public UserResponse toUserResponse(User user) {

        UserResponse dto = new UserResponse();

        dto.setUserId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPwd(user.getPwd());
        dto.setDob(user.getDob());
        dto.setMobNo(user.getMobNo());
        dto.setAddressLine1(user.getAddressLine1());
        dto.setAddressLine2(user.getAddressLine2());
        dto.setAddressLine3(user.getAddressLine3());

        // ---------------- COMPANY & ROLE MAPPING ----------------
        if (user.getUserCompanies() != null && !user.getUserCompanies().isEmpty()) {

            // ✅ Companies (id + name)
            List<CompanyResponseDto> companies = user.getUserCompanies().stream()
                    .filter(uc -> uc.getCompany() != null)
                    .map(uc -> CompanyResponseDto.builder()
                            .companyId(uc.getCompany().getCompanyId())
                            .companyName(uc.getCompany().getCompanyName())
                            .build()
                    )
                    .distinct()
                    .toList();

            // ✅ Roles (id + name)
            List<RoleResponseDto> roles = user.getUserCompanies().stream()
                    .filter(uc -> uc.getRole() != null)
                    .map(uc -> RoleResponseDto.builder()
                            .roleId(uc.getRole().getRoleId())
                            .roleName(uc.getRole().getRoleName())
                            .build()
                    )
                    .distinct()
                    .toList();

            dto.setCompanies(companies);
            dto.setRoles(roles);

        } else {
            dto.setCompanies(Collections.emptyList());
            dto.setRoles(Collections.emptyList());
        }

        return dto;
    }
    public static  List<PermissionSuperAdminResponseDto> mapToStructuredPermissions(List<Permission> permissions) {

        Map<String, Map<String, List<Permission>>> grouped =
                permissions.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getActive()))
                        .collect(Collectors.groupingBy(
                                Permission::getPermissionSuperGroup,
                                LinkedHashMap::new,
                                Collectors.groupingBy(
                                        Permission::getPermissionGroup,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        ));

        return grouped.entrySet().stream()
                .map(superGroupEntry -> {

                    List<PermissionGroupDto> groupDtos =
                            superGroupEntry.getValue().entrySet().stream()
                                    .map(groupEntry -> {

                                        List<PermissionDataDto> permissionData =
                                                groupEntry.getValue().stream()
                                                        .map(p -> new PermissionDataDto(
                                                                p.getPermissionId(),
                                                                p.getPermissionName()
                                                        ))
                                                        .toList();

                                        return new PermissionGroupDto(
                                                groupEntry.getKey(),
                                                permissionData
                                        );

                                    }).toList();

                    return new PermissionSuperAdminResponseDto(
                            superGroupEntry.getKey(),
                            groupDtos
                    );

                }).toList();
    }
    public FuelStationResponse toFuelStationResponse(FuelStation fuelStation) {

        FuelStationResponse dto = new FuelStationResponse();
        dto.setFuelStationId(fuelStation.getFuelStationId());
        dto.setFuelStationName(fuelStation.getFuelStationName());
        dto.setState(fuelStation.getState().getStateId());
        dto.setDistrict(fuelStation.getDistrict().getDistrictId());
        dto.setSubDistrict(fuelStation.getSubDistrict());
        dto.setVillage(fuelStation.getVillage());
        dto.setOwnerName(fuelStation.getOwnerName());
        dto.setOwnerMobileNumber(fuelStation.getOwnerMobileNumber());
        dto.setActive(fuelStation.getActive());
        dto.setDeleted(fuelStation.getDeleted());

        // ---------------- COMPANY MAPPING ----------------
        Company company = fuelStation.getCompany();
        if (company != null) {
            dto.setCompanyId(company.getCompanyId());
            dto.setCompanyName(company.getCompanyName());
        }

        return dto;
    }

    public List<PermissionsBasedOnRoleDTO> rolesPermissionToDtos(List<Role> roles) {
        List<PermissionsBasedOnRoleDTO> dtos = new ArrayList<>();

        for (Role role : roles) {
            if (role.getPermissions() != null) {
                for (Permission permission : role.getPermissions()) {
                    PermissionsBasedOnRoleDTO permissionDTO = new PermissionsBasedOnRoleDTO(
                            permission.getPermissionId(),
                            permission.getPermissionGroup(),
                            permission.getPermissionSuperGroup(),
                            permission.getPermissionName()
                    );
                    dtos.add(permissionDTO);
                }
            }
        }
        return dtos;
    }
}


