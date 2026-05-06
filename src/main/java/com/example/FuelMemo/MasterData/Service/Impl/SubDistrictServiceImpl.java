package com.example.FuelMemo.MasterData.Service.Impl;

import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.MasterData.Config.spring_batch.sub_district_batch.SubDistrictProcessor;
import com.example.FuelMemo.MasterData.Config.spring_batch.sub_district_batch.SubDistrictWriter;
import com.example.FuelMemo.MasterData.Dto.SubDistrictDto;
import com.example.FuelMemo.MasterData.Dto.SubDistrictExportDTO;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.SubDistrictUpdateDto;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import com.example.FuelMemo.MasterData.Repository.*;
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
import java.util.*;

@Service
public class SubDistrictServiceImpl implements SubDistrictService {
    @Getter
    private static String userName;
    @PersistenceContext
    private final EntityManager entityManager;

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final DistrictRepository districtRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;

//    private final MiningLeaseRepository miningLeaseRepository;
//    private final QuarryLeaseRepository quarryLeaseRepository;
//    private final StockRegistrationRepository stockRegistrationRepository;
//    private final WeighBridgeRepository weighBridgeRepository;
//    private final EquipmentRepository equipmentRepository;
//    private final VehicleRepository vehicleRepository;
//    private final FuelStationRepository fuelStationRepository;
//    private final MasterIndividualRepository masterIndividualRepository;
//    private final MasterIndividualService masterIndividualService;
//    private final MasterEntityRepository masterEntityRepository;
//    private final MasterEntityService masterEntityService;

    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private final DataUtil dateUtil;
    private final SubDistrictRepository subDistrictRepository;

//    private final RoyaltyPassRepository royaltyPassRepository;
//
//    private final GeneralDetailsRepository generalDetailsRepository;
//
//    private final DeliveryChallanRepository deliveryChallanRepository;
//
//    private final CdmWeighBridgeRepository cdmWeighBridgeRepository;
//
//    private final TransportMemoRepository transportMemoRepository;
//
//    private final FuelMemoRepository fuelMemoRepository;

    private final VillageRepository villageRepository;
    private final VillageService villageService;
    private final PermissionService permissionService;


    @Autowired
    public SubDistrictServiceImpl(EntityManager entityManager, DistrictRepository districtRepository, StateRepository stateRepository,
                                  CountryRepository countryRepository, JobLauncher jobLauncher, @Qualifier("subDistrictReaderJob") Job job,
                                  DataUtil dateUtil, SubDistrictRepository subDistrictRepository, VillageRepository villageRepository, VillageService villageService, PermissionService permissionService) {
        this.entityManager = entityManager;
        this.districtRepository = districtRepository;
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
        this.permissionService = permissionService;
//        this.miningLeaseRepository = miningLeaseRepository;
//        this.quarryLeaseRepository = quarryLeaseRepository;
//        this.stockRegistrationRepository = stockRegistrationRepository;
//        this.weighBridgeRepository = weighBridgeRepository;
//        this.equipmentRepository = equipmentRepository;
//        this.vehicleRepository = vehicleRepository;
//        this.fuelStationRepository = fuelStationRepository;
//        this.masterIndividualRepository = masterIndividualRepository;
//        this.masterIndividualService = masterIndividualService;
//        this.masterEntityRepository = masterEntityRepository;
//        this.masterEntityService = masterEntityService;

        this.jobLauncher = jobLauncher;
        this.job = job;
        this.dateUtil = dateUtil;
        this.subDistrictRepository = subDistrictRepository;
//        this.royaltyPassRepository = royaltyPassRepository;
//        this.generalDetailsRepository = generalDetailsRepository;
//        this.deliveryChallanRepository = deliveryChallanRepository;
//        this.cdmWeighBridgeRepository = cdmWeighBridgeRepository;
//        this.transportMemoRepository = transportMemoRepository;
//        this.fuelMemoRepository = fuelMemoRepository;
        this.villageRepository = villageRepository;
        this.villageService = villageService;
    }

    @Override
    public MessageResponse addSubDistrict(SubDistrictDto subDistrictDto, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Permission check using PermissionService
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "SUB_DISTRICT_WRITE");

        if (hasPermission) {
            // HANDLE IF DISTRICT EXIST BY ID
            District district = districtRepository.findById(subDistrictDto.getDistrictId())
                    .orElseThrow(() -> new ResourceNotFoundException("District with ID '" + subDistrictDto.getDistrictId() + "' not found!"));

            String cityName = district.getCityName();

            // HANDLE IF DUPLICATE SUB-DISTRICT NAME
            if (subDistrictRepository.existsBySubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                    subDistrictDto.getSubDistrictName(),
                    cityName,
                    district.getState().getStateName(),
                    district.getCountry().getCountryName()))
                throw new DuplicateEntryException("Sub-District with name '" + subDistrictDto.getSubDistrictName() + "' already exist!");

