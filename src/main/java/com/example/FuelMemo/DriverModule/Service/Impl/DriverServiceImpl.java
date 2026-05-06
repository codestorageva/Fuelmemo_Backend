package com.example.FuelMemo.DriverModule.Service.Impl;

import com.example.FuelMemo.CompanyModule.Dto.Response.CompanyResponseDto;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.DriverModule.Dto.DriverRequestDto;
import com.example.FuelMemo.DriverModule.Dto.DriverResponseDto;
import com.example.FuelMemo.DriverModule.Dto.DriverUpdateDto;
import com.example.FuelMemo.DriverModule.Entity.Driver;
import com.example.FuelMemo.DriverModule.Repository.DriverRepository;
import com.example.FuelMemo.DriverModule.Service.DriverService;
import com.example.FuelMemo.Shared.Exception.BadRequestException;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DriverServiceImpl implements DriverService {

    private final CompanyRepository companyRepository;

    public DriverServiceImpl(CompanyRepository companyRepository, DriverRepository driverRepository, EntityManager entityManager, DataUtil dataUtil) {
        this.companyRepository = companyRepository;
        this.driverRepository = driverRepository;
        this.entityManager = entityManager;
        this.dataUtil = dataUtil;
    }

    private final DriverRepository driverRepository;
    private final EntityManager entityManager;
    private final DataUtil dataUtil;

    // ================= CREATE DRIVER =================
    @Override
    public MessageResponse createDriver(DriverRequestDto dto) {

        if (dto == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        if (dto.getDriverName() == null || dto.getDriverName().isBlank()) {
            throw new BadRequestException("Driver name is required");
        }

        if (dto.getCompanyId() == null) {
            throw new BadRequestException("Company ID is required");
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        //  Check existing driver
        Driver driver = driverRepository.findByDriverNameIgnoreCase(dto.getDriverName().trim());

        if (driver == null) {
            // Driver doesn't exist, create new
            driver = new Driver();
            driver.setDriverName(dto.getDriverName().trim());
        }

        if (driver == null) {
            // New driver
            driver = new Driver();
            driver.setDriverName(dto.getDriverName().trim());
        }

        // Check already linked
        if (driver.getCompanies().contains(company)) {
            throw new BadRequestException("Driver already assigned to this company");
        }

        driver.getCompanies().add(company);

        driverRepository.save(driver);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.CREATED)
                .message("Driver created successfully")
                .build();
    }

    // ================= UPDATE DRIVER =================

    @Override
    public MessageResponse updateDriver(Integer driverId, DriverUpdateDto dto) {

        if (driverId == null) {
            throw new BadRequestException("Driver ID is required");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        if (driver.getIsDeleted()) {
            throw new BadRequestException("Cannot update deleted driver");
        }

        if (dto == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        // Update name
        if (dto.getDriverName() != null && !dto.getDriverName().isBlank()) {
            driver.setDriverName(dto.getDriverName().trim());
        }

        // Flexible handling of companyId
        if (dto.getCompanyId() != null) {

            List<Integer> companyIds;

            if (dto.getCompanyId() instanceof Integer) {
                companyIds = List.of((Integer) dto.getCompanyId()); // single value → list
            } else if (dto.getCompanyId() instanceof List) {
                companyIds = (List<Integer>) dto.getCompanyId();   // already list
            } else {
                throw new BadRequestException("Invalid companyId format");
            }

            for (Integer cid : companyIds) {
                Company company = companyRepository.findById(cid)
                        .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + cid));

                if (!driver.getCompanies().contains(company)) {
                    driver.getCompanies().add(company);
                }
            }
        }

        driverRepository.save(driver);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Driver updated successfully")
                .build();
    }

    // ================= SOFT DELETE DRIVER =================

    @Override
    public MessageResponse deleteDriver(Integer driverId, Integer companyId) {

        if (driverId == null || companyId == null) {
            throw new BadRequestException("Driver ID and Company ID are required");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with id: " + driverId)
                );

        if (driver.getIsDeleted() != null && driver.getIsDeleted()) {
            throw new BadRequestException("Driver already deleted");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (!driver.getCompanies().contains(company)) {
            throw new BadRequestException("Driver not assigned to this company");
        }

        //  Do NOT remove from join table
        // driver.getCompanies().remove(company); // <-- remove this

        //  Soft delete the driver by setting isDeleted = true
        driver.setIsDeleted(true);

        driverRepository.save(driver);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Driver marked as deleted")
                .build();
    }

    // ================= RESTORE DRIVER =================

    @Override
    public MessageResponse restoreDriver(Integer driverId, Integer companyId) {

        if (driverId == null || companyId == null) {
            throw new BadRequestException("Driver ID and Company ID are required");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with id: " + driverId)
                );

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        //  Restore driver only if soft-deleted
        if (driver.getIsDeleted() != null && driver.getIsDeleted()) {
            driver.setIsDeleted(false);
            driver.setActive(true); // optional, if you have active flag
        }

        //  Attach company if not already assigned
        if (!driver.getCompanies().contains(company)) {
            driver.getCompanies().add(company);
        }

        driverRepository.save(driver);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Driver restored successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DataResponse getDriverById(Integer driverId, Integer companyId) {

        if (driverId == null || companyId == null) {
            throw new BadRequestException("Driver ID and Company ID are required");
        }

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Driver not found with id: " + driverId)
                );

        // ✅ Company validation
        boolean belongsToCompany = driver.getCompanies().stream()
                .anyMatch(c -> c.getCompanyId().equals(companyId));

        if (!belongsToCompany) {
            throw new BadRequestException("Driver does not belong to this company");
        }

        DriverResponseDto responseDto = DataUtil.mapToDriverResponse(driver);

        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(responseDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HttpResponse getAllDrivers(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted) {

        if (companyId == null) {
            throw new BadRequestException("Company ID is required");
        }

        // Validate company exists
        companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found with id: " + companyId)
                );

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Driver> cq = cb.createQuery(Driver.class);
        Root<Driver> root = cq.from(Driver.class);

        // Join with companies
        Join<Object, Object> companyJoin = root.join("companies", JoinType.INNER);
        root.fetch("companies", JoinType.LEFT);

        // Build predicates dynamically
        List<Predicate> predicates = buildDynamicDriverPredicates(cb, root, keyword, isDeleted);
        predicates.add(cb.equal(companyJoin.get("companyId"), companyId));

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        // Sorting
        Path<?> sortPath;
        try {
            sortPath = root.get(sortBy);
        } catch (Exception e) {
            sortPath = root.get("driverId");
        }

        Order order = sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(sortPath)
                : cb.desc(sortPath);

        cq.orderBy(order);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Driver> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Driver> countRoot = countQuery.from(Driver.class);
        Join<Object, Object> countJoin = countRoot.join("companies", JoinType.INNER);

        List<Predicate> countPredicates = buildDynamicDriverPredicates(cb, countRoot, keyword, isDeleted);
        countPredicates.add(cb.equal(countJoin.get("companyId"), companyId));

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        Page<Driver> page = new PageImpl<>(resultList, pageable, totalCount);

        List<DriverResponseDto> drivers = page.getContent()
                .stream()
                .map(dataUtil::mapToDriverResponseDTO)
                .toList();

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .data(drivers)
                .build();
    }

    // Helper method for dynamic predicates
    private List<Predicate> buildDynamicDriverPredicates(
            CriteriaBuilder cb,
            Root<Driver> root,
            String keyword,
            Boolean isDeleted) {

        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            predicates.add(
                    cb.like(
                            cb.lower(root.get("driverName")),
                            "%" + keyword.toLowerCase() + "%"
                    )
            );
        }

        if (isDeleted != null) {
            predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
        }

        return predicates;
    }

    @Override
    @Transactional(readOnly = true)
    public HttpResponse getDriversByCompanyId(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Driver> cq = cb.createQuery(Driver.class);
        Root<Driver> root = cq.from(Driver.class);

        // LEFT JOIN with companies to include drivers without a company
        Join<Object, Object> companyJoin = root.join("companies", JoinType.LEFT);
        root.fetch("companies", JoinType.LEFT);

        // Build predicates dynamically
        List<Predicate> predicates = buildDynamicDriverPredicates(cb, root, keyword, isDeleted);

        // Company filter: include drivers without a company
        if (companyId != null) {
            predicates.add(cb.or(
                    cb.isNull(companyJoin.get("companyId")),  // driver without company
                    cb.equal(companyJoin.get("companyId"), companyId) // driver linked to this company
            ));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        // Sorting
        Path<?> sortPath;
        try {
            sortPath = root.get(sortBy);
        } catch (IllegalArgumentException e) {
            sortPath = root.get("driverId");
        }
        Order order = sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(sortPath)
                : cb.desc(sortPath);
        cq.orderBy(order);

        // Pagination
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<Driver> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Driver> countRoot = countQuery.from(Driver.class);
        Join<Object, Object> countJoin = countRoot.join("companies", JoinType.LEFT);

        List<Predicate> countPredicates = buildDynamicDriverPredicates(cb, countRoot, keyword, isDeleted);
        if (companyId != null) {
            countPredicates.add(cb.or(
                    cb.isNull(countJoin.get("companyId")),
                    cb.equal(countJoin.get("companyId"), companyId)
            ));
        }

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        // Map to DTOs
        Page<Driver> page = new PageImpl<>(resultList, pageable, totalCount);
        List<DriverResponseDto> drivers = page.getContent()
                .stream()
                .map(dataUtil::mapToDriverResponseDTO)
                .toList();

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Data fetched successfully")
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .data(drivers)
                .build();
    }
}