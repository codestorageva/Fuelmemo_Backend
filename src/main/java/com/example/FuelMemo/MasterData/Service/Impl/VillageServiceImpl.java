package com.example.FuelMemo.MasterData.Service.Impl;


import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.MasterData.Config.spring_batch.village_batch.VillageProcessor;
import com.example.FuelMemo.MasterData.Config.spring_batch.village_batch.VillageWriter;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.VillageUpdateDto;
import com.example.FuelMemo.MasterData.Dto.VillageDto;
import com.example.FuelMemo.MasterData.Dto.VillageExportDTO;
import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import com.example.FuelMemo.MasterData.Entity.Village;
import com.example.FuelMemo.MasterData.Repository.*;
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
public class VillageServiceImpl implements VillageService {
    @Getter
    private static String userName;
    @PersistenceContext
    private final EntityManager entityManager;

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final DistrictRepository districtRepository;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final DataUtil dateUtil;
    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private final SubDistrictRepository subDistrictRepository;
    private final VillageRepository villageRepository;
    private final PermissionService permissionService;

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
//
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


    @Autowired
    public VillageServiceImpl(EntityManager entityManager, DistrictRepository districtRepository, StateRepository stateRepository,
                              CountryRepository countryRepository, DataUtil dateUtil,
                              JobLauncher jobLauncher, @Qualifier("villageReaderJob") Job job, SubDistrictRepository subDistrictRepository, VillageRepository villageRepository, PermissionService permissionService) {
        this.entityManager = entityManager;
        this.districtRepository = districtRepository;
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
        this.dateUtil = dateUtil;
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
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
//        this.royaltyPassRepository = royaltyPassRepository;
//        this.generalDetailsRepository = generalDetailsRepository;
//        this.deliveryChallanRepository = deliveryChallanRepository;
//        this.cdmWeighBridgeRepository = cdmWeighBridgeRepository;
//        this.transportMemoRepository = transportMemoRepository;
//        this.fuelMemoRepository = fuelMemoRepository;
    }

    @Override
    public MessageResponse addVillage(VillageDto villageDto, Map<String, String> headers) {
        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Permission check via service
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "WRITE_VILLAGE"
        );

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        villageDto.setVillageName(villageDto.getVillageName().trim());