            // IS DISTRICT STATUS ACTIVE ?
            if (!district.getStatus())
                throw new InactiveStatusException("District '" + district.getCityName() + "' is inactive!");

            SubDistrict subDistrict = new SubDistrict();
            subDistrict.setSubDistrictName(subDistrictDto.getSubDistrictName().trim());
            subDistrict.setCountry(district.getCountry());
            subDistrict.setState(district.getState());
            subDistrict.setDistrict(district);

            if (subDistrictDto.getStatus() != null)
                subDistrict.setStatus(subDistrictDto.getStatus());
            else
                subDistrict.setStatus(true);
            subDistrict.setCreatedAt(LocalDateTime.now());
            subDistrict.setUpdatedAt(LocalDateTime.now());
            subDistrict.setDeletedAt(null);
            subDistrict.setIsDeleted(false);
            subDistrict.setOperatorName(loggedInUser);

            subDistrictRepository.save(subDistrict);

            return new MessageResponse(true, HttpStatus.OK, "Subdistrict saved.");
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public DataResponse getSubDistrictById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // Check permission: SUPERADMIN or SUB_DISTRICT_READ
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "SUB_DISTRICT_READ");

        if (hasPermission) {
            // Original logic intact
            return subDistrictRepository.findById(id)
                    .map(subDistrict -> new DataResponse(true, HttpStatus.OK, dateUtil.subDistrictToDto(subDistrict)))
                    .orElseThrow(() -> new ResourceNotFoundException("Sub-District with ID '" + id + "' not found!"));
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public HttpResponse getAllSubDistricts(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers) {
        Page<SubDistrict> pageSubDistrict = null;
        List<SubDistrictDto> subdistricts = null;

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

        String roleName = headers.get("rolename");

        // Simple role-based permission check (no REST call)
        if ("ROLE_SUPERADMIN".equals(roleName)) {
            try {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<SubDistrict> criteriaQuery = criteriaBuilder.createQuery(SubDistrict.class);
                Root<SubDistrict> root = criteriaQuery.from(SubDistrict.class);

                Join<SubDistrict, District> districtJoin = root.join("district");

                // Sorting
                Order order = "asc".equalsIgnoreCase(sortDirection) ?
                        criteriaBuilder.asc(root.get(sortBy)) :
                        criteriaBuilder.desc(root.get(sortBy));
                criteriaQuery.orderBy(order);

                // Predicates
                Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isStatus);
                Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
                Predicate districtNotDeletedPredicate = criteriaBuilder.equal(districtJoin.get("isDeleted"), Boolean.FALSE);
                Predicate districtActivePredicate = criteriaBuilder.equal(districtJoin.get("status"), Boolean.TRUE);
                Predicate stateActiveAndNotDeletedPredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(districtJoin.get("state").get("isDeleted"), Boolean.FALSE),
                        criteriaBuilder.equal(districtJoin.get("state").get("status"), Boolean.TRUE)
                );
                Predicate countryActiveAndNotDeletedPredicate = criteriaBuilder.and(
                        criteriaBuilder.equal(districtJoin.get("state").get("country").get("isDeleted"), Boolean.FALSE),
                        criteriaBuilder.equal(districtJoin.get("state").get("country").get("status"), Boolean.TRUE)
                );
                Predicate subDistrictIdPredicate = criteriaBuilder.equal(root.get("subDistrictId"), intKeyword);

                Predicate subDistrictAndDistrictAndStateAndCountryNamePredicate = null;
                if (keyword != null) {
                    subDistrictAndDistrictAndStateAndCountryNamePredicate =
                            criteriaBuilder.or(
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("subDistrictName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("district").get("cityName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("state").get("stateName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                    criteriaBuilder.like(criteriaBuilder.lower(root.get("country").get("countryName").as(String.class)), "%" + keyword.toLowerCase() + "%")
                            );
                }

                // Combine predicates
                Predicate combinedPredicate = null;
                if (isStatus != null) combinedPredicate = statusPredicate;

                if (softDeleted != null) {
                    if (intKeyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, subDistrictIdPredicate, deletedPredicate, districtActivePredicate, districtNotDeletedPredicate, stateActiveAndNotDeletedPredicate, countryActiveAndNotDeletedPredicate);
                    } else if (keyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, subDistrictAndDistrictAndStateAndCountryNamePredicate, deletedPredicate, districtActivePredicate, districtNotDeletedPredicate, stateActiveAndNotDeletedPredicate, countryActiveAndNotDeletedPredicate);
                    } else {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate, districtActivePredicate, districtNotDeletedPredicate, stateActiveAndNotDeletedPredicate, countryActiveAndNotDeletedPredicate);
                    }
                }

                // Execute query
                criteriaQuery.select(root).where(combinedPredicate);

                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                List<SubDistrict> resultList = entityManager.createQuery(criteriaQuery)
                        .setFirstResult((int) pageable.getOffset())
                        .setMaxResults(pageable.getPageSize())
                        .getResultList();

                long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();
                pageSubDistrict = new PageImpl<>(resultList, pageable, totalCount);

                subdistricts = dateUtil.subDistrictsToDtos(resultList);

            } catch (NoResultException ignored) {
            }

            return HttpResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .pageNumber(pageSubDistrict.getNumber())
                    .pageSize(pageSubDistrict.getSize())
                    .totalElements(pageSubDistrict.getTotalElements())
                    .totalPages(pageSubDistrict.getTotalPages())
                    .isLastPage(pageSubDistrict.isLast())
                    .data(subdistricts)
                    .build();
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }


    @Override
    public MessageResponse updateSubDistrict(SubDistrictUpdateDto subDistrictDto, int id, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // Define allowed roles for updating SubDistrict
        Set<String> allowedRoles = Set.of("ROLE_SUPERADMIN"); // Add any roles you want

        // Role + permission check
        if (!allowedRoles.contains(roleName)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        // HANDLE IF SUB-DISTRICT EXIST BY ID
        SubDistrict subDistrict = subDistrictRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-District with ID '" + id + "' not found!"));

        String cityName = subDistrict.getDistrict().getCityName();
        String stateName = subDistrict.getState().getStateName();
        String countryName = subDistrict.getCountry().getCountryName();

        // HANDLE IF DUPLICATE SUB-DISTRICT NAME
        if (subDistrictDto.getSubDistrictName() != null) {
            if (subDistrictDto.getDistrictId() != null) {
                District district = districtRepository.findById(subDistrictDto.getDistrictId())
                        .orElseThrow(() -> new ResourceNotFoundException("District with ID '" + subDistrictDto.getDistrictId() + "' not found!"));

                cityName = district.getCityName();
                stateName = district.getState().getStateName();
                countryName = district.getCountry().getCountryName();

                subDistrict.setDistrict(district);
            }

            SubDistrict subDistrictWithSameName = subDistrictRepository
                    .findBySubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                            subDistrictDto.getSubDistrictName().trim(),
                            cityName.trim(),
                            stateName.trim(),
                            countryName.trim()
                    );

            if (subDistrictWithSameName != null && !subDistrictWithSameName.getSubDistrictId().equals(subDistrict.getSubDistrictId()))
                throw new DuplicateEntryException("Sub-District '" + subDistrictDto.getSubDistrictName() + "' already exist!");

            subDistrict.setSubDistrictName(subDistrictDto.getSubDistrictName().trim());
        }

        if (subDistrictDto.getStatus() != null)
            subDistrict.setStatus(subDistrictDto.getStatus());

        subDistrict.setUpdatedAt(LocalDateTime.now());
        subDistrict.setOperatorName(loggedInUser);
        subDistrictRepository.save(subDistrict);

        return new MessageResponse(true, HttpStatus.OK, "Subdistrict updated.");
    }


