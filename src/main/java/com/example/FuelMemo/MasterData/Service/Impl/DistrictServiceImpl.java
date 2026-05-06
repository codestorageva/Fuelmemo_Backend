package com.example.FuelMemo.MasterData.Service.Impl;

import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.MasterData.Config.spring_batch.district_batch.DistrictProcessor;
import com.example.FuelMemo.MasterData.Config.spring_batch.district_batch.DistrictWriter;
import com.example.FuelMemo.MasterData.Dto.DistrictDto;
import com.example.FuelMemo.MasterData.Dto.DistrictExportDTO;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.DistrictUpdateDto;
import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.*;
import com.example.FuelMemo.MasterData.Service.DistrictService;
import com.example.FuelMemo.MasterData.Service.SubDistrictService;
import com.example.FuelMemo.MasterData.Service.VillageService;
import com.example.FuelMemo.Shared.Exception.DuplicateEntryException;
import com.example.FuelMemo.Shared.Exception.InactiveStatusException;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
import com.example.FuelMemo.Shared.Exception.UserUnauthorizedException;
import com.example.FuelMemo.Shared.Response.*;
import com.example.FuelMemo.Shared.Util.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.Getter;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DistrictServiceImpl implements DistrictService {
    @Getter
    private static String userName;
    @PersistenceContext
    private final EntityManager entityManager;
    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final DistrictRepository districtRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private final DataUtil dateUtil;
    private final SubDistrictRepository subDistrictRepository;
    private final VillageRepository villageRepository;
    private final SubDistrictService subDistrictService;
    private final VillageService villageService;
    private final PermissionService permissionService;


    @Autowired
    public DistrictServiceImpl(EntityManager entityManager, DistrictRepository districtRepository, StateRepository stateRepository,
                               CountryRepository countryRepository,
                               JobLauncher jobLauncher, @Qualifier("districtReaderJob") Job job,
                               DataUtil dateUtil, SubDistrictRepository subDistrictRepository, VillageRepository villageRepository,
                               SubDistrictService subDistrictService, VillageService villageService, PermissionService permissionService) {
        this.entityManager = entityManager;
        this.districtRepository = districtRepository;
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.dateUtil = dateUtil;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
        this.subDistrictService = subDistrictService;
        this.villageService = villageService;
        this.permissionService = permissionService;
    }

    //----------------------------------------------------SERVICE METHODS-----------------------------------------------
    @Override
    public MessageResponse addDistrict(DistrictDto districtDto, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "WRITE_DISTRICT"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {

            districtDto.setCityName(districtDto.getCityName().trim());

            // HANDLE IF STATE EXIST BY ID
            State state = stateRepository.findById(districtDto.getStateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "State with Id '" + districtDto.getStateId() + "' not found!"
                    ));

            String stateName = state.getStateName();

            // HANDLE IF DUPLICATE DISTRICT NAME
            if (districtRepository.existsByCityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                    districtDto.getCityName(),
                    stateName,
                    state.getCountry().getCountryName()
            )) {
                throw new DuplicateEntryException(
                        "District with name '" + districtDto.getCityName() + "' already exist!"
                );
            }

            // IS STATE STATUS ACTIVE?
            if (!state.getStatus())
                throw new InactiveStatusException(
                        "State with id '" + districtDto.getStateId() + "' is not active!"
                );

            District district = new District();
            district.setCityName(districtDto.getCityName());
            district.setApiCityName(districtDto.getApiCityName());
            district.setCountry(state.getCountry());
            district.setState(state);

            if (districtDto.getStatus() != null)
                district.setStatus(districtDto.getStatus());
            else
                district.setStatus(true);

            district.setIsDeleted(false);
            district.setCreatedAt(LocalDateTime.now());
            district.setUpdatedAt(LocalDateTime.now());
            district.setDeletedAt(null);
            district.setOperatorName(loggedInUser);

            districtRepository.save(district);

            return new MessageResponse(true, HttpStatus.OK, "District Saved");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public DataResponse getDistrictById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "READ_DISTRICT"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            District district = districtRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "District with id '" + id + "' not found!"
                    ));

            return new DataResponse(true, HttpStatus.OK, dateUtil.districtToDto(district));

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public HttpResponse getAllDistricts(String keyword,
                                        int pageNumber,
                                        int pageSize,
                                        String sortBy,
                                        String sortDirection,
                                        String isDeleted,
                                        String status,
                                        String authorization
                                        ) {

        // ================= PERMISSION CHECK =================
        if (authorization == null) {
            throw new UserUnauthorizedException("Required headers missing.");
        }

        boolean hasPermission = permissionService.hasPermission(
                authorization,
                "READ_DISTRICT"
        );

//        if (!roleName.equalsIgnoreCase("ROLE_SUPERADMIN") && !hasPermission) {
//            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
//        }

        // ================= BOOLEAN CONVERSION =================
        Boolean softDeleted = "true".equalsIgnoreCase(isDeleted);
        Boolean isStatus = "true".equalsIgnoreCase(status);

        Integer intKeyword = null;
        try {
            if (keyword != null) {
                intKeyword = Integer.parseInt(keyword);
            }
        } catch (Exception ignored) {}

        // ================= CRITERIA BUILDER =================
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<District> cq = cb.createQuery(District.class);
        Root<District> root = cq.from(District.class);

        Join<District, State> stateJoin = root.join("state", JoinType.INNER);
        Join<State, Country> countryJoin = stateJoin.join("country", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        // Mandatory filters
        predicates.add(cb.equal(root.get("isDeleted"), softDeleted));
        predicates.add(cb.equal(root.get("status"), isStatus));
        predicates.add(cb.equal(stateJoin.get("isDeleted"), false));
        predicates.add(cb.equal(stateJoin.get("status"), true));
        predicates.add(cb.equal(countryJoin.get("isDeleted"), false));
        predicates.add(cb.equal(countryJoin.get("status"), true));

        // Keyword filter
        if (intKeyword != null) {
            predicates.add(cb.equal(root.get("districtId"), intKeyword));
        } else if (keyword != null && !keyword.isBlank()) {
            predicates.add(
                    cb.or(
                            cb.like(cb.lower(root.get("cityName")),
                                    "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(stateJoin.get("stateName")),
                                    "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(countryJoin.get("countryName")),
                                    "%" + keyword.toLowerCase() + "%")
                    )
            );
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // ================= SORTING =================
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "updatedAt";
        }

        Order order = "desc".equalsIgnoreCase(sortDirection)
                ? cb.desc(root.get(sortBy))
                : cb.asc(root.get(sortBy));

        cq.orderBy(order);

        // ================= PAGINATION =================
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<District> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ================= COUNT QUERY =================
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<District> countRoot = countQuery.from(District.class);
        Join<District, State> countStateJoin = countRoot.join("state", JoinType.INNER);
        Join<State, Country> countCountryJoin = countStateJoin.join("country", JoinType.INNER);

        List<Predicate> countPredicates = new ArrayList<>();

        countPredicates.add(cb.equal(countRoot.get("isDeleted"), softDeleted));
        countPredicates.add(cb.equal(countRoot.get("status"), isStatus));
        countPredicates.add(cb.equal(countStateJoin.get("isDeleted"), false));
        countPredicates.add(cb.equal(countStateJoin.get("status"), true));
        countPredicates.add(cb.equal(countCountryJoin.get("isDeleted"), false));
        countPredicates.add(cb.equal(countCountryJoin.get("status"), true));

        if (intKeyword != null) {
            countPredicates.add(cb.equal(countRoot.get("districtId"), intKeyword));
        } else if (keyword != null && !keyword.isBlank()) {
            countPredicates.add(
                    cb.or(
                            cb.like(cb.lower(countRoot.get("cityName")),
                                    "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(countStateJoin.get("stateName")),
                                    "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(countCountryJoin.get("countryName")),
                                    "%" + keyword.toLowerCase() + "%")
                    )
            );
        }

        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        Page<District> pageDistrict =
                new PageImpl<>(resultList, pageable, totalCount);

        List<DistrictDto> districts = dateUtil.districtsToDtos(resultList);

        // ================= RESPONSE =================
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageDistrict.getNumber())
                .pageSize(pageDistrict.getSize())
                .totalElements(pageDistrict.getTotalElements())
                .totalPages(pageDistrict.getTotalPages())
                .isLastPage(pageDistrict.isLast())
                .data(districts)
                .build();
    }
    @Override
    public MessageResponse updateDistrict(DistrictUpdateDto districtDto, int id, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "UPDATE_DISTRICT"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            // HANDLE IF DISTRICT EXIST BY ID
            District district = districtRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "District with id '" + id + "' not found!"
                    ));

            String stateName = district.getState().getStateName();
            String countryName = district.getCountry().getCountryName();

            // HANDLE IF DUPLICATE DISTRICT NAME
            if (districtDto.getCityName() != null) {

                if (districtDto.getStateId() != null) {
                    State state = stateRepository.findById(districtDto.getStateId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "State with ID '" + districtDto.getStateId() + "' not found!"
                            ));

                    stateName = state.getStateName();
                    countryName = state.getCountry().getCountryName();
                    district.setState(state);
                }

                District districtWithSameName = districtRepository.findByCityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                        districtDto.getCityName().trim(),
                        stateName.trim(),
                        countryName.trim()
                );

                if (districtWithSameName != null && !districtWithSameName.getDistrictId().equals(district.getDistrictId()))
                    throw new DuplicateEntryException(
                            "District '" + districtDto.getCityName() + "' already exist!"
                    );

                district.setCityName(districtDto.getCityName().trim());
            }

            if (districtDto.getStatus() != null)
                district.setStatus(districtDto.getStatus());
            if (districtDto.getApiCityName() != null) {
                district.setApiCityName(districtDto.getApiCityName().trim());
            }

            district.setUpdatedAt(LocalDateTime.now());
            district.setOperatorName(loggedInUser);

            districtRepository.save(district);

            return new MessageResponse(true, HttpStatus.OK, "District updated.");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


