package com.example.FuelMemo.MasterData.Service.Impl;


import com.example.FuelMemo.AuthModule.Service.PermissionService;
import com.example.FuelMemo.MasterData.Config.spring_batch.state_batch.StateProcessor;
import com.example.FuelMemo.MasterData.Config.spring_batch.state_batch.StateWriter;
import com.example.FuelMemo.MasterData.Dto.StateDto;
import com.example.FuelMemo.MasterData.Dto.StateExportDTO;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.StateUpdateDto;
import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.*;
import com.example.FuelMemo.MasterData.Service.DistrictService;
import com.example.FuelMemo.MasterData.Service.StateService;
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
public class StateServiceImpl implements StateService {

    @Getter
    private static String userName;
    @PersistenceContext
    private final EntityManager entityManager;

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;
    private final DataUtil dateUtil;
    private final JobLauncher jobLauncher;
    private final Job job;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

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
//    private FuelMemoRepository fuelMemoRepository;

    private final DistrictRepository districtRepository;

    private final SubDistrictRepository subDistrictRepository;

    private final VillageRepository villageRepository;

    private final DistrictService districtService;

    private final SubDistrictService subDistrictService;

    private final VillageService villageService;
    private final PermissionService permissionService;


    @Autowired
    public StateServiceImpl(EntityManager entityManager, StateRepository stateRepository, CountryRepository countryRepository,
                            DataUtil dateUtil, JobLauncher jobLauncher, @Qualifier("stateReaderJob") Job job,
                            DistrictRepository districtRepository, SubDistrictRepository subDistrictRepository, VillageRepository villageRepository, DistrictService districtService, SubDistrictService subDistrictService, VillageService villageService, PermissionService permissionService) throws IOException {
        this.entityManager = entityManager;
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
        this.dateUtil = dateUtil;
        this.jobLauncher = jobLauncher;
        this.job = job;
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

        this.districtRepository = districtRepository;
        this.subDistrictRepository = subDistrictRepository;
        this.villageRepository = villageRepository;
        this.districtService = districtService;
        this.subDistrictService = subDistrictService;
        this.villageService = villageService;
    }

    //----------------------------------------------------SERVICE METHODS-----------------------------------------------
    @Override
    public MessageResponse addState(StateDto stateDto, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Permission check using PermissionService instead of REST call
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "WRITE_STATE");

        if (hasPermission) {
            stateDto.setStateName(stateDto.getStateName().trim());

            //  HANDLE IF COUNTRY IS NULL
            Country country = countryRepository.findById(stateDto.getCountryId()).orElseThrow(
                    () -> new ResourceNotFoundException("Country with ID '" + stateDto.getCountryId() + "' not found!")
            );

            //  HANDLE DUPLICATE ENTRY STATE NAME
            if (stateRepository.existsByStateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                    stateDto.getStateName(),
                    country.getCountryName()
            )) {
                throw new DuplicateEntryException("State with name '" + stateDto.getStateName() + "' already exist!");
            }

            //  IS COUNTRY STATUS ACTIVE ?
            if (!country.getStatus())
                throw new InactiveStatusException("Country with id '" + stateDto.getCountryId() + "' is not active!");

            State state = new State();
            state.setStateName(stateDto.getStateName());
            state.setCountry(country);
            state.setStatus(stateDto.getStatus() != null ? stateDto.getStatus() : true);
            state.setCreatedAt(LocalDateTime.now());
            state.setUpdatedAt(LocalDateTime.now());
            state.setDeletedAt(null);
            state.setIsDeleted(false);
            state.setOperatorName(loggedInUser);

