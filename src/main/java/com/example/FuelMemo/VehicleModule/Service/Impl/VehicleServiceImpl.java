package com.example.FuelMemo.VehicleModule.Service.Impl;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import com.example.FuelMemo.VehicleModule.Dto.CompanyVehicleResponseDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleCreateDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleResponseDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleUpdateDTO;
import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import com.example.FuelMemo.VehicleModule.Repository.VehicleRepository;
import com.example.FuelMemo.VehicleModule.Service.VehicleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final EntityManager entityManager;
    private final DataUtil dataUtil;

//    @Override
//    public MessageResponse createVehicle(VehicleCreateDTO dto) {
//
//        Vehicle vehicle = new Vehicle();
//        vehicle.setVehicleNumber(dto.getVehicleNumber());
//        vehicle.setOwnerName(dto.getOwnerName());
//        vehicle.setOwnerPhone(dto.getOwnerPhone());
//        vehicle.setActive(true);
//        vehicle.setDeleted(false);
//
//        // 🔥 Fetch companies
//        Set<Company> companies = new HashSet<>(
//                companyRepository.findAllById(dto.getCompanyIds())
//        );
//
//        if (companies.isEmpty()) {
//            throw new RuntimeException("No valid companies found");
//        }
//
//        vehicle.setCompanies(companies);
//
//        // maintain both side relation
//        companies.forEach(company ->
//                company.getVehicles().add(vehicle)
//        );
//
//        vehicleRepository.save(vehicle);
//
//        return MessageResponse.builder()
//                .success(true)
//                .successCode(HttpStatus.CREATED)
//                .message("Vehicle created successfully")
//                .build();
//    }

    @Override
    public MessageResponse createVehicle(VehicleCreateDTO dto) {

        // ✅ 1. Basic Validation
        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        if (dto.getVehicleNumber() == null || dto.getVehicleNumber().isBlank()) {
            throw new IllegalArgumentException("Vehicle number is required");
        }

        // ✅ 2. Company Fetch
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Company not found with id: " + dto.getCompanyId())
                );

        // ✅ 3. Duplicate Vehicle Check (IMPORTANT 🔥)
        boolean exists = vehicleRepository
                .existsByVehicleNumberAndCompanyCompanyIdAndDeletedFalse(
                        dto.getVehicleNumber().trim(),
                        dto.getCompanyId()
                );

        if (exists) {
            throw new IllegalArgumentException("Vehicle already exists in this company");
        }

        // ✅ 4. Create Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(dto.getVehicleNumber().trim());
        vehicle.setOwnerName(dto.getOwnerName() != null ? dto.getOwnerName().trim() : null);
        vehicle.setOwnerPhone(dto.getOwnerPhone() != null ? dto.getOwnerPhone().trim() : null);
        vehicle.setActive(true);
        vehicle.setDeleted(false);
        vehicle.setCompany(company);

        // ✅ 5. Save
        vehicleRepository.save(vehicle);

        // ✅ 6. Response
        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.CREATED)
                .message("Vehicle created successfully")
                .build();
    }

    @Override
    public MessageResponse updateVehicle(Integer vehicleId, VehicleUpdateDTO dto) {

        // ✅ 1. Validation
        if (dto.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }

        // ✅ 2. Fetch Vehicle
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (Boolean.TRUE.equals(vehicle.getDeleted())) {
            throw new IllegalArgumentException("Cannot update deleted vehicle");
        }

        // ✅ 3. Company Check (MOST IMPORTANT 🔥)
        if (!vehicle.getCompany().getCompanyId().equals(dto.getCompanyId())) {
            throw new IllegalArgumentException("Vehicle does not belong to this company");
        }

        // ✅ 4. Duplicate Check (if vehicle number बदलાય)
        if (dto.getVehicleNumber() != null && !dto.getVehicleNumber().isBlank()) {

            String newNumber = dto.getVehicleNumber().trim().toUpperCase();

            boolean exists = vehicleRepository
                    .existsByVehicleNumberAndCompanyCompanyIdAndDeletedFalseAndVehicleIdNot(
                            newNumber,
                            dto.getCompanyId(),
                            vehicleId
                    );

            if (exists) {
                throw new IllegalArgumentException("Vehicle number already exists in this company");
            }

            vehicle.setVehicleNumber(newNumber);
        }

        // ✅ 5. Other Fields
        if (dto.getOwnerName() != null)
            vehicle.setOwnerName(dto.getOwnerName().trim());

        if (dto.getOwnerPhone() != null)
            vehicle.setOwnerPhone(dto.getOwnerPhone().trim());

        // ❌ Company change avoid (recommended)
        // vehicle.setCompany(...) → normally NO

        // ✅ 6. Save
        vehicleRepository.save(vehicle);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Vehicle updated successfully")
                .build();
    }
    public MessageResponse deleteVehicle(Integer vehicleId, Integer companyId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getCompany().getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Vehicle does not belong to this company");
        }

        if (Boolean.TRUE.equals(vehicle.getDeleted())) {
            throw new IllegalArgumentException("Vehicle already deleted");
        }

        vehicle.setDeleted(true);
        vehicle.setActive(false);

        vehicleRepository.save(vehicle);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Vehicle deleted successfully")
                .build();
    }

    public MessageResponse restoreVehicle(Integer vehicleId, Integer companyId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getCompany().getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Vehicle does not belong to this company");
        }

        if (!Boolean.TRUE.equals(vehicle.getDeleted())) {
            throw new IllegalArgumentException("Vehicle is not deleted");
        }

        vehicle.setDeleted(false);
        vehicle.setActive(true);

        vehicleRepository.save(vehicle);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Vehicle restored successfully")
                .build();
    }

    @Override
    @Transactional
    public DataResponse getVehicleById(Integer vehicleId, Integer companyId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehicle not found with id: " + vehicleId)
                );

        if (!vehicle.getCompany().getCompanyId().equals(companyId)
                || Boolean.TRUE.equals(vehicle.getDeleted())) {
            throw new ResourceNotFoundException("Vehicle not found");
        }

        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dataUtil.mapToVehicleResponse(vehicle))
                .build();
    }

    // ================= GET ALL =================

    @Override
    @Transactional
    public HttpResponse getAllVehicles(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Vehicle> cq = cb.createQuery(Vehicle.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        root.fetch("company", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // 🔎 Keyword Search
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            predicates.add(
                    cb.or(
                            cb.like(cb.lower(root.get("vehicleNumber")), pattern),
                            cb.like(cb.lower(root.get("ownerName")), pattern)
                    )
            );
        }

        // 🔥 3 Condition Logic
        if (isDeleted != null) {
            predicates.add(cb.equal(root.get("deleted"), isDeleted));
        }
        // if null → no filter → both records

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        // Sorting
        Path<?> sortPath;
        try {
            sortPath = root.get(sortBy);
        } catch (Exception e) {
            sortPath = root.get("vehicleId");
        }

        Order order = sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(sortPath)
                : cb.desc(sortPath);

        cq.orderBy(order);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Vehicle> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // 🔢 Count Query (important fix)
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Vehicle> countRoot = countQuery.from(Vehicle.class);

        List<Predicate> countPredicates = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            countPredicates.add(
                    cb.or(
                            cb.like(cb.lower(countRoot.get("vehicleNumber")), pattern),
                            cb.like(cb.lower(countRoot.get("ownerName")), pattern)
                    )
            );
        }

        if (isDeleted != null) {
            countPredicates.add(cb.equal(countRoot.get("deleted"), isDeleted));
        }

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        Page<Vehicle> page = new PageImpl<>(resultList, pageable, totalCount);

        List<VehicleResponseDTO> vehicles = page.getContent()
                .stream()
                .map(DataUtil::mapToVehicleResponse)
                .toList();

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Vehicles fetched successfully")
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .data(vehicles)
                .build();
    }
    // ================= COMPANY WISE =================

    @Override
    @Transactional
    public HttpResponse<CompanyVehicleResponseDTO> getVehiclesByCompanyId(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ===== MAIN QUERY =====
        CriteriaQuery<Vehicle> cq = cb.createQuery(Vehicle.class);
        Root<Vehicle> root = cq.from(Vehicle.class);

        List<Predicate> predicates = new ArrayList<>();

        // ✅ Company Filter (IMPORTANT)
        predicates.add(cb.equal(root.get("company").get("companyId"), companyId));

        // ✅ Keyword Search
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            predicates.add(
                    cb.or(
                            cb.like(cb.lower(root.get("vehicleNumber")), pattern),
                            cb.like(cb.lower(root.get("ownerName")), pattern)
                    )
            );
        }

        // ✅ Deleted Filter
        if (isDeleted != null) {
            predicates.add(cb.equal(root.get("deleted"), isDeleted));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);

        // ✅ Safe Sorting
        Path<?> sortPath;
        try {
            sortPath = root.get(sortBy);
        } catch (Exception e) {
            sortPath = root.get("vehicleId");
        }

        cq.orderBy(sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(sortPath)
                : cb.desc(sortPath));

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Vehicle> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ===== COUNT QUERY =====
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Vehicle> countRoot = countQuery.from(Vehicle.class);

        List<Predicate> countPredicates = new ArrayList<>();

        // ✅ Company Filter (again)
        countPredicates.add(
                cb.equal(countRoot.get("company").get("companyId"), companyId)
        );

        // ✅ Keyword
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            countPredicates.add(
                    cb.or(
                            cb.like(cb.lower(countRoot.get("vehicleNumber")), pattern),
                            cb.like(cb.lower(countRoot.get("ownerName")), pattern)
                    )
            );
        }

        // ✅ Deleted Filter
        if (isDeleted != null) {
            countPredicates.add(cb.equal(countRoot.get("deleted"), isDeleted));
        }

        countQuery.select(cb.countDistinct(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        Page<Vehicle> page = new PageImpl<>(resultList, pageable, totalCount);

        // ✅ Company Fetch
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // ✅ Mapping
        CompanyVehicleResponseDTO responseDTO =
                DataUtil.mapToCompanyVehicleResponse(company, page.getContent());

        return HttpResponse.<CompanyVehicleResponseDTO>builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Company vehicles fetched successfully")
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .data(responseDTO)
                .build();
    }
}