/*
    @Transactional
    @Override
    public MessageResponse deleteDistrictById(int id, Map<String, String> headers) {
        try {
            String roleName = headers.get("rolename");

            // Define the URL to call the AUTH service
            String url = UriComponentsBuilder.fromHttpUrl("http://" + hostname + "/auth/role/checkPermission?permissionName=DISTRICT_DELETE").toUriString();

            // Create headers and add the Authorization token
            HttpHeaders header = new HttpHeaders();
            header.set(HttpHeaders.AUTHORIZATION, headers.get("authorization"));

            // Create the HttpEntity with headers
            HttpEntity<String> entity = new HttpEntity<>(header);

            // Make a REST call to AUTH service to validate the token
            ResponseEntity<BooleanResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BooleanResponse.class
            );

            BooleanResponse checkPermissionRes = response.getBody();

            assert checkPermissionRes != null;

            if (roleName.equals("ROLE_SUPERADMIN") || checkPermissionRes.getIsPermission().equals(true)) {
                District district = districtRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("District with id '" + id + "' not found!"));

                for (SubDistrict sd : subDistrictRepository.findAllByDistrict_DistrictId(district.getDistrictId()))
                    subDistrictService.deleteSubDistrictById(sd.getSubDistrictId(), headers);
                for (Village v : villageRepository.findAllByDistrict_DistrictId(district.getDistrictId()))
                    villageService.deleteVillageById(v.getVillageId(), headers);

                int rowsAffected = 0;

                try {
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    CriteriaDelete<District> criteriaDelete = criteriaBuilder.createCriteriaDelete(District.class);
                    Root<District> root = criteriaDelete.from(District.class);

                    criteriaDelete.where(
                            criteriaBuilder.equal(root.get("districtId"), district.getDistrictId())
                    );
                    rowsAffected = entityManager.createQuery(criteriaDelete).executeUpdate();
                    System.out.println("Operation Performed Successfully " + rowsAffected + " rows Affected");

                } catch (Exception e) {
                    System.out.println("Error Occurred " + e.getMessage());
                    System.out.println(e);
                }

                if (rowsAffected > 0 || rowsAffected == 1)
                    return new MessageResponse(true, HttpStatus.OK, "Data Deleted");

//                districtRepository.delete(district);
                return new MessageResponse(true, HttpStatus.OK, "District with id '" + id + "' deleted!");
            } else {
                throw new UserUnauthorizedException("You don't have permission to perform this operation.");
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Due to multiple login you've been logged out automatically by system")) {
                // Handle the specific unauthorized error
                throw new LoggedOutException("You have been logged out automatically. Please generate a new token and log in again.");
            } else {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }
*/

    @Override
    public MessageResponse softDeleteDistrictById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "DELETE_DISTRICT"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            District district = districtRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "District with id '" + id + "' not found!"
                    ));

            district.setIsDeleted(true);
            district.setDeletedAt(LocalDateTime.now());
            districtRepository.save(district);

            return new MessageResponse(true, HttpStatus.OK, "District with id '" + id + "' soft deleted!");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public MessageResponse restoreDistrictById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "RESTORE_DISTRICT"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            District district = districtRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "District with id '" + id + "' not found!"
                    ));

            district.setIsDeleted(false);
            district.setDeletedAt(null);
            districtRepository.save(district);

            return new MessageResponse(true, HttpStatus.OK, "District with id '" + id + "' restored!");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public MessageListResponse uploadDistrictCSV(MultipartFile file, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String email = headers.get("email"); // Extract email from headers or token
        String roleName = headers.get("rolename");

        System.out.println("Uploading District CSV by user: " + loggedInUser + " | Email: " + email + " | Role: " + roleName);

        // ✅ Permission check
        boolean hasPermission = false;
        try {
            if ("ROLE_SUPERADMIN".equalsIgnoreCase(roleName)) {
                hasPermission = true; // superadmin bypass
            } else {
                hasPermission = permissionService.hasPermission(email, "IMPORT_DISTRICT");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return MessageListResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error checking permissions: " + e.getMessage())
                    .build();
        }

        if (!hasPermission) {
            return MessageListResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.FORBIDDEN)
                    .message("You don't have permission to perform this operation.")
                    .build();
        }

        // ✅ File validation
        if (file == null || file.isEmpty()) {
            return MessageListResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.BAD_REQUEST)
                    .message("CSV File is not uploaded or empty.")
                    .build();
        }

        if (!Objects.equals(file.getContentType(), "text/csv") &&
                !dateUtil.isSupportedExtensionBatch(file.getOriginalFilename())) {
            return MessageListResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.BAD_REQUEST)
                    .message("Invalid file type. Only CSV or Excel is allowed.")
                    .build();
        }

        try {
            File tempFile = File.createTempFile(
                    LocalDate.now().format(dateFormatter) + "_" +
                            LocalTime.now().format(timeFormatter) + "_District_" + "temp",
                    ".csv"
            );

            file.transferTo(tempFile);
            userName = loggedInUser; // For audit

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFileDistrict", tempFile.getAbsolutePath())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            long recordsUploaded = 0;
            long failedRecords = 0;

            if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                System.out.println("District CSV Job Completed...");
                recordsUploaded = DistrictWriter.getInsertedCounter();
                failedRecords = DistrictWriter.getFailedCounter();

                // Clean temp file
                if (tempFile.exists() && !tempFile.delete()) {
                    System.out.println("Warning: Temporary file could not be deleted");
                }

                // Reset counters
                DistrictWriter.setInsertedCounter(0);
                DistrictWriter.setFailedCounter(0);
            }

            // ✅ Response logic
            if (DistrictProcessor.getDistricts() == null) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("You already have all districts in our records.")
                        .build();
            } else if (recordsUploaded == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("No records uploaded. " + failedRecords +
                                " districts failed due to missing States or Countries.")
                        .data(DistrictProcessor.getDistricts())
                        .build();
            } else if (failedRecords == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("All (" + recordsUploaded + ") district data uploaded successfully.")
                        .build();
            } else {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message(recordsUploaded + " district data partially uploaded. " +
                                failedRecords + " districts failed due to missing States or Countries.")
                        .data(DistrictProcessor.getDistricts())
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return MessageListResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Error processing CSV: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public LongResponse getTotalDistrict() {
        Long count = districtRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }

    // ------------------------
    // Fetch districts by state & active status
    // ------------------------
    @Override
    public ListResponse getAllDistrictsByStatusAndIsDeleted(int stateId, Map<String, String> headers) {
        List<DistrictDto> districts = null;

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<District> cq = cb.createQuery(District.class);
            Root<District> root = cq.from(District.class);

            Predicate stateIdPredicate = cb.equal(root.get("state").get("stateId"), stateId);
            Predicate statusPredicate = cb.equal(root.get("status"), true);
            Predicate deletedPredicate = cb.equal(root.get("isDeleted"), false);

            cq.select(root)
                    .where(stateIdPredicate, statusPredicate, deletedPredicate)
                    .orderBy(cb.asc(root.get("cityName")));

            List<District> resultList = entityManager.createQuery(cq).getResultList();
            districts = dateUtil.districtsToDtos(resultList);

        } catch (NoResultException ignored) {
            // districts remains null
        }

        return new ListResponse(true, HttpStatus.OK, districts);
    }

    @Override
    public ListResponse exportedDistrictData(Map<String, String> headers) {
        List<DistrictExportDTO> districts = new ArrayList<>();

        String roleName = headers.get("rolename");

        // ----------- Permission check (no REST call) -----------
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "EXPORT_DISTRICT");


        if (!hasPermission) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        // ---------------- Fetch districts ----------------
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<District> cq = cb.createQuery(District.class);
            Root<District> root = cq.from(District.class);

            cq.select(root)
                    .where(cb.equal(root.get("isDeleted"), false))
                    .orderBy(cb.asc(root.get("cityName")));

            List<District> resultList = entityManager.createQuery(cq).getResultList();
            districts = dateUtil.districtExportDTOStoDistricts(resultList);

        } catch (NoResultException ignored) {
            // districts remains empty
        }

        return new ListResponse(true, HttpStatus.OK, districts);
    }


}