            stateRepository.save(state);
            return new MessageResponse(true, HttpStatus.OK, "State saved.");
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public DataResponse getStateById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Permission check using PermissionService
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "READ_STATE");

        if (!hasPermission) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        // ------------------- Existing logic -------------------
        return stateRepository.findById(id)
                .map(state -> new DataResponse(true, HttpStatus.OK, dateUtil.stateToDto(state)))
                .orElseThrow(() -> new ResourceNotFoundException("State with id '" + id + "' not found!"));
    }


    @Override
    public HttpResponse getAllStates(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers) {

        Page<State> pageState = null;
        List<StateDto> states = null;

        Integer intKeyword = null;
        Boolean softDeleted = null;
        Boolean isStatus = null;

        if (isDeleted.equalsIgnoreCase("true")) {
            softDeleted = Boolean.TRUE;
        } else if (isDeleted.equalsIgnoreCase("false")) {
            softDeleted = Boolean.FALSE;
        }

        if (status.equalsIgnoreCase("true")) {
            isStatus = Boolean.TRUE;
        } else if (status.equalsIgnoreCase("false")) {
            isStatus = Boolean.FALSE;
        }

        try {
            intKeyword = Integer.parseInt(keyword);
        } catch (Exception ignored) {
        }

        String roleName = headers.get("rolename");

        // ✅ Permission check using PermissionService instead of REST call
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "READ_STATE");

        if (!hasPermission) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        // ------------------- Existing logic unchanged -------------------
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<State> criteriaQuery = criteriaBuilder.createQuery(State.class);
            Root<State> root = criteriaQuery.from(State.class);

            Join<State, Country> countryJoin = root.join("country");

            Order order = sortDirection.equalsIgnoreCase("asc") ?
                    criteriaBuilder.asc(root.get(sortBy)) :
                    criteriaBuilder.desc(root.get(sortBy));
            criteriaQuery.orderBy(order);

            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), isStatus);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), softDeleted);
            Predicate countryNotDeletedPredicate = criteriaBuilder.equal(countryJoin.get("isDeleted"), Boolean.FALSE);
            Predicate countryActivePredicate = criteriaBuilder.equal(countryJoin.get("status"), Boolean.TRUE);
            Predicate stateIdPredicate = criteriaBuilder.equal(root.get("stateId"), intKeyword);
            Predicate stateAndCountryNamePredicate = null;

            if (keyword != null) {
                stateAndCountryNamePredicate =
                        criteriaBuilder.or(
                                criteriaBuilder.like(criteriaBuilder.lower(root.get("stateName").as(String.class)), "%" + keyword.toLowerCase() + "%"),
                                criteriaBuilder.like(criteriaBuilder.lower(countryJoin.get("countryName").as(String.class)), "%" + keyword.toLowerCase() + "%")
                        );
            }

            Predicate combinedPredicate = null;

            if (isStatus != null) {
                combinedPredicate = statusPredicate;

                if (softDeleted != null) {
                    if (intKeyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, stateIdPredicate, deletedPredicate, countryActivePredicate, countryNotDeletedPredicate);
                    } else if (keyword != null) {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, stateAndCountryNamePredicate, deletedPredicate, countryActivePredicate, countryNotDeletedPredicate);
                    } else {
                        combinedPredicate = criteriaBuilder.and(combinedPredicate, deletedPredicate, countryActivePredicate, countryNotDeletedPredicate);
                    }
                }
            }

            criteriaQuery.select(root).where(combinedPredicate);

            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            List<State> resultList = entityManager.createQuery(criteriaQuery)
                    .setFirstResult((int) pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();

            long totalCount = entityManager.createQuery(criteriaQuery).getResultList().size();
            pageState = new PageImpl<>(resultList, pageable, totalCount);

            states = dateUtil.statesToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .pageNumber(pageState.getNumber())
                .pageSize(pageState.getSize())
                .totalElements(pageState.getTotalElements())
                .totalPages(pageState.getTotalPages())
                .isLastPage(pageState.isLast())
                .data(states)
                .build();
    }


    @Override
    public MessageResponse updateState(StateUpdateDto stateDto, int id, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String roleName = headers.get("rolename");

        // ✅ Permission check using PermissionService instead of REST call
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "UPDATE_STATE");

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            //  HANDLE IF STATE EXIST BY ID
            State state = stateRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("State with id '" + id + "' not found!"));

            if (stateDto.getStateName() != null) {
                State stateWithSameName = stateRepository.findByStateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(
                        stateDto.getStateName().trim(),
                        state.getCountry().getCountryName()
                );

                if (stateWithSameName != null && !stateWithSameName.getStateId().equals(state.getStateId()))
                    throw new DuplicateEntryException("State '" + stateDto.getStateName() + "' already exist!");

                state.setStateName(stateDto.getStateName().trim());
            }

            if (stateDto.getCountryId() != null) {
                Country country = countryRepository.findById(stateDto.getCountryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Country with ID '" + stateDto.getCountryId() + "' not found!"));

                // state.setCountry(country); // logic unchanged
            }

            if (stateDto.getStatus() != null)
                state.setStatus(stateDto.getStatus());

            state.setUpdatedAt(LocalDateTime.now());
            state.setOperatorName(loggedInUser);
            stateRepository.save(state);

            return new MessageResponse(true, HttpStatus.OK, "State updated.");
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

/*
    @Transactional
    @Override
    public MessageResponse deleteStateById(int id, Map<String, String> headers) {

            String roleName = headers.get("rolename");

            // Define the URL to call the AUTH service
            String url = UriComponentsBuilder.fromHttpUrl("http://" + hostname + "/auth/role/checkPermission?permissionName=STATE_DELETE").toUriString();

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
                State state = stateRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("State with id '" + id + "' not found!"));

                for (District d : districtRepository.findAllByState_StateId(state.getStateId()))
                    districtService.deleteDistrictById(d.getDistrictId(), headers);
                for (SubDistrict sd : subDistrictRepository.findAllByState_StateId(state.getStateId()))
                    subDistrictService.deleteSubDistrictById(sd.getSubDistrictId(), headers);
                for (Village v : villageRepository.findAllByState_StateId(state.getStateId()))
                    villageService.deleteVillageById(v.getVillageId(), headers);

//        List<MiningLease> miningLeases = new ArrayList<>();
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<MiningLease> criteriaQuery = criteriaBuilder.createQuery(MiningLease.class);
//            Root<MiningLease> root = criteriaQuery.from(MiningLease.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//
//        List<QuarryLease> quarryLeases = new ArrayList<>();
//
//        try {
//            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
//            CriteriaQuery<QuarryLease> criteriaQuery = criteriaBuilder.createQuery(QuarryLease.class);
//            Root<QuarryLease> root = criteriaQuery.from(QuarryLease.class);
//
//            criteriaQuery.select(root)
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//                    .where(criteriaBuilder.equal(root.get("state"), state));
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
//        for (MiningLease m : miningLeaseRepository.findAllByState_StateId(id))
//            miningLeaseRepository.delete(m);
//        for (QuarryLease m : quarryLeaseRepository.findAllByState_StateId(id))
//            quarryLeaseRepository.delete(m);
//        for (StockRegistration m : stockRegistrationRepository.findAllByState_StateId(id))
//            stockRegistrationRepository.delete(m);
//        for (WeighBridge m : weighBridgeRepository.findAllByState_StateId(id))
//            weighBridgeRepository.delete(m);
//        for (Equipment m : equipmentRepository.findAllByState_StateId(id))
//            equipmentRepository.delete(m);
//        for (Vehicle m : vehicleRepository.findAllByState_StateId(id))
//            vehicleRepository.delete(m);
//        for (FuelStation m : fuelStationRepository.findAllByState_StateId(id))
//            fuelStationRepository.delete(m);
//        for (MasterEntity m : masterEntityRepository.findAllByState_StateId(id))
//            masterEntityService.hardDeleteEntity(m.getId());
//        for (MasterIndividual m : masterIndividualRepository.findAllByState_StateId(id))
//            masterIndividualService.hardDeleteIndividual(m.getId());

                int rowsAffected = 0;

                try {
                    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                    CriteriaDelete<State> criteriaDelete = criteriaBuilder.createCriteriaDelete(State.class);
                    Root<State> root = criteriaDelete.from(State.class);

                    criteriaDelete.where(
                            criteriaBuilder.equal(root.get("stateId"), state.getStateId())
                    );
                    rowsAffected = entityManager.createQuery(criteriaDelete).executeUpdate();
                    System.out.println("Operation Performed Successfully " + rowsAffected + " rows Affected");

                } catch (Exception e) {
                    System.out.println("Error Occured " + e.getMessage());
                    System.out.println(e);
                }

                if (rowsAffected > 0 || rowsAffected == 1)
                    return new MessageResponse(true, HttpStatus.OK, "Data Deleted");

//        stateRepository.delete(state);
                return new MessageResponse(true, HttpStatus.OK, "State with id " + id + "' deleted.");
            } else {
                throw new UserUnauthorizedException("You don't have permission to perform this operation.");
            }
    }
*/

    @Override
    public MessageResponse softDeleteStateById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        // ✅ Permission check using PermissionService instead of REST call
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "DELETE_STATE");

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            State state = stateRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("State with id '" + id + "' not found!"));
            state.setIsDeleted(true);
            state.setDeletedAt(LocalDateTime.now());
            stateRepository.save(state);

            return new MessageResponse(true, HttpStatus.OK, "State with id " + id + "' Soft deleted!");
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public MessageResponse restoreStateById(int id, Map<String, String> headers) {

        String roleName = headers.get("rolename");

        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "RESTORE_STATE");

        if (roleName.equals("ROLE_SUPERADMIN") || hasPermission) {
            State state = stateRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("State with id '" + id + "' not found!"));
            state.setIsDeleted(false);
            state.setDeletedAt(null);
            stateRepository.save(state);

            return new MessageResponse(true, HttpStatus.OK, "State with id " + id + "' Restored!");
        } else {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }
    }

    @Override
    public MessageListResponse uploadStateCSV(MultipartFile file, Map<String, String> headers) {

        String loggedInUser = headers.get("loggedinuser");
        String email = headers.get("email"); // Extract email from headers or token
        String roleName = headers.get("rolename");

        System.out.println("Uploading State CSV by user: " + loggedInUser + " | Email: " + email + " | Role: " + roleName);

        boolean hasPermission = false;
        try {
            if ("ROLE_SUPERADMIN".equalsIgnoreCase(roleName)) {
                hasPermission = true; // superadmin bypass
            } else {
                hasPermission = permissionService.hasPermission(email, "IMPORT_STATE");
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
                            LocalTime.now().format(timeFormatter) + "_State_" + "temp",
                    ".csv"
            );

            file.transferTo(tempFile);
            userName = loggedInUser; // For audit

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("inputFileState", tempFile.getAbsolutePath())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            long recordsUploaded = 0;
            long failedRecords = 0;

            if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
                System.out.println("State CSV Job Completed...");
                recordsUploaded = StateWriter.getInsertedCounter();
                failedRecords = StateWriter.getFailedCounter();

                if (tempFile.exists() && !tempFile.delete()) {
                    System.out.println("Warning: Temporary file could not be deleted");
                }

                StateWriter.setInsertedCounter(0);
                StateWriter.setFailedCounter(0);
            }

            if (StateProcessor.getCountries() == null) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("You already have all states in our records.")
                        .build();
            } else if (recordsUploaded == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("No records uploaded. " + failedRecords + " states failed due to missing countries.")
                        .data(StateProcessor.getCountries())
                        .build();
            } else if (failedRecords == 0) {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message("All (" + recordsUploaded + ") state data uploaded successfully.")
                        .build();
            } else {
                return MessageListResponse.builder()
                        .success(true)
                        .successCode(HttpStatus.OK)
                        .message(recordsUploaded + " state data partially uploaded. " +
                                failedRecords + " states failed due to missing countries.")
                        .data(StateProcessor.getCountries())
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
    public ListResponse getAllStatesByStatusAndIsDeleted(Map<String, String> headers) {
        List<StateDto> states = null;

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<State> criteriaQuery = criteriaBuilder.createQuery(State.class);
            Root<State> root = criteriaQuery.from(State.class);

            Predicate countryIdPredicate = criteriaBuilder.equal(root.get("country").get("countryId"), 196);
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), true);
            Predicate deletedPredicate = criteriaBuilder.equal(root.get("isDeleted"), false);

            criteriaQuery.select(root)
                    .where(countryIdPredicate, statusPredicate, deletedPredicate)
                    .orderBy(criteriaBuilder.asc(root.get("stateName")));

            List<State> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            states = dateUtil.statesToDtos(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, states);
    }

    @Override
    public ListResponse exportedStateData(Map<String, String> headers) {
        List<StateExportDTO> states = new ArrayList<>();

        String roleName = headers.get("rolename");
        boolean hasPermission = "ROLE_SUPERADMIN".equals(roleName)
                || permissionService.hasPermission(headers.get("authorization"), "EXPORT_STATE");

        if (!hasPermission) {
            throw new UserUnauthorizedException("You don't have permission to perform this operation.");
        }

        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<State> criteriaQuery = criteriaBuilder.createQuery(State.class);
            Root<State> root = criteriaQuery.from(State.class);

            criteriaQuery.select(root)
                    .where(criteriaBuilder.equal(root.get("isDeleted"), false))
                    .orderBy(criteriaBuilder.asc(root.get("stateName")));

            List<State> resultList = entityManager.createQuery(criteriaQuery).getResultList();

            states = dateUtil.stateExportDTOStoStates(resultList);

        } catch (NoResultException ignored) {
        }

        return new ListResponse(true, HttpStatus.OK, states);
    }

    @Override
    public LongResponse getTotalState() {
        Long count = stateRepository.count();
        return new LongResponse(true, HttpStatus.OK, count);
    }

}