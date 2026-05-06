package com.example.FuelMemo.FuelStationModule.Service.impl;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.FuelStationModule.Dto.FuelStationDto;
import com.example.FuelMemo.FuelStationModule.Dto.Request.FuelStationUpdateDto;
import com.example.FuelMemo.FuelStationModule.Dto.Response.FuelStationResponse;
import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
import com.example.FuelMemo.FuelStationModule.Repository.FuelStationRepository;
import com.example.FuelMemo.FuelStationModule.Service.FuelStationService;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import com.example.FuelMemo.Shared.Exception.AlreadyDeletedException;
import com.example.FuelMemo.Shared.Exception.DuplicateEntryException;
import com.example.FuelMemo.Shared.Exception.InactiveStatusException;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class FuelStationServiceImpl implements FuelStationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final FuelStationRepository fuelStationRepository;

    public FuelStationServiceImpl(FuelStationRepository fuelStationRepository, CompanyRepository companyRepository, DataUtil dataUtil, StateRepository stateRepository, DistrictRepository districtRepository) {
        this.fuelStationRepository = fuelStationRepository;
        this.companyRepository = companyRepository;
        this.dataUtil = dataUtil;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
    }

    private final CompanyRepository companyRepository;
    private final DataUtil dataUtil;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;

    @Override
    public MessageResponse createFuelStation(FuelStationDto dto) {

        // Validate Company
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found with id: " + dto.getCompanyId()));

        // Fetch State
        State state = stateRepository.findById(dto.getStateId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("State not found with ID: " + dto.getStateId()));

        // Fetch District
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("District not found with ID: " + dto.getDistrictId()));

        // Create New Station
        FuelStation station = new FuelStation();

        station.setFuelStationName(dto.getFuelStationName());
        station.setState(state);
        station.setDistrict(district);
        station.setSubDistrict(dto.getSubDistrict());
        station.setVillage(dto.getVillage());
        station.setOwnerName(dto.getOwnerName());
        station.setOwnerMobileNumber(dto.getOwnerMobileNumber());
        station.setCompany(company);
        station.setActive(true);
        station.setDeleted(false);

        fuelStationRepository.save(station);

        return new MessageResponse(true, HttpStatus.CREATED, "Fuel Station created successfully");
    }

    @Override
    public DataResponse getFuelStationById(Integer fuelStationId) {

        FuelStation station = fuelStationRepository
                .findByFuelStationIdAndDeletedFalse(fuelStationId);

        if (station == null)
            throw new ResourceNotFoundException(
                    "Active fuel station not found with id: " + fuelStationId);

        return new DataResponse(true, HttpStatus.OK, dataUtil.toFuelStationResponse(station));
    }

    @Override
    public DataResponse getFuelStationsByCompanyId(Integer companyId) {

        List<FuelStation> stations = fuelStationRepository
                .findByCompanyCompanyIdAndDeletedFalse(companyId);

        if (stations == null || stations.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No active fuel stations found for company id: " + companyId
            );
        }

        return new DataResponse(true, HttpStatus.OK, stations.stream().map(dataUtil::toFuelStationResponse).toList());
    }

    @Override
    public DataResponse getAllDeletedFuelStations() {

        List<FuelStation> stations =
                fuelStationRepository.findByDeletedTrue();

        List<FuelStationResponse> stationDtoList = stations.stream()
                .map(dataUtil::toFuelStationResponse)
                .toList();

        return new DataResponse(true, HttpStatus.OK, stationDtoList);
    }

    @Override
    public HttpResponse getAllFuelStations(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        List<String> allowedSortFields = List.of("fuelStationName", "state", "district", "subDistrict", "village", "ownerName", "ownerMobileNumber");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "fuelStationName";
        }

        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<FuelStation> cq = cb.createQuery(FuelStation.class);
            Root<FuelStation> root = cq.from(FuelStation.class);

            Predicate notDeleted = cb.equal(root.get("deleted"), false);

            if (keyword != null && !keyword.trim().isEmpty()) {

                String like = "%" + keyword.toLowerCase() + "%";

                Predicate keywordMatch = cb.or(
                        cb.like(cb.lower(root.get("fuelStationName")), like),
                        cb.like(cb.lower(root.get("state")), like),
                        cb.like(cb.lower(root.get("district")), like),
                        cb.like(cb.lower(root.get("subDistrict")), like),
                        cb.like(cb.lower(root.get("village")), like),
                        cb.like(cb.lower(root.get("ownerName")), like),
                        cb.like(cb.lower(root.get("ownerMobileNumber")), like)
                );

                cq.where(cb.and(notDeleted, keywordMatch));

            } else {
                cq.where(notDeleted);
            }

            cq.orderBy(sortDir.equalsIgnoreCase("asc")
                    ? cb.asc(root.get(sortBy))
                    : cb.desc(root.get(sortBy)));

            Pageable pageable = PageRequest.of(pageNumber, pageSize);

            List<FuelStation> stations = entityManager.createQuery(cq)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            // -------- COUNT QUERY --------

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<FuelStation> countRoot = countQuery.from(FuelStation.class);

            Predicate countNotDeleted = cb.equal(countRoot.get("deleted"), false);

            if (keyword != null && !keyword.trim().isEmpty()) {

                String like = "%" + keyword.toLowerCase() + "%";

                Predicate countKeywordMatch = cb.or(
                        cb.like(cb.lower(countRoot.get("fuelStationName")), like),
                        cb.like(cb.lower(countRoot.get("state")), like),
                        cb.like(cb.lower(countRoot.get("district")), like),
                        cb.like(cb.lower(countRoot.get("subDistrict")), like),
                        cb.like(cb.lower(countRoot.get("village")), like),
                        cb.like(cb.lower(countRoot.get("ownerName")), like),
                        cb.like(cb.lower(countRoot.get("ownerMobileNumber")), like)
                );

                countQuery.select(cb.count(countRoot))
                        .where(cb.and(countNotDeleted, countKeywordMatch));

            } else {
                countQuery.select(cb.count(countRoot))
                        .where(countNotDeleted);
            }

            Long totalCount = entityManager.createQuery(countQuery)
                    .getSingleResult();

            Page<FuelStation> stationPage =
                    new PageImpl<>(stations, pageable, totalCount);

            return HttpResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .message("Fuel stations fetched successfully")
                    .pageNumber(stationPage.getNumber())
                    .pageSize(stationPage.getSize())
                    .totalElements(stationPage.getTotalElements())
                    .totalPages(stationPage.getTotalPages())
                    .isLastPage(stationPage.isLast())
                    .data(stations.stream().map(dataUtil::toFuelStationResponse).toList())
                    .build();

        } catch (Exception e) {
            throw new ResourceNotFoundException(
                    "Failed to fetch fuel stations: " + e.getMessage());
        }
    }

    @Override
    public MessageResponse updateFuelStation(Integer fuelStationId, FuelStationUpdateDto dto) {

        FuelStation station = fuelStationRepository.findById(fuelStationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fuel Station not found with ID: " + fuelStationId));

        if (Boolean.TRUE.equals(station.getDeleted())) {
            throw new InactiveStatusException("Cannot update a deleted fuel station. Restore it first.");
        }

        String stationName = dto.getFuelStationName().trim();

        if (fuelStationRepository.existsByFuelStationNameIgnoreCaseAndFuelStationIdNot(stationName, fuelStationId)) {
            throw new DuplicateEntryException(
                    "Fuel Station with name '" + stationName + "' already exists!");
        }

        // ================= FETCH STATE =================
        State state = stateRepository.findById(dto.getStateId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("State not found with ID: " + dto.getStateId()));

        // ================= FETCH DISTRICT =================
        District district = districtRepository.findById(dto.getDistrictId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("District not found with ID: " + dto.getDistrictId()));

        // ================= UPDATE BASIC FIELDS =================
        station.setFuelStationName(stationName);
        station.setState(state);
        station.setDistrict(district);
        station.setSubDistrict(dto.getSubDistrict());
        station.setVillage(dto.getVillage());
        station.setOwnerName(dto.getOwnerName());
        station.setOwnerMobileNumber(dto.getOwnerMobileNumber());
        station.setUpdatedAt(LocalDateTime.now());

        // ================= COMPANY UPDATE =================
        if (dto.getCompanyId() != null) {

            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Company not found with id: " + dto.getCompanyId()));

            if (Boolean.TRUE.equals(company.getIsDeleted())) {
                throw new InactiveStatusException("Cannot assign deleted company.");
            }

            station.setCompany(company);
        }

        fuelStationRepository.save(station);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Fuel Station updated successfully.")
                .build();
    }
    @Override
    public MessageResponse softDeleteFuelStation(Integer fuelStationId) {

        FuelStation station = fuelStationRepository.findById(fuelStationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fuel Station not found"));

        if (Boolean.TRUE.equals(station.getDeleted())) {
            throw new InactiveStatusException("Fuel Station already soft deleted");
        }

        station.setDeleted(true);
        fuelStationRepository.save(station);

        return new MessageResponse(true, HttpStatus.OK, "Fuel Station soft deleted successfully");
    }

    @Override
    public MessageResponse restoreFuelStation(Integer fuelStationId) {

        FuelStation station = fuelStationRepository.findById(fuelStationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fuel Station not found"));

        if (!Boolean.TRUE.equals(station.getDeleted())) {
            throw new AlreadyDeletedException("Fuel Station already active");
        }

        station.setDeleted(false);
        fuelStationRepository.save(station);

        return new MessageResponse(true, HttpStatus.OK, "Fuel Station restored successfully");
    }

    @Override
    public MessageResponse deleteFuelStation(Integer fuelStationId) {

        FuelStation station = fuelStationRepository.findById(fuelStationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Fuel Station not found"));

        if (Boolean.TRUE.equals(station.getDeleted())) {
            throw new AlreadyDeletedException(
                    "Cannot permanently delete soft-deleted fuel station");
        }

        fuelStationRepository.delete(station);

        return new MessageResponse(true, HttpStatus.OK, "Fuel Station deleted successfully");
    }
}