        // HANDLE IF SUB-DISTRICT EXIST BY ID
        SubDistrict subDistrict = subDistrictRepository.findById(villageDto.getSubDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sub-District with ID '" + villageDto.getSubDistrictId() + "' not found!"
                ));

        String subDistrictName = subDistrict.getSubDistrictName();

        // HANDLE IF DUPLICATE VILLAGE NAME
        if (villageRepository.existsByVillageNameIgnoreCaseAndSubDistrict_SubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                villageDto.getVillageName(), subDistrictName,
                subDistrict.getDistrict().getCityName(),
                subDistrict.getState().getStateName(),
                subDistrict.getCountry().getCountryName()
        )) {
            throw new DuplicateEntryException("Village with name '" + villageDto.getVillageName() + "' already exist!");
        }

        // IS SUB-DISTRICT STATUS ACTIVE
        if (!subDistrict.getStatus()) {
            throw new InactiveStatusException("Sub-District '" + subDistrict.getSubDistrictName() + "' is inactive!");
        }

        Village village = new Village();
        village.setVillageName(villageDto.getVillageName());
        village.setCountry(subDistrict.getCountry());
        village.setState(subDistrict.getState());
        village.setDistrict(subDistrict.getDistrict());
        village.setSubDistrict(subDistrict);
        village.setStatus(villageDto.getStatus() != null ? villageDto.getStatus() : true);
        village.setCreatedAt(LocalDateTime.now());
        village.setUpdatedAt(LocalDateTime.now());
        village.setDeletedAt(null);
        village.setIsDeleted(false);
        village.setOperatorName(loggedInUser);

        villageRepository.save(village);

        return new MessageResponse(true, HttpStatus.OK, "Village saved.");
    }

    @Override
    public DataResponse getVillageById(int id, Map<String, String> headers) {
        String roleName = headers.get("rolename");

        // ✅ Permission check
        boolean hasPermission = permissionService.hasPermission(
                headers.get("authorization"),
                "READ_VILLAGE"
        );

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        Village village = villageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Village with ID '" + id + "' not found!"));

        return new DataResponse(true, HttpStatus.OK, dateUtil.villageToDto(village));
    }

    @Override
    public HttpResponse getAllVillages(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection,
                                       String isDeleted, String status, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Permission check using service instead of REST call
        boolean hasPermission = permissionService.hasPermission(headers.get("authorization"), "READ_VILLAGE");

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        // --- Existing CriteriaBuilder + paging logic untouched ---
        Page<Village> pageVillage = null;
        List<VillageDto> villages = null;
        Integer intKeyword = null;
        Boolean softDeleted = null;
        Boolean isStatus = null;

        if (isDeleted.equalsIgnoreCase("true")) softDeleted = Boolean.TRUE;
        else if (isDeleted.equalsIgnoreCase("false")) softDeleted = Boolean.FALSE;

        if (status.equalsIgnoreCase("true")) isStatus = Boolean.TRUE;
        else if (status.equalsIgnoreCase("false")) isStatus = Boolean.FALSE;

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Village> criteriaQuery = criteriaBuilder.createQuery(Village.class);
            Root<Village> root = criteriaQuery.from(Village.class);

            Join<Village, SubDistrict> subDistrictJoin = root.join("subDistrict");

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            // --- Existing predicates + query logic intact ---
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isStatus);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate subDistrictNotDeletedPredicate = criteriaBuilder.equal(subDistrictJoin.get("isDeleted"), Boolean.FALSE);
            Predicate subDistrictActivePredicate = criteriaBuilder.equal(subDistrictJoin.get("status"), Boolean.TRUE);
            Predicate districtActiveAndNotDeleted = criteriaBuilder.and(
                    criteriaBuilder.equal(subDistrictJoin.get("district").get("isDeleted"), Boolean.FALSE),
                    criteriaBuilder.equal(subDistrictJoin.get("district").get("status"), Boolean.TRUE)
            );
            Predicate stateActiveAndNotDeleted = criteriaBuilder.and(
                    criteriaBuilder.equal(subDistrictJoin.get("district").get("state").get("isDeleted"), Boolean.FALSE),
                    criteriaBuilder.equal(subDistrictJoin.get("district").get("state").get("status"), Boolean.TRUE)
            );
            Predicate countryActiveAndNotDeleted = criteriaBuilder.and(
                    criteriaBuilder.equal(subDistrictJoin.get("district").get("state").get("country").get("isDeleted"), Boolean.FALSE),
                    criteriaBuilder.equal(subDistrictJoin.get("district").get("state").get("country").get("status"), Boolean.TRUE)
            );
            Predicate villageIdPredicate = criteriaBuilder.equal(root.get("villageId"), intKeyword);
            Predicate villageAndSubDistrictAndDistrictAndStateAndCountryNamePredicate = null;

            if (keyword != null) {
                villageAndSubDistrictAndDistrictAndStateAndCountryNamePredicate =
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("villageName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("subDistrict").get("subDistrictName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("district").get("cityName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("state").get("stateName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("country").get("countryName").as(String.class)), "%" + keyword.toLowerCase() + "%")
                        );
            }

            Predicate combinedPredicate = statusPredicate;
            if (softDeleted != null) {
                if (intKeyword != null)
                    combinedPredicate = criteriaBuilder.and(combinedPredicate, villageIdPredicate, deletedPredicate, subDistrictActivePredicate, subDistrictNotDeletedPredicate, districtActiveAndNotDeleted, stateActiveAndNotDeleted, countryActiveAndNotDeleted);
                else if (keyword != null)
                    combinedPredicate = criteriaBuilder.and(combinedPredicate, villageAndSubDistrictAndDistrictAndStateAndCountryNamePredicate, deletedPredicate, subDistrictActivePredicate, subDistrictNotDeletedPredicate, districtActiveAndNotDeleted, stateActiveAndNotDeleted, countryActiveAndNotDeleted);
                else
                    combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate, subDistrictActivePredicate, subDistrictNotDeletedPredicate, districtActiveAndNotDeleted, stateActiveAndNotDeleted, countryActiveAndNotDeleted);
            }

            criteriaQuery.select(root).where(combinedPredicate);

            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            List<Village> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();
            pageVillage = new PageImpl<>(resultList, pageable, totalCount);

            villages = dateUtil.villagesToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageVillage.getNumber())
                .pageSize(pageVillage.getSize())
                .totalElements(pageVillage.getTotalElements())
                .totalPages(pageVillage.getTotalPages())
                .isLastPage(pageVillage.isLast())
                .data(villages)
                .build();
    }

    @Override
    public MessageResponse updateVillage(VillageUpdateDto villageDto, int villageId, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Permission check using service instead of REST call
        boolean hasPermission = permissionService.hasPermission(headers.get("authorization"), "UPDATE_VILLAGE");

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        // HANDLE IF VILLAGE EXIST BY ID
        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new ResourceNotFoundException("Village with ID '" + villageId + "' not found!"));

        String subDistrictName = village.getSubDistrict().getSubDistrictName();
        String cityName = village.getDistrict().getCityName();
        String stateName = village.getState().getStateName();
        String countryName = village.getCountry().getCountryName();

        // HANDLE IF DUPLICATE VILLAGE NAME
        if (villageDto.getVillageName() != null) {
            if (villageDto.getSubDistrictId() != null) {
                SubDistrict subDistrict = subDistrictRepository.findById(villageDto.getSubDistrictId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sub-District with ID '" + villageDto.getSubDistrictId() + "' not found!"));

                subDistrictName = subDistrict.getSubDistrictName();
                cityName = subDistrict.getDistrict().getCityName();
                stateName = subDistrict.getState().getStateName();
                countryName = subDistrict.getCountry().getCountryName();

                village.setSubDistrict(subDistrict);
            }

            Village villageWithSameName = villageRepository.findByVillageNameIgnoreCaseAndSubDistrict_SubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase
                    (villageDto.getVillageName().trim(), subDistrictName.trim(), cityName.trim(), stateName.trim(), countryName.trim());

            if (villageWithSameName != null && !villageWithSameName.getVillageId().equals(village.getVillageId()))
                throw new DuplicateEntryException("Village '" + villageDto.getVillageName() + "' already exist!");

            village.setVillageName(villageDto.getVillageName().trim());
        }

        if (villageDto.getStatus() != null)
            village.setStatus(villageDto.getStatus());

        village.setOperatorName(loggedInUser);
        village.setUpdatedAt(LocalDateTime.now());
        villageRepository.save(village);

        return new MessageResponse(true, HttpStatus.OK, "Village updated.");
    }