/*
    @Transactional
    @Override
    public MessageResponse deleteSubDistrictById(int id, Map<String, String> headers) {

            String roleName = headers.get("rolename");

            // Define the URL to call the AUTH service
            String url = UriComponentsBuilder.fromHttpUrl("http://" + hostname + "/auth/role/checkPermission?permissionName=SUB_DISTRICT_DELETE").toUriString();

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
                SubDistrict subDistrict = subDistrictRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Sub-District with ID '" + id + "' not found!"));

                for (Village v : villageRepository.findAllBySubDistrict_SubDistrictId(subDistrict.getSubDistrictId()))
                    villageService.deleteVillageById(v.getVillageId(), headers);

//        for (MasterIndividual m : masterIndividualRepository.findAllBySubDistrict_SubDistrictId(id))
//            masterIndividualService.hardDeleteIndividual(m.getId());
//        for (MasterEntity m : masterEntityRepository.findAllBySubDistrict_SubDistrictId(id))
//            masterEntityService.hardDeleteEntity(m.getId());
//        for (MiningLease m : miningLeaseRepository.findAllBySubDistrict_SubDistrictId(id))
//            miningLeaseRepository.delete(m);
//        for (QuarryLease m : quarryLeaseRepository.findAllBySubDistrict_SubDistrictId(id))
//            quarryLeaseRepository.delete(m);
//        for (StockRegistration m : stockRegistrationRepository.findAllBySubDistrict_SubDistrictId(id))
//            stockRegistrationRepository.delete(m);
//        for (WeighBridge m : weighBridgeRepository.findAllBySubDistrict_SubDistrictId(id))
//            weighBridgeRepository.delete(m);
//        for (Equipment m : equipmentRepository.findAllBySubDistrict_SubDistrictId(id))
//            equipmentRepository.delete(m);
//        for (Vehicle m : vehicleRepository.findAllBySubDistrict_SubDistrictId(id))
//            vehicleRepository.delete(m);
//        for (FuelStation m : fuelStationRepository.findAllBySubDistrict_SubDistrictId(id))
//            fuelStationRepository.delete(m);
//
//        List<MiningLease> miningLeases = new ArrayList<>();
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<MiningLease> criteriaQuery = criteriaBuilder.createQuery(MiningLease.class);
//            Root<MiningLease> root = criteriaQuery.from(MiningLease.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            miningLeases = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (miningLeases.size() > 0 && !miningLeases.isEmpty()) {
//                miningLeases.forEach(miningLease -> {
//                    List<RoyaltyPass> royaltyPasses = new ArrayList<>();
//                    try {
//                        CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                        Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                        criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("miningLease"), miningLease.getId()));
//
//                        royaltyPasses = entityManager.createQuery(criteriaQuery_royaltyPass).getResultList();
//
//                        if (royaltyPasses.size() > 0 && !royaltyPasses.isEmpty()) {
//                            royaltyPasses.forEach(royaltyPass -> {
//                                GeneralDetails details = generalDetailsRepository.findByRoyaltyPass(royaltyPass.getId());
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(royaltyPass.getId());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(royaltyPass.getId());
//                                CdmWeighBridge cdmWeighBridge = cdmWeighBridgeRepository.findByRoyaltyPass(royaltyPass.getId());
//                                FuelMemo fuelMemo = fuelMemoRepository.findByRoyaltyPassId(royaltyPass.getId());
//
//                                if (details != null)
//                                    generalDetailsRepository.delete(details);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (cdmWeighBridge != null)
//                                    cdmWeighBridgeRepository.delete(cdmWeighBridge);
//
//                                if (fuelMemo != null)
//                                    fuelMemoRepository.delete(fuelMemo);
//                            });
//                            royaltyPassRepository.deleteAll(royaltyPasses);
//                        }
//
//                    } catch (Exception ignore) {
//
//                    }
//                });
//            }
//        } catch (Exception ignore) {
//        }
//
//        List<QuarryLease> quarryLeases = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<QuarryLease> criteriaQuery = criteriaBuilder.createQuery(QuarryLease.class);
//            Root<QuarryLease> root = criteriaQuery.from(QuarryLease.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            quarryLeases = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (quarryLeases.size() > 0 && !quarryLeases.isEmpty()) {
//                quarryLeases.forEach(quarryLease -> {
//                    List<RoyaltyPass> royaltyPasses = new ArrayList<>();
//                    try {
//                        CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                        Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                        criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("quarryLease"), quarryLease.getId()));
//
//                        royaltyPasses = entityManager.createQuery(criteriaQuery_royaltyPass).getResultList();
//
//                        if (royaltyPasses.size() > 0 && !royaltyPasses.isEmpty()) {
//                            royaltyPasses.forEach(royaltyPass -> {
//                                GeneralDetails details = generalDetailsRepository.findByRoyaltyPass(royaltyPass.getId());
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(royaltyPass.getId());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(royaltyPass.getId());
//                                CdmWeighBridge cdmWeighBridge = cdmWeighBridgeRepository.findByRoyaltyPass(royaltyPass.getId());
//                                FuelMemo fuelMemo = fuelMemoRepository.findByRoyaltyPassId(royaltyPass.getId());
//
//                                if (details != null)
//                                    generalDetailsRepository.delete(details);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (cdmWeighBridge != null)
//                                    cdmWeighBridgeRepository.delete(cdmWeighBridge);
//
//                                if (fuelMemo != null)
//                                    fuelMemoRepository.delete(fuelMemo);
//                            });
//                            royaltyPassRepository.deleteAll(royaltyPasses);
//                        }
//
//                    } catch (Exception ignore) {
//                    }
//                });
//            }
//        } catch (Exception ignore) {
//        }
//
//        List<StockRegistration> stockRegistrations = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<StockRegistration> criteriaQuery = criteriaBuilder.createQuery(StockRegistration.class);
//            Root<StockRegistration> root = criteriaQuery.from(StockRegistration.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            stockRegistrations = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (stockRegistrations.size() > 0 && !stockRegistrations.isEmpty()) {
//                stockRegistrations.forEach(stockRegistration -> {
//                    List<RoyaltyPass> royaltyPasses = new ArrayList<>();
//                    try {
//                        CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                        Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                        criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("stockRegistration"), stockRegistration.getId()));
//
//                        royaltyPasses = entityManager.createQuery(criteriaQuery_royaltyPass).getResultList();
//
//                        if (royaltyPasses.size() > 0 && !royaltyPasses.isEmpty()) {
//                            royaltyPasses.forEach(royaltyPass -> {
//                                GeneralDetails details = generalDetailsRepository.findByRoyaltyPass(royaltyPass.getId());
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(royaltyPass.getId());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(royaltyPass.getId());
//                                CdmWeighBridge cdmWeighBridge = cdmWeighBridgeRepository.findByRoyaltyPass(royaltyPass.getId());
//                                FuelMemo fuelMemo = fuelMemoRepository.findByRoyaltyPassId(royaltyPass.getId());
//
//                                if (details != null)
//                                    generalDetailsRepository.delete(details);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (cdmWeighBridge != null)
//                                    cdmWeighBridgeRepository.delete(cdmWeighBridge);
//
//                                if (fuelMemo != null)
//                                    fuelMemoRepository.delete(fuelMemo);
//                            });
//                            royaltyPassRepository.deleteAll(royaltyPasses);
//                        }
//
//                    } catch (Exception ignore) {
//                    }
//                });
//            }
//        } catch (Exception ignore) {
//        }
//
//        List<WeighBridge> weighBridges = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<WeighBridge> criteriaQuery = criteriaBuilder.createQuery(WeighBridge.class);
//            Root<WeighBridge> root = criteriaQuery.from(WeighBridge.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            weighBridges = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (weighBridges.size() > 0 && !weighBridges.isEmpty()) {
//                weighBridges.forEach(weighBridge -> {
//                    List<CdmWeighBridge> cdmWeighBridges = new ArrayList<>();
//                    try {
//                        CriteriaBuilder criteriaBuilder_cdmWeighBridges = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<CdmWeighBridge> criteriaQuery_cdmWeighBridges = criteriaBuilder_cdmWeighBridges.createQuery(CdmWeighBridge.class);
//                        Root<CdmWeighBridge> root_cdmWeighBridges = criteriaQuery_cdmWeighBridges.from(CdmWeighBridge.class);
//
//                        criteriaQuery_cdmWeighBridges.select(root_cdmWeighBridges)
//                                .where(criteriaBuilder_cdmWeighBridges.equal(root_cdmWeighBridges.get("weighBridge"), weighBridge.getId()));
//
//                        cdmWeighBridges = entityManager.createQuery(criteriaQuery_cdmWeighBridges).getResultList();
//
//                        if (cdmWeighBridges.size() > 0 && !cdmWeighBridges.isEmpty()) {
//                            cdmWeighBridges.forEach(cdmWeighBridge -> {
//                                RoyaltyPass royaltyPass = null;
//
//                                try {
//                                    CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                                    CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                                    Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                                    criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                            .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("royaltyPass"), cdmWeighBridge.getRoyaltyPass()));
//
//                                    royaltyPass = entityManager.createQuery(criteriaQuery_royaltyPass).getSingleResult();
//                                } catch (Exception ignored) {
//
//                                }
//
//                                GeneralDetails details = generalDetailsRepository.findByRoyaltyPass(cdmWeighBridge.getRoyaltyPass());
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(cdmWeighBridge.getRoyaltyPass());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(cdmWeighBridge.getRoyaltyPass());
//                                FuelMemo fuelMemo = fuelMemoRepository.findByRoyaltyPassId(cdmWeighBridge.getRoyaltyPass());
//
//                                if (royaltyPass != null)
//                                    royaltyPassRepository.delete(royaltyPass);
//
//                                if (details != null)
//                                    generalDetailsRepository.delete(details);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (fuelMemo != null)
//                                    fuelMemoRepository.delete(fuelMemo);
//                            });
//                            cdmWeighBridgeRepository.deleteAll(cdmWeighBridges);
//                        }
//
//                    } catch (Exception ignore) {
//                    }
//                });
//            }
//        } catch (Exception ignore) {
//        }
//
//        List<Equipment> equipmentList = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<Equipment> criteriaQuery = criteriaBuilder.createQuery(Equipment.class);
//            Root<Equipment> root = criteriaQuery.from(Equipment.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            equipmentList = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (equipmentList.size() > 0 && !equipmentList.isEmpty()) {
//                equipmentList.forEach(equipment -> {
//                    List<GeneralDetails> generalDetails = new ArrayList<>();
//
//                    try {
//                        CriteriaBuilder criteriaBuilder_generalDetails = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<GeneralDetails> criteriaQuery_generalDetails = criteriaBuilder_generalDetails.createQuery(GeneralDetails.class);
//                        Root<GeneralDetails> root_generalDetails = criteriaQuery_generalDetails.from(GeneralDetails.class);
//
//                        Predicate miningBy = criteriaBuilder_generalDetails.equal(root_generalDetails.get("miningBy"), equipment.getEquipmentId());
//                        Predicate loadedBy = criteriaBuilder_generalDetails.equal(root_generalDetails.get("loadedBy"), equipment.getEquipmentId());
//
//                        Predicate predicateMiningByOrLoadedBy
//                                = criteriaBuilder_generalDetails.or(miningBy, loadedBy);
//
//                        criteriaQuery_generalDetails.select(root_generalDetails)
//                                .where(predicateMiningByOrLoadedBy);
//
//                        generalDetails = entityManager.createQuery(criteriaQuery_generalDetails).getResultList();
//                        if (generalDetails.size() > 0 && !generalDetails.isEmpty()) {
//                            generalDetails.forEach(generalDetail -> {
//                                RoyaltyPass royaltyPass = null;
//
//                                try {
//                                    CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                                    CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                                    Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                                    criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                            .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("royaltyPass"), generalDetail.getRoyaltyPass()));
//
//                                    royaltyPass = entityManager.createQuery(criteriaQuery_royaltyPass).getSingleResult();
//                                } catch (Exception ignored) {
//                                }
//
//
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(generalDetail.getRoyaltyPass());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(generalDetail.getRoyaltyPass());
//                                CdmWeighBridge cdmWeighBridge = cdmWeighBridgeRepository.findByRoyaltyPass(generalDetail.getRoyaltyPass());
//                                FuelMemo fuelMemo = fuelMemoRepository.findByRoyaltyPassId(generalDetail.getRoyaltyPass());
//
//                                if (royaltyPass != null)
//                                    royaltyPassRepository.delete(royaltyPass);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (cdmWeighBridge != null)
//                                    cdmWeighBridgeRepository.delete(cdmWeighBridge);
//
//                                if (fuelMemo != null)
//                                    fuelMemoRepository.delete(fuelMemo);
//                            });
//                            generalDetailsRepository.deleteAll(generalDetails);
//                        }
//                    } catch (NoResultException ignored) {
//                    }
//                });
//            }
//        } catch (Exception ignore) {
//        }
//
//        List<Vehicle> vehicles = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<Vehicle> criteriaQuery = criteriaBuilder.createQuery(Vehicle.class);
//            Root<Vehicle> root = criteriaQuery.from(Vehicle.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            vehicles = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (vehicles.size() > 0 && !vehicles.isEmpty()) {
//                vehicles.forEach(vehicle -> {
//                    List<RoyaltyPass> royaltyPasses = new ArrayList<>();
//
//                    try {
//                        CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                        Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                        criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("vehicleNo"), vehicle.getVehicleNo()));
//
//                        royaltyPasses = entityManager.createQuery(criteriaQuery_royaltyPass).getResultList();
//
//                        if (royaltyPasses.size() > 0 && !royaltyPasses.isEmpty()) {
//                            royaltyPasses.forEach(royaltyPass -> {
//                                GeneralDetails details = generalDetailsRepository.findByRoyaltyPass(royaltyPass.getId());
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(royaltyPass.getId());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(royaltyPass.getId());
//                                CdmWeighBridge cdmWeighBridge = cdmWeighBridgeRepository.findByRoyaltyPass(royaltyPass.getId());
//                                FuelMemo fuelMemo = fuelMemoRepository.findByRoyaltyPassId(royaltyPass.getId());
//
//                                if (details != null)
//                                    generalDetailsRepository.delete(details);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (cdmWeighBridge != null)
//                                    cdmWeighBridgeRepository.delete(cdmWeighBridge);
//
//                                if (fuelMemo != null)
//                                    fuelMemoRepository.delete(fuelMemo);
//
//                            });
//                            royaltyPassRepository.deleteAll(royaltyPasses);
//
//                        }
//                    } catch (NoResultException ignored) {
//                    }
//                });
//            }
//        } catch (NoResultException ignored) {
//        }
//
//        List<FuelStation> fuelStations = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<FuelStation> criteriaQuery = criteriaBuilder.createQuery(FuelStation.class);
//            Root<FuelStation> root = criteriaQuery.from(FuelStation.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("subDistrict"), subDistrict));
//
//            fuelStations = entityManager.createQuery(criteriaQuery).getResultList();
//
//            if (fuelStations.size() > 0 && !fuelStations.isEmpty()) {
//                fuelStations.forEach(fuelStation -> {
//                    List<FuelMemo> fuelMemos = new ArrayList<>();
//
//                    try {
//                        CriteriaBuilder criteriaBuilder_fuelMemos = entityManager.getCriteriaBuilder();
//                        CriteriaQuery<FuelMemo> criteriaQuery_fuelMemos = criteriaBuilder_fuelMemos.createQuery(FuelMemo.class);
//                        Root<FuelMemo> root_fuelMemos = criteriaQuery_fuelMemos.from(FuelMemo.class);
//
//                        criteriaQuery_fuelMemos.select(root_fuelMemos)
//                                .where(criteriaBuilder_fuelMemos.equal(root_fuelMemos.get("fuelStationId"), fuelStation.getId()));
//
//                        fuelMemos = entityManager.createQuery(criteriaQuery_fuelMemos).getResultList();
//
//                        if (fuelMemos.size() > 0 && !fuelMemos.isEmpty()) {
//                            fuelMemos.forEach(fuelMemo -> {
//                                RoyaltyPass royaltyPass = null;
//
//                                try {
//                                    CriteriaBuilder criteriaBuilder_royaltyPass = entityManager.getCriteriaBuilder();
//                                    CriteriaQuery<RoyaltyPass> criteriaQuery_royaltyPass = criteriaBuilder_royaltyPass.createQuery(RoyaltyPass.class);
//                                    Root<RoyaltyPass> root_royaltyPass = criteriaQuery_royaltyPass.from(RoyaltyPass.class);
//
//                                    criteriaQuery_royaltyPass.select(root_royaltyPass)
//                                            .where(criteriaBuilder_royaltyPass.equal(root_royaltyPass.get("royaltyPassId"), fuelMemo.getRoyaltyPassId()));
//
//                                    royaltyPass = entityManager.createQuery(criteriaQuery_royaltyPass).getSingleResult();
//                                } catch (Exception ignored) {
//                                }
//
//
//                                GeneralDetails details = generalDetailsRepository.findByRoyaltyPass(fuelMemo.getRoyaltyPassId());
//                                DeliveryChallan deliveryChallan = deliveryChallanRepository.findByRoyaltyPass(fuelMemo.getRoyaltyPassId());
//                                TransportMemo transportMemo = transportMemoRepository.findByRoyaltyPass(fuelMemo.getRoyaltyPassId());
//                                CdmWeighBridge cdmWeighBridge = cdmWeighBridgeRepository.findByRoyaltyPass(fuelMemo.getRoyaltyPassId());
//
//                                if (royaltyPass != null)
//                                    royaltyPassRepository.delete(royaltyPass);
//
//                                if (details != null)
//                                    generalDetailsRepository.delete(details);
//
//                                if (deliveryChallan != null)
//                                    deliveryChallanRepository.delete(deliveryChallan);
//
//                                if (transportMemo != null)
//                                    transportMemoRepository.delete(transportMemo);
//
//                                if (cdmWeighBridge != null)
//                                    cdmWeighBridgeRepository.delete(cdmWeighBridge);
//                            });
//                            fuelMemoRepository.deleteAll(fuelMemos);
//                        }
//                    } catch (NoResultException ignored) {
//                    }
//                });
//            }
//        } catch (Exception ignore) {
//        }
//
//        for (MiningLease m : miningLeaseRepository.findAllBySubDistrict_SubDistrictId(id))
//            miningLeaseRepository.delete(m);
//        for (QuarryLease m : quarryLeaseRepository.findAllBySubDistrict_SubDistrictId(id))
//            quarryLeaseRepository.delete(m);
//        for (StockRegistration m : stockRegistrationRepository.findAllBySubDistrict_SubDistrictId(id))
//            stockRegistrationRepository.delete(m);
//        for (WeighBridge m : weighBridgeRepository.findAllBySubDistrict_SubDistrictId(id))
//            weighBridgeRepository.delete(m);
//        for (Equipment m : equipmentRepository.findAllBySubDistrict_SubDistrictId(id))
//            equipmentRepository.delete(m);
//        for (Vehicle m : vehicleRepository.findAllBySubDistrict_SubDistrictId(id))
//            vehicleRepository.delete(m);
//        for (FuelStation m : fuelStationRepository.findAllBySubDistrict_SubDistrictId(id))
//            fuelStationRepository.delete(m);
//        for (MasterEntity m : masterEntityRepository.findAllBySubDistrict_SubDistrictId(id))
//            masterEntityService.hardDeleteEntity(m.getId());
//        for (MasterIndividual m : masterIndividualRepository.findAllBySubDistrict_SubDistrictId(id))
//            masterIndividualService.hardDeleteIndividual(m.getId());

                int rowsAffected = 0;

                try {
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    CriteriaDelete<SubDistrict> criteriaDelete = criteriaBuilder.createCriteriaDelete(SubDistrict.class);
                    Root<SubDistrict> root = criteriaDelete.from(SubDistrict.class);

                    criteriaDelete.where(
                            criteriaBuilder.equal(root.get("subDistrictId"), subDistrict.getSubDistrictId())
                    );
                    rowsAffected = entityManager.createQuery(criteriaDelete).executeUpdate();
                    System.out.println("Operation Performed Successfully " + rowsAffected + " rows Affected");

                } catch (Exception e) {
                    System.out.println("Error Occurred " + e.getMessage());
                    System.out.println(e);
                }

                if (rowsAffected > 0 || rowsAffected == 1)
                    return new MessageResponse(true, HttpStatus.OK, "Data Deleted");

//        subDistrictRepository.delete(subDistrict);

                return new MessageResponse(true, HttpStatus.OK, "Sub-District deleted!");
            } else {
                throw new UserUnauthorizedException("You don't have permission to perform this operation.");
            }
    }
*/

    @Override
    public MessageResponse softDeleteSubDistrictById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Check permission via PermissionService
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "SUB_DISTRICT_DELETE"
        );

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        SubDistrict subDistrict = subDistrictRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-District with ID '" + id + "' not found!"));

        subDistrict.setIsDeleted(true);
        subDistrict.setDeletedAt(LocalDateTime.now());
        subDistrictRepository.save(subDistrict);

        return new MessageResponse(true, HttpStatus.OK,
                "SubDistrict with id '" + id + "' soft deleted!");
    }

    @Override
    public MessageResponse restoreSubDistrictById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Check permission via PermissionService
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "SUB_DISTRICT_RESTORE"
        );

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        SubDistrict subDistrict = subDistrictRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-District with ID '" + id + "' not found!"));

        subDistrict.setIsDeleted(false);
        subDistrict.setDeletedAt(null);
        subDistrictRepository.save(subDistrict);

        return new MessageResponse(true, HttpStatus.OK,
                "SubDistrict with id '" + id + "' restored!");
    }


    @Override
    public ListResponse getAllSubDistrictsByStatusAndIsDeleted(int districtId, Map<String, String> headers) {
        // ✅ Permission check
        String roleName = headers.get("rolename");
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "SUB_DISTRICT_READ"
        );

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        List<SubDistrictDto> subdistricts = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<SubDistrict> criteriaQuery = criteriaBuilder.createQuery(SubDistrict.class);
            Root<SubDistrict> root = criteriaQuery.from(SubDistrict.class);

            Predicate districtIdPredicate = criteriaBuilder.equal(root.get("district").get("districtId"), districtId);
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), true);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);

            criteriaQuery.select(root)
                    .where(districtIdPredicate, statusPredicate, deletedPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("subDistrictName")));

            List<SubDistrict> resultList = entityManager.createQuery(criteriaQuery).getResultList();
            subdistricts = dateUtil.subDistrictsToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, subdistricts);
    }

    @Override
    public ListResponse exportedSubDistrictData(Map<String, String> headers) {
        List<SubDistrictExportDTO> districts = new ArrayList<>();

        // ✅ Permission check
        String roleName = headers.get("rolename");
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "SUB_DISTRICT_EXPORT"
        );

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<SubDistrict> criteriaQuery = criteriaBuilder.createQuery(SubDistrict.class);
            Root<SubDistrict> root = criteriaQuery.from(SubDistrict.class);

            criteriaQuery.select(root)
                    .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                    .orderBy(criteriaBuilder.asc(root.get("subDistrictName")));

            List<SubDistrict> resultList = entityManager.createQuery(criteriaQuery).getResultList();
            districts = dateUtil.subDistrictExportDTOStoSubDistricts(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, districts);
    }

    @Override
    public MessageListResponse uploadSubDistrictCSV(MultipartFile file, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String email = headers.get("email"); // Extract email from headers or token
        String roleName = headers.get("rolename");

        System.out.println("Uploading Sub-District CSV by user: " + loggedInUser + " | Email: " + email + " | Role: " + roleName);

        // ✅ Permission check
        boolean hasPermission = false;
        try {
            if ("ROLE_SUPERADMIN".equalsIgnoreCase(roleName)) {
                hasPermission = true; // superadmin bypass
            } else {
                hasPermission = permissionService.hasPermission(email, "SUB_DISTRICT_IMPORT");
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
                            LocalTime.now().format(timeFormatter) + "_Sub_District_temp",
                    ".csv"
            );

            file.transferTo(tempFile);
            userName = loggedInUser; // For audit

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFileSubDistrict", tempFile.getAbsolutePath())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            long recordsUploaded = 0;
            long failedRecords = 0;

            if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                System.out.println("Sub-District CSV Job Completed...");
                recordsUploaded = SubDistrictWriter.getInsertedCounter();
                failedRecords = SubDistrictWriter.getFailedCounter();

                // Clean temp file
                if (tempFile.exists() && !tempFile.delete()) {
                    System.out.println("Warning: Temporary file could not be deleted");
                }

                // Reset counters
                SubDistrictWriter.setInsertedCounter(0);
                SubDistrictWriter.setFailedCounter(0);
            }

            // ✅ Response logic
            if (SubDistrictProcessor.getSubDistricts() == null) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("You already have all Sub-Districts in our records.")
                        .build();
            } else if (recordsUploaded == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("No records uploaded. " + failedRecords +
                                " Sub-Districts failed due to missing Districts, States, or Countries.")
                        .data(SubDistrictProcessor.getSubDistricts())
                        .build();
            } else if (failedRecords == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("All (" + recordsUploaded + ") Sub-District data uploaded successfully.")
                        .build();
            } else {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message(recordsUploaded + " Sub-District data partially uploaded. " +
                                failedRecords + " Sub-Districts failed due to missing Districts, States, or Countries.")
                        .data(SubDistrictProcessor.getSubDistricts())
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
    public LongResponse getTotalSubDistrict() {
        Long count = subDistrictRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }

}