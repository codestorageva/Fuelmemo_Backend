package com.example.FuelMemo.MasterData.Service.Impl;

import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.MasterData.Config.spring_batch.country_batch.CountryWriter;
import com.example.FuelMemo.MasterData.Dto.CountryDto;
import com.example.FuelMemo.MasterData.Dto.CountryExportDTO;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.CountryUpdateDto;
import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Repository.*;
import com.example.FuelMemo.MasterData.Service.*;
import com.example.FuelMemo.Shared.Exception.DuplicateEntryException;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CountryServiceImpl implements CountryService {

    @Getter
    private static String userName;
    @PersistenceContext
    private final EntityManager entityManager;

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final CountryRepository countryRepository;
    private final DataUtil dateUtil;
    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final SubDistrictRepository subDistrictRepository;
    private final VillageRepository villageRepository;
    private final StateService stateService;
    private final DistrictService districtService;
    private final SubDistrictService subDistrictService;
    private final VillageService villageService;
    private final PermissionService permissionService;


    public CountryServiceImpl(EntityManager entityManager, CountryRepository countryRepository, DataUtil dateUtil,
                              JobLauncher jobLauncher, @Qualifier("countryReaderJob") Job job, StateRepository stateRepository,
                              DistrictRepository districtRepository, SubDistrictRepository subDistrictRepository,
                              VillageRepository villageRepository, StateService stateService, DistrictService districtService,
                              SubDistrictService subDistrictService, VillageService villageService, PermissionService permissionService) throws IOException {
        this.entityManager = entityManager;
        this.countryRepository = countryRepository;
        this.dateUtil = dateUtil;
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
        this.stateService = stateService;
        this.districtService = districtService;
        this.subDistrictService = subDistrictService;
        this.villageService = villageService;
        this.permissionService = permissionService;
    }

    //----------------------------------------------------SERVICE METHODS-----------------------------------------------

    @Override
    public MessageResponse addCountry(CountryDto c, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "WRITE_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {

            // HANDLE DUPLICATE NAME ENTRY EXCEPTION
            if (countryRepository.existsByCountryNameIgnoreCase(c.getCountryName()))
                throw new DuplicateEntryException(
                        "Country with name '" + c.getCountryName() + "' already exist!"
                );

            Country country = new Country();
            country.setCountryName(c.getCountryName().trim());

            if (c.getStatus() != null)
                country.setStatus(c.getStatus());
            else
                country.setStatus(true);

            country.setIsDeleted(false);
            country.setCreatedAt(LocalDateTime.now());
            country.setUpdatedAt(LocalDateTime.now());
            country.setDeletedAt(null);
            country.setOperatorName(loggedInUser);

            countryRepository.save(country);

            return new MessageResponse(true, HttpStatus.OK, "Country saved.");
        } else {
            throw new UserUnauthorizedException(
                    "You don't have permission to perform this operation."
            );
        }
    }


    @Override
    public DataResponse getCountryById(int id, Map<String, String> headers) {

        // Optional fetch
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country with id '" + id + "' not Found!"));

        // Permission check
        String roleName = headers.get("rolename");
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "READ_COUNTRY"
        );

        if (!roleName.equals("ROLE_SUPERADMIN") && !hasPermission) {
            throw new UserUnauthorizedException("You don't have permission to view this country.");
        }

        return new DataResponse(true, HttpStatus.OK, dateUtil.countryToDto(country));
    }


    @Override
    public HttpResponse getAllCountries(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            String isDeleted,
            String status,
            Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call result object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "READ_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {

            Page<Country> pageCountry = null;
            List<CountryDto> countries = null;

            Integer intKeyword = null;
            Boolean softDeleted = null;
            Boolean isStatus = null;

            if ("true".equalsIgnoreCase(isDeleted)) softDeleted = Boolean.TRUE;
            else if ("false".equalsIgnoreCase(isDeleted)) softDeleted = Boolean.FALSE;

            if ("true".equalsIgnoreCase(status)) isStatus = Boolean.TRUE;
            else if ("false".equalsIgnoreCase(status)) isStatus = Boolean.FALSE;

            try {
                intKeyword = Integer.parseInt(keyword);
            } catch (Exception ignored) {
            }

            try {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Country> criteriaQuery = criteriaBuilder.createQuery(Country.class);
                Root<Country> root = criteriaQuery.from(Country.class);

                Order order = sortDirection.equalsIgnoreCase("asc") ?
                        criteriaBuilder.asc(root.get(sortBy)) :
                        criteriaBuilder.desc(root.get(sortBy));
                criteriaQuery.orderBy(order);

                Predicate statusPredicate = isStatus != null ? criteriaBuilder.equal(root.get("status"), isStatus) : null;
                Predicate deletedPredicate = softDeleted != null ? criteriaBuilder.equal(root.get("isDeleted"), softDeleted) : null;
                Predicate countryIdPredicate = intKeyword != null ? criteriaBuilder.equal(root.get("countryId"), intKeyword) : null;
                Predicate countryNamePredicate = (keyword != null && intKeyword == null) ?
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("countryName").as(String.class)), "%" + keyword.toLowerCase() + "%") :
                        null;

                Predicate combinedPredicate = null;

                if (statusPredicate != null) combinedPredicate = statusPredicate;
                if (deletedPredicate != null)
                    combinedPredicate = combinedPredicate != null ? criteriaBuilder.and(combinedPredicate, deletedPredicate) : deletedPredicate;
                if (countryIdPredicate != null)
                    combinedPredicate = combinedPredicate != null ? criteriaBuilder.and(combinedPredicate, countryIdPredicate) : countryIdPredicate;
                if (countryNamePredicate != null)
                    combinedPredicate = combinedPredicate != null ? criteriaBuilder.and(combinedPredicate, countryNamePredicate) : countryNamePredicate;

                criteriaQuery.select(root).where(combinedPredicate);

                Pageable pageable = PageRequest.of(pageNumber, pageSize);

                List<Country> resultList = entityManager.createQuery(criteriaQuery)
                        .setFirstResult((int) pageable.getOffset())
                        .setMaxResults(pageable.getPageSize())
                        .getResultList();

                long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();

                pageCountry = new PageImpl<>(resultList, pageable, totalCount);
                countries = dateUtil.countriesToDtos(resultList);

            } catch (NoResultException ignored) {
            }

            return HttpResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .pageNumber(pageCountry.getNumber())
                    .pageSize(pageCountry.getSize())
                    .totalElements(pageCountry.getTotalElements())
                    .totalPages(pageCountry.getTotalPages())
                    .isLastPage(pageCountry.isLast())
                    .data(countries)
                    .build();

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    public MessageResponse updateCountry(CountryUpdateDto c, int id, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "UPDATE_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {

            Country country;

            // HANDLE IF COUNTRY EXIST BY ID
            country = countryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Country with Id '" + id + "' not found!"));

            // HANDLE DUPLICATE ENTRY EXCEPTION
            if (c.getCountryName() != null) {
                Country countryWithSameName = countryRepository.findByCountryNameIgnoreCase(c.getCountryName().trim());

                if (countryWithSameName != null && !countryWithSameName.getCountryId().equals(country.getCountryId()))
                    throw new DuplicateEntryException("Country '" + c.getCountryName() + "' already exist!");

                country.setCountryName(c.getCountryName().trim());
            }

            if (c.getStatus() != null)
                country.setStatus(c.getStatus());

            country.setUpdatedAt(LocalDateTime.now());
            country.setOperatorName(loggedInUser);

            countryRepository.save(country);

            return new MessageResponse(true, HttpStatus.OK, "Country-Data updated");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    /*
        @Transactional
        @Override
        public MessageResponse deleteCountryById(int id, Map<String, String> headers) {

                String roleName = headers.get("rolename");

                // Define the URL to call the AUTH service
                String url = UriComponentsBuilder.fromHttpUrl(authURL + "/auth/role/checkPermission?permissionName=COUNTRY_DELETE").toUriString();

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
                    Country country = countryRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("Country with Id '" + id + "' not found!"));

                    for (State s : stateRepository.findAllByCountry_CountryId(country.getCountryId()))
                        stateService.deleteStateById(s.getStateId(), headers);
                    for (District d : districtRepository.findAllByCountry_CountryId(country.getCountryId()))
                        districtService.deleteDistrictById(d.getDistrictId(), headers);
                    for (SubDistrict sd : subDistrictRepository.findAllByCountry_CountryId(country.getCountryId()))
                        subDistrictService.deleteSubDistrictById(sd.getSubDistrictId(), headers);
                    for (Village v : villageRepository.findAllByCountry_CountryId(country.getCountryId()))
                        villageService.deleteVillageById(v.getVillageId(), headers);

                    int rowsAffected = 0;

                    try {
                        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                        CriteriaDelete<Country> criteriaDelete = criteriaBuilder.createCriteriaDelete(Country.class);
                        Root<Country> root = criteriaDelete.from(Country.class);

                        criteriaDelete.where(
                                criteriaBuilder.equal(root.get("countryId"), country.getCountryId())
                        );
                        rowsAffected = entityManager.createQuery(criteriaDelete).executeUpdate();
                        System.out.println("Operation Performed Successfully " + rowsAffected + " rows Affected");

                    } catch (Exception e) {
                        System.out.println("Error Occured " + e.getMessage());
                    }

                    if (rowsAffected > 0 || rowsAffected == 1)
                        return new MessageResponse(true, HttpStatus.OK, "Data Deleted");

    //                countryRepository.delete(country);
                    return new MessageResponse(true, HttpStatus.OK, "Country with Id '" + id + "' hard deleted");
                } else {
                    throw new UserUnauthorizedException("You don't have permission to perform this operation.");
                }
        }
    */

    @Override
    public MessageResponse softDeleteCountryById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "DELETE_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {

            Country country = countryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Country with Id '" + id + "' not found!"));

            country.setIsDeleted(true);
            country.setDeletedAt(LocalDateTime.now());
            countryRepository.save(country);

            return new MessageResponse(true, HttpStatus.OK, "Country with Id '" + id + "' Soft Deleted");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public MessageResponse restoreCountryById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "RESTORE_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {

            Country country = countryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Country with Id '" + id + "' not found!"));

            country.setIsDeleted(false);
            country.setDeletedAt(null);
            countryRepository.save(country);

            return new MessageResponse(true, HttpStatus.OK, "Country with id '" + id + "' restored!");

        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public ListResponse getAllCountriesByStatusAndIsDeleted(Map<String, String> headers) {

        List<CountryDto> countries = null;
        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "READ_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            try {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Country> criteriaQuery = criteriaBuilder.createQuery(Country.class);
                Root<Country> root = criteriaQuery.from(Country.class);

                Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), true);
                Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);

                criteriaQuery.select(root)
                        .where(statusPredicate, deletedPredicate)
                        .orderBy(criteriaBuilder.asc(root.get("countryName")));

                List<Country> resultList = entityManager.createQuery(criteriaQuery).getResultList();
                countries = dateUtil.countriesToDtos(resultList);

            } catch (NoResultException ignored) {
            }

            return new ListResponse(true, HttpStatus.OK, countries);
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public ListResponse exportedCountryData(Map<String, String> headers) {

        List<CountryExportDTO> countries = new ArrayList<>();
        String roleName = headers.get("rolename");

        // ✅ Fix: use boolean instead of REST call object
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "EXPORT_COUNTRY"
        );

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            try {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Country> criteriaQuery = criteriaBuilder.createQuery(Country.class);
                Root<Country> root = criteriaQuery.from(Country.class);

                criteriaQuery.select(root)
                        .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                        .orderBy(criteriaBuilder.asc(root.get("countryName")));

                List<Country> resultList = entityManager.createQuery(criteriaQuery).getResultList();
                countries = dateUtil.countryExportDTOStoCountries(resultList);

            } catch (NoResultException ignored) {
            }

            return new ListResponse(true, HttpStatus.OK, countries);
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public LongResponse getTotalCountry() {
        Long count = countryRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }

    @Override
    public MessageResponse uploadCountryCSV(MultipartFile file, Map<String, String> headers) {
        String loggedInUser = headers.get("loggedinuser");
        String email = headers.get("email"); // Get email from headers
        String roleName = headers.get("rolename");

        // Log info for debugging
        System.out.println("Uploading CSV by user: " + loggedInUser + " | Email: " + email + " | Role: " + roleName);

        // ✅ Permission check
        boolean hasPermission = false;
        try {
            if ("ROLE_SUPERADMIN".equalsIgnoreCase(roleName)) {
                hasPermission = true; // superadmin always allowed
            } else {
                hasPermission = permissionService.hasPermission(email, "IMPORT_COUNTRY");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResponse(false, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error checking permissions: " + e.getMessage());
        }

        if (!hasPermission) {
            return new MessageResponse(false, HttpStatus.FORBIDDEN,
                    "You don't have permission to perform this operation.");
        }

        // ✅ File validation
        if (file == null || file.isEmpty()) {
            return new MessageResponse(false, HttpStatus.BAD_REQUEST, "CSV File is not uploaded or empty.");
        }

        String contentType = file.getContentType();
        if (!Objects.equals(contentType, "text/csv") && !dateUtil.isSupportedExtensionBatch(file.getOriginalFilename())) {
            return new MessageResponse(false, HttpStatus.BAD_REQUEST, "Invalid file type. Only CSV or Excel is allowed.");
        }

        try {
            // Temporary file creation
            File tempFile = File.createTempFile(
                    LocalDate.now().format(dateFormatter) + "_" +
                            LocalTime.now().format(timeFormatter) + "_Country_" + "temp",
                    ".csv"
            );

            file.transferTo(tempFile);

            // Pass logged-in user for auditing
            userName = loggedInUser;

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFile", tempFile.getAbsolutePath())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            long recordsUploaded = 0;
            if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                recordsUploaded = CountryWriter.getCounter();
                System.out.println("CSV Job Completed. Records uploaded: " + recordsUploaded);
                // Clean temp file
                if (tempFile.exists() && !tempFile.delete()) {
                    System.out.println("Warning: Temporary file could not be deleted");
                }
                CountryWriter.setCounter(0);
            }

            return new MessageResponse(true, HttpStatus.OK,
                    "CSV file uploaded successfully. " + recordsUploaded + " countries uploaded.");

        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResponse(false, HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error processing CSV: " + e.getMessage());
        }
    }

}