/*
    @Transactional
    @Override
    public MessageResponse deleteVillageById(int id, Map<String, String> headers) {
        try {
            String roleName = headers.get("rolename");

            // Define the URL to call the AUTH service
            String url = UriComponentsBuilder.fromHttpUrl("http://" + hostname + "/auth/role/checkPermission?permissionName=VILLAGE_DELETE").toUriString();

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
                Village village = villageRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Village with ID '" + id + "' not found!"));

//        List<MiningLease> miningLeases = new ArrayList<>();
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<MiningLease> criteriaQuery = criteriaBuilder.createQuery(MiningLease.class);
//            Root<MiningLease> root = criteriaQuery.from(MiningLease.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//                    .where(criteriaBuilder.equal(root.get("village"), village));
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
//        for (MiningLease m : miningLeaseRepository.findAllByVillage_VillageId(id))
//            miningLeaseRepository.delete(m);
//        for (QuarryLease m : quarryLeaseRepository.findAllByVillage_VillageId(id))
//            quarryLeaseRepository.delete(m);
//        for (StockRegistration m : stockRegistrationRepository.findAllByVillage_VillageId(id))
//            stockRegistrationRepository.delete(m);
//        for (WeighBridge m : weighBridgeRepository.findAllByVillage_VillageId(id))
//            weighBridgeRepository.delete(m);
//        for (Equipment m : equipmentRepository.findAllByVillage_VillageId(id))
//            equipmentRepository.delete(m);
//        for (Vehicle m : vehicleRepository.findAllByVillage_VillageId(id))
//            vehicleRepository.delete(m);
//        for (FuelStation m : fuelStationRepository.findAllByVillage_VillageId(id))
//            fuelStationRepository.delete(m);
//        for (MasterEntity m : masterEntityRepository.findAllByVillage_VillageId(id))
//            masterEntityService.hardDeleteEntity(m.getId());
//        for (MasterIndividual m : masterIndividualRepository.findAllByVillage_VillageId(id))
//            masterIndividualService.hardDeleteIndividual(m.getId());

                int rowsAffected = 0;

                try {
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    CriteriaDelete<Village> criteriaDelete = criteriaBuilder.createCriteriaDelete(Village.class);
                    Root<Village> root = criteriaDelete.from(Village.class);

                    criteriaDelete.where(
                            criteriaBuilder.equal(root.get("villageId"), village.getVillageId())
                    );
                    rowsAffected = entityManager.createQuery(criteriaDelete).executeUpdate();
                    System.out.println("Operation Performed Successfully " + rowsAffected + " rows Affected");

                } catch (Exception e) {
                    System.out.println("Error Occured " + e.getMessage());
                    System.out.println(e);
                }

                if (rowsAffected > 0 || rowsAffected == 1)
                    return new MessageResponse(true, HttpStatus.OK, "Data Deleted");

                villageRepository.delete(village);
                return new MessageResponse(true, HttpStatus.OK, "Village '" + village.getVillageName() + "' deleted!");

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
    public MessageResponse softDeleteVillageById(int villageId, Map<String, String> headers) {
        String roleName = headers.get("rolename");

        // ✅ Permission check
        boolean hasPermission = permissionService.hasPermission(headers.get("authorization"), "DELETE_VILLAGE");

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new ResourceNotFoundException("Village with ID '" + villageId + "' not found!"));

        village.setIsDeleted(true);
        village.setDeletedAt(LocalDateTime.now());
        villageRepository.save(village);

        return new MessageResponse(true, HttpStatus.OK, "Village with id '" + villageId + "' soft deleted!");
    }

    @Override
    public MessageResponse restoreVillageById(int villageId, Map<String, String> headers) {
        String roleName = headers.get("rolename");

        // ✅ Permission check
        boolean hasPermission = permissionService.hasPermission(headers.get("authorization"), "RESTORE_VILLAGE");

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        Village village = villageRepository.findById(villageId)
                .orElseThrow(() -> new ResourceNotFoundException("Village with ID '" + villageId + "' not found!"));

        village.setIsDeleted(false);
        village.setDeletedAt(null);
        villageRepository.save(village);

        return new MessageResponse(true, HttpStatus.OK, "Village with id '" + villageId + "' restored!");
    }


    @Override
    public ListResponse getAllVillagesByStatusAndIsDeleted(int subDistrictId, Map<String, String> headers) {
        List<VillageDto> villages = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Village> criteriaQuery = criteriaBuilder.createQuery(Village.class);
            Root<Village> root = criteriaQuery.from(Village.class);

            Predicate subDistrictIdPredicate = criteriaBuilder.equal(root.get("subDistrict").get("subDistrictId"), subDistrictId);
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), true);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);

            criteriaQuery.select(root)
                    .where(subDistrictIdPredicate, statusPredicate, deletedPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("villageName")));

            // Fetch results for the current page
            List<Village> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            // Convert the resultList to DTOs
            villages = dateUtil.villagesToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true,HttpStatus.OK, villages);

    }

    @Override
    public ListResponse exportedVillageData(Map<String, String> headers) {
        List<VillageExportDTO> villages = new ArrayList<>();
        String roleName = headers.get("rolename");

        // ✅ Use permissionService instead of REST call
        boolean hasPermission = permissionService.hasPermission(headers.get("authorization"), "EXPORT_VILLAGE");

        if (!(roleName.equals("ROLE_SUPERADMIN") || hasPermission)) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Village> criteriaQuery = criteriaBuilder.createQuery(Village.class);
            Root<Village> root = criteriaQuery.from(Village.class);

            criteriaQuery.select(root)
                    .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                    .orderBy(criteriaBuilder.asc(root.get("villageName")));

            List<Village> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            villages = dateUtil.villageExportDTOStoVillages(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, villages);
    }

    @Override
    public MessageListResponse uploadVillageCSV(MultipartFile file, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String email = headers.get("email"); // extract from headers or token
        String roleName = headers.get("rolename");

        // ✅ Permission check
        boolean hasPermission = false;
        try {
            if ("ROLE_SUPERADMIN".equalsIgnoreCase(roleName)) {
                hasPermission = true; // superadmin bypass
            } else {
                hasPermission = permissionService.hasPermission(email, "IMPORT_VILLAGE");
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
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
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
                            LocalTime.now().format(timeFormatter) + "_Village_temp",
                    ".csv"
            );

            file.transferTo(tempFile);
            userName = loggedInUser; // For audit

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFileVillage", tempFile.getAbsolutePath())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            long recordsUploaded = 0;
            long failedRecords = 0;

            if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                System.out.println("Village CSV Job Completed...");
                recordsUploaded = VillageWriter.getInsertedCounter();
                failedRecords = VillageWriter.getFailedCounter();

                // Clean temp file
                if (tempFile.exists() && !tempFile.delete()) {
                    System.out.println("Warning: Temporary file could not be deleted");
                }

                // Reset counters
                VillageWriter.setInsertedCounter(0);
                VillageWriter.setFailedCounter(0);
            }

            // ✅ Response logic
            if (VillageProcessor.getVillages() == null) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("You already have all Villages in our records.")
                        .build();
            } else if (recordsUploaded == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("No records uploaded. " + failedRecords +
                                " Villages failed due to missing Sub-Districts, Districts, States, or Countries.")
                        .data(VillageProcessor.getVillages())
                        .build();
            } else if (failedRecords == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("All (" + recordsUploaded + ") Village data uploaded successfully.")
                        .build();
            } else {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message(recordsUploaded + " Village data partially uploaded. " +
                                failedRecords + " Villages failed due to missing Sub-Districts, Districts, States, or Countries.")
                        .data(VillageProcessor.getVillages())
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
    public LongResponse getTotalVillage() {
        Long count = villageRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }

}
