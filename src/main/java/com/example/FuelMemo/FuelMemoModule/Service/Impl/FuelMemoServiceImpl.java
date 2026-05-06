package com.example.FuelMemo.FuelMemoModule.Service.Impl;

import ch.qos.logback.classic.Logger;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.AuthModule.Repository.UserRepository;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.DriverModule.Entity.Driver;
import com.example.FuelMemo.DriverModule.Repository.DriverRepository;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoCreateResponse;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoRequestDto;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoResponseDto;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoUpdateRequestDto;
import com.example.FuelMemo.FuelMemoModule.Entity.EntryType;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMonth;
import com.example.FuelMemo.FuelMemoModule.Repository.FuelMemoRepository;
import com.example.FuelMemo.FuelMemoModule.Repository.FuelMonthRepository;
import com.example.FuelMemo.FuelMemoModule.Service.FuelMemoService;
import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
import com.example.FuelMemo.FuelStationModule.Repository.FuelStationRepository;
import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import com.example.FuelMemo.RoyaltyModule.Entity.RoyaltyPass;
import com.example.FuelMemo.Shared.Exception.InactiveStatusException;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import com.example.FuelMemo.SiteModule.Entity.Site;
import com.example.FuelMemo.SiteModule.Repository.SiteRepository;
import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import com.example.FuelMemo.VehicleModule.Repository.VehicleRepository;
import com.example.FuelMemo.RoyaltyModule.Repository.RoyaltyPassRepository;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class FuelMemoServiceImpl implements FuelMemoService {

    private final FuelMemoRepository fuelMemoRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final SiteRepository siteRepository;
    private final FuelStationRepository fuelStationRepository;
    private final CompanyRepository companyRepository;
    private final RoyaltyPassRepository royaltyPassRepository;
    private final FuelMonthRepository fuelMonthRepository;
    private final EntityManager entityManager;
    private final DataUtil dataUtil;

    public FuelMemoServiceImpl(FuelMemoRepository fuelMemoRepository, VehicleRepository vehicleRepository, DriverRepository driverRepository, SiteRepository siteRepository, FuelStationRepository fuelStationRepository, CompanyRepository companyRepository, RoyaltyPassRepository royaltyPassRepository, FuelMonthRepository fuelMonthRepository, EntityManager entityManager, DataUtil dataUtil, UserRepository userRepository) {
        this.fuelMemoRepository = fuelMemoRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.siteRepository = siteRepository;
        this.fuelStationRepository = fuelStationRepository;
        this.companyRepository = companyRepository;
        this.royaltyPassRepository = royaltyPassRepository;
        this.fuelMonthRepository = fuelMonthRepository;
        this.entityManager = entityManager;
        this.dataUtil = dataUtil;
        this.userRepository = userRepository;
    }

    private final UserRepository userRepository;



    @Transactional
    @Override
    public FuelMemoCreateResponse createFuelMemo(FuelMemoRequestDto request) {

        // ---------------- AUTH USER ----------------
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginInput = authentication.getName();

        User loggedInUser = Optional
                .ofNullable(userRepository.findByUserNameOrEmail(loginInput, loginInput))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ---------------- FETCH REQUIRED ENTITIES ----------------
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        FuelStation fuelStation = fuelStationRepository.findById(request.getFuelStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Fuel station not found"));

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // ---------------- VALIDATE MONTH ----------------
        validateMonthOpen(request.getDate(), company.getCompanyId());

        int month = request.getDate().getMonthValue();
        int year = request.getDate().getYear();

        FuelMonth fuelMonth = fuelMonthRepository
                .findByMonthAndYearAndCompany_CompanyIdAndDeletedFalse(
                        month, year, company.getCompanyId()
                )
                .orElseGet(() -> {
                    FuelMonth newMonth = FuelMonth.builder()
                            .month(month)
                            .year(year)
                            .company(company)
                            .closed(false)
                            .deleted(false)
                            .build();
                    return fuelMonthRepository.save(newMonth);
                });

        // ---------------- GENERATE SR NO ----------------
        Integer srNo = generateSrNo(company.getCompanyId(), request.getDate());

        // ---------------- ROYALTY PASS LOGIC ----------------
        RoyaltyPass royaltyPass = null;

        if (request.getRoyaltyPassId() != null) {

            royaltyPass = royaltyPassRepository.findById(request.getRoyaltyPassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Royalty pass not found"));

            // ❌ Prevent using deleted pass
            if (Boolean.TRUE.equals(royaltyPass.getDeleted())) {
                throw new InactiveStatusException("Cannot use deleted Royalty Pass");
            }

            // ❌ Prevent already used pass
            if (royaltyPass.getStatus() == PassStatus.USED) {
                throw new InactiveStatusException("Royalty Pass already used");
            }



            if (request.getQuantity() == null ||
                    request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }

            // ❌ Extra safety check (DB level)
            boolean alreadyUsed = fuelMemoRepository
                    .existsByRoyaltyPass_RoyaltyIdAndDeletedFalse(royaltyPass.getRoyaltyId());

            if (alreadyUsed) {
                throw new InactiveStatusException("This Royalty Pass is already used!");
            }

            // ✅ Mark as used
            royaltyPass.setStatus(PassStatus.USED);
            royaltyPass.setUpdatedAt(LocalDateTime.now());
            royaltyPassRepository.save(royaltyPass);
        }

        // ---------------- CREATE FUEL MEMO ----------------
        FuelMemo memo = FuelMemo.builder()
                .srNo(srNo)
                .date(request.getDate())
                .quantity(request.getQuantity())
                .user(loggedInUser)
                .vehicle(vehicle)
                .driver(driver)
                .site(site)
                .fuelStation(fuelStation)
                .company(company)
                .fuelMonth(fuelMonth)
                .royaltyPass(royaltyPass)
                .entryType(EntryType.NORMAL)
                .active(true)
                .deleted(false)
                .build();

        fuelMemoRepository.save(memo);

        // ---------------- RESPONSE ----------------
        return FuelMemoCreateResponse.builder()
                .success(true)
                .message("Fuel Memo Created Successfully")
                .srNo(srNo)
                .successCode(HttpStatus.CREATED)
                .build();
    }
    @Transactional
    @Override
    public MessageResponse updateFuelMemo(Integer id, FuelMemoUpdateRequestDto request) {

        // ================= FETCH MEMO =================
        FuelMemo memo = fuelMemoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FuelMemo not found with ID: " + id));

        // ================= VALIDATIONS =================
        if (Boolean.TRUE.equals(memo.getDeleted())) {
            throw new IllegalStateException("FuelMemo already deleted");
        }

        if (memo.getEntryType() == EntryType.ADJUSTMENT) {
            throw new IllegalStateException("Adjustment memo cannot be updated");
        }

        // ================= COMPANY =================
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        // ================= MONTH VALIDATION =================
        validateMonthOpen(memo.getDate(), memo.getCompany().getCompanyId());

        boolean isDateChanged = !memo.getDate().equals(request.getDate());

        if (isDateChanged) {
            validateMonthOpen(request.getDate(), company.getCompanyId());
        }

        // ================= FETCH RELATIONS =================
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        FuelStation fuelStation = fuelStationRepository.findById(request.getFuelStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Fuel Station not found"));

        // ================= ROYALTY PASS HANDLING =================
        RoyaltyPass oldPass = memo.getRoyaltyPass();
        RoyaltyPass newPass = null;

        if (request.getRoyaltyPassId() != null) {

            newPass = royaltyPassRepository.findById(request.getRoyaltyPassId())
                    .orElseThrow(() -> new ResourceNotFoundException("Royalty Pass not found"));

            if (Boolean.TRUE.equals(newPass.getDeleted())) {
                throw new InactiveStatusException("Cannot use deleted Royalty Pass");
            }

            boolean alreadyUsed =
                    fuelMemoRepository.existsByRoyaltyPass_RoyaltyIdAndFuelMemoIdNotAndDeletedFalse(
                            newPass.getRoyaltyId(), memo.getFuelMemoId());

            if (alreadyUsed) {
                throw new InactiveStatusException("Royalty Pass already used");
            }
        }

        // 🔥 HANDLE CHANGE
        if (oldPass != null && (newPass == null || !oldPass.getRoyaltyId().equals(newPass.getRoyaltyId()))) {
            oldPass.setStatus(PassStatus.AVAILABLE);
            royaltyPassRepository.save(oldPass);
        }

        if (newPass != null && (oldPass == null || !newPass.getRoyaltyId().equals(oldPass.getRoyaltyId()))) {
            newPass.setStatus(PassStatus.USED);
            royaltyPassRepository.save(newPass);
        }

        // ================= QUANTITY =================
        if (request.getQuantity() == null ||
                request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // ================= SR NO =================
        if (isDateChanged) {
            Integer srNo = generateSrNo(company.getCompanyId(), request.getDate());
            memo.setSrNo(srNo);
        }

        // ================= USER =================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginInput = authentication.getName();

        User loggedInUser = Optional
                .ofNullable(userRepository.findByUserNameOrEmail(loginInput, loginInput))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ================= UPDATE =================
        memo.setDate(request.getDate());
        memo.setQuantity(request.getQuantity());
        memo.setUser(loggedInUser);
        memo.setVehicle(vehicle);
        memo.setDriver(driver);
        memo.setSite(site);
        memo.setUpdatedAt(LocalDateTime.now());
        memo.setFuelStation(fuelStation);
        memo.setCompany(company);
        memo.setRoyaltyPass(newPass);

        fuelMemoRepository.save(memo);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("FuelMemo Updated Successfully")
                .build();
    }

    private Integer generateSrNo(Integer companyId, LocalDate date) {

        LocalDate startDate = date.withDayOfMonth(1);
        LocalDate endDate = date.withDayOfMonth(date.lengthOfMonth());

        FuelMemo lastMemo = fuelMemoRepository
                .findTopByCompany_CompanyIdAndDateBetweenAndDeletedFalseOrderBySrNoDesc(
                        companyId,
                        startDate,
                        endDate
                );

        if (lastMemo == null) {
            return 1;
        }

        return lastMemo.getSrNo() + 1;
    }

//    private void validateMonthOpen(LocalDate date) {
//
//        if (date == null) {
//            throw new RuntimeException("Date must not be null");
//        }
//
//        int month = date.getMonthValue();
//        int year = date.getYear();
//
//        FuelMonth fuelMonth = fuelMonthRepository
//                .findByMonthAndYearAndDeletedFalse(month, year);
//
//        // ✅ FINAL LOGIC
//        if (Boolean.TRUE.equals(fuelMonth.getClosed())
//                && fuelMonth.getReopenedAt() == null) {
//
//            throw new RuntimeException(
//                    "Fuel month is closed for " + month + "/" + year
//            );
//        }
//    }

    private void validateMonthOpen(LocalDate date, Integer companyId) {

        if (date == null) {
            throw new RuntimeException("Date must not be null");
        }

        int month = date.getMonthValue();
        int year = date.getYear();

        FuelMonth fuelMonth = fuelMonthRepository
                .findByMonthAndYearAndCompany_CompanyIdAndDeletedFalse(
                        month, year, companyId
                )
                .orElse(null); // ✅ FIX

        // 👉 If month not created → treat as OPEN
        if (fuelMonth == null) {
            return;
        }

        // ✅ FINAL LOGIC
        if (Boolean.TRUE.equals(fuelMonth.getClosed())
                && fuelMonth.getReopenedAt() == null) {

            throw new RuntimeException(
                    "Fuel month is closed for " + month + "/" + year
            );
        }
    }
    @Transactional
    @Override
    public MessageResponse createAdjustmentEntry(FuelMemoRequestDto request) {

        // ================= VALIDATE DATE =================
        if (request.getDate() == null) {
            throw new IllegalArgumentException("Date must not be null");
        }

        int month = request.getDate().getMonthValue();
        int year = request.getDate().getYear();

        // ================= USER =================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginInput = authentication.getName();

        User loggedInUser = Optional
                .ofNullable(userRepository.findByUserNameOrEmail(loginInput, loginInput))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ================= COMPANY =================
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new InactiveStatusException("Company is inactive");
        }

        // ================= FUEL MONTH =================
        FuelMonth fuelMonth = fuelMonthRepository
                .findByMonthAndYearAndCompany_CompanyIdAndDeletedFalse(
                        month, year, company.getCompanyId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Fuel month not configured"));

        // ✅ Correct rule:
        if (!Boolean.TRUE.equals(fuelMonth.getClosed())) {
            throw new IllegalStateException("Adjustment allowed only for CLOSED month");
        }

        // ================= FETCH RELATIONS =================
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found"));

        FuelStation fuelStation = fuelStationRepository.findById(request.getFuelStationId())
                .orElseThrow(() -> new ResourceNotFoundException("Fuel station not found"));

        // ================= ROYALTY PASS =================
        // ❌ Adjustment should NOT use royalty pass
        if (request.getRoyaltyPassId() != null) {
            throw new IllegalStateException("Royalty Pass is not allowed in Adjustment Entry");
        }

        // ================= QUANTITY =================
        if (request.getQuantity() == null ||
                request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // ================= SR NO =================
        Integer srNo = generateSrNo(company.getCompanyId(), request.getDate());

        // ================= CREATE =================
        FuelMemo memo = FuelMemo.builder()
                .srNo(srNo)
                .date(request.getDate())
                .quantity(request.getQuantity())
                .user(loggedInUser)
                .vehicle(vehicle)
                .driver(driver)
                .site(site)
                .fuelStation(fuelStation)
                .company(company)
                .fuelMonth(fuelMonth)
                .royaltyPass(null) // 🔥 Important
                .entryType(EntryType.ADJUSTMENT)
                .active(true)
                .deleted(false)
                .build();

        fuelMemoRepository.save(memo);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.CREATED)
                .message("Adjustment Entry Created Successfully")
                .build();
    }
    @Override
    public MessageResponse deleteFuelMemo(Integer id) {

        FuelMemo memo = fuelMemoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FuelMemo not found with ID: " + id));

        // Already deleted check
        if (Boolean.TRUE.equals(memo.getDeleted())) {
            throw new InactiveStatusException("FuelMemo already deleted");
        }

        // Optional: Adjustment delete block
        if (memo.getEntryType() == EntryType.ADJUSTMENT) {
            throw new RuntimeException("Adjustment memo cannot be deleted");
        }
        if (memo.getRoyaltyPass() != null) {
            RoyaltyPass pass = memo.getRoyaltyPass();
            pass.setStatus(PassStatus.AVAILABLE);
            royaltyPassRepository.save(pass);
        }

        // Month closed validation
        validateMonthOpen(memo.getDate(), memo.getCompany().getCompanyId());

        memo.setDeleted(true);
        memo.setUpdatedAt(LocalDateTime.now());

        fuelMemoRepository.save(memo);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("FuelMemo deleted successfully with ID: " + id)
                .build();
    }
    @Override
    public MessageResponse restoreFuelMemo(Integer id) {

        FuelMemo memo = fuelMemoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("FuelMemo not found with ID: " + id));

        if (!Boolean.TRUE.equals(memo.getDeleted())) {
            throw new IllegalStateException("FuelMemo is not deleted");
        }

        // Month closed validation
        validateMonthOpen(memo.getDate(), memo.getCompany().getCompanyId());

        // ✅ Handle Royalty Pass safely
        if (memo.getRoyaltyPass() != null) {

            RoyaltyPass pass = memo.getRoyaltyPass();

            boolean alreadyUsed = fuelMemoRepository
                    .existsByRoyaltyPass_RoyaltyIdAndDeletedFalseAndFuelMemoIdNot(
                            pass.getRoyaltyId(), memo.getFuelMemoId()
                    );

            if (alreadyUsed) {
                throw new IllegalStateException("Royalty pass already used by another memo");
            }

            pass.setStatus(PassStatus.USED);
            royaltyPassRepository.save(pass);
        }

        memo.setDeleted(false);
        memo.setUpdatedAt(LocalDateTime.now());

        fuelMemoRepository.save(memo);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("FuelMemo restored successfully with ID: " + id)
                .build();
    }
    @Override
    public HttpResponse<?> getAllFuelMemos(String keyword,
                                           Integer pageNumber,
                                           Integer pageSize,
                                           String sortBy,
                                           String sortDirection,
                                           String isDeleted,
                                           Integer month,
                                           Integer year,
                                           String monthStatus) {

        // ================= DEFAULTS =================
        pageNumber = (pageNumber == null || pageNumber < 0) ? 0 : pageNumber;
        pageSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;
        sortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        sortDirection = (sortDirection == null || sortDirection.isEmpty()) ? "desc" : sortDirection;

        // ================= VALIDATE SORT =================
        List<String> allowedSortFields = List.of("date", "quantity", "srNo");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "date";
        }

        // ================= VALIDATE MONTH =================
        if (month != null ^ year != null) {
            throw new IllegalArgumentException("Both month and year must be provided together");
        }

        // ================= VALIDATE MONTH STATUS =================
        if (monthStatus != null &&
                !monthStatus.equalsIgnoreCase("OPEN") &&
                !monthStatus.equalsIgnoreCase("CLOSED")) {

            throw new IllegalArgumentException("Invalid monthStatus. Use OPEN or CLOSED");
        }

        // ================= DELETE FILTER =================
        Boolean deletedFilter = null;
        if ("true".equalsIgnoreCase(isDeleted)) deletedFilter = true;
        else if ("false".equalsIgnoreCase(isDeleted)) deletedFilter = false;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ================= MAIN QUERY =================
        CriteriaQuery<FuelMemo> cq = cb.createQuery(FuelMemo.class);
        Root<FuelMemo> root = cq.from(FuelMemo.class);
        Join<FuelMemo, FuelMonth> monthJoin = root.join("fuelMonth", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(
                cb, root, monthJoin, keyword, deletedFilter, month, year, monthStatus
        );

        cq.where(predicates.toArray(new Predicate[0]));

        Order order = sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(root.get(sortBy))
                : cb.desc(root.get(sortBy));

        cq.orderBy(order);

        TypedQuery<FuelMemo> query = entityManager.createQuery(cq);
        query.setFirstResult(pageNumber * pageSize);
        query.setMaxResults(pageSize);

        List<FuelMemo> memos = query.getResultList();

        // ================= COUNT QUERY =================
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FuelMemo> countRoot = countQuery.from(FuelMemo.class);
        Join<FuelMemo, FuelMonth> countMonthJoin = countRoot.join("fuelMonth", JoinType.LEFT);

        List<Predicate> countPredicates = buildPredicates(
                cb, countRoot, countMonthJoin, keyword, deletedFilter, month, year, monthStatus
        );

        countQuery.select(cb.count(countRoot))
                .where(countPredicates.toArray(new Predicate[0]));

        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // ================= DTO =================
        List<FuelMemoResponseDto> dtoList = dataUtil.mapToFuelMemoDtoList(memos);

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLastPage = pageNumber >= (totalPages - 1);

        // ================= RESPONSE =================
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Fuel Memos fetched successfully")
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLastPage(isLastPage)
                .data(dtoList)
                .build();
    }
    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<FuelMemo> root,
            Join<FuelMemo, FuelMonth> monthJoin,
            String keyword,
            Boolean deletedFilter,
            Integer month,
            Integer year,
            String monthStatus
    ) {

        List<Predicate> predicates = new ArrayList<>();

        // ================= DELETE FILTER =================
        if (deletedFilter != null) {
            predicates.add(cb.equal(root.get("deleted"), deletedFilter));
        }

        // ================= MONTH FILTER =================
        if (month != null && year != null) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            predicates.add(cb.between(root.get("date"), startDate, endDate));
        }

        // ================= MONTH STATUS =================
        if (monthStatus != null) {

            Expression<Boolean> closedExpr = cb.coalesce(monthJoin.get("closed"), false);
            Expression<LocalDateTime> reopenedExpr = monthJoin.get("reopenedAt");

            if ("OPEN".equalsIgnoreCase(monthStatus)) {
                predicates.add(cb.or(
                        cb.equal(closedExpr, false),
                        cb.isNotNull(reopenedExpr)
                ));
            } else if ("CLOSED".equalsIgnoreCase(monthStatus)) {
                predicates.add(cb.and(
                        cb.equal(closedExpr, true),
                        cb.isNull(reopenedExpr)
                ));
            }
        }

        // ================= SEARCH =================
        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = "%" + keyword.toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("user").get("userName")), k),
                    cb.like(cb.lower(root.get("vehicle").get("vehicleNumber")), k),
                    cb.like(cb.lower(root.get("driver").get("driverName")), k),
                    cb.like(cb.lower(root.get("site").get("siteName")), k),
                    cb.like(cb.lower(root.get("fuelStation").get("fuelStationName")), k),
                    cb.like(cb.lower(root.get("company").get("companyName")), k)
            ));
        }

        return predicates;
    }

    @Override
    public DataResponse getFuelMemoById(Integer id) {

        // ================= VALIDATION =================
        if (id == null) {
            throw new IllegalArgumentException("FuelMemo ID must not be null");
        }

        // ================= FETCH =================
        FuelMemo memo = fuelMemoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("FuelMemo not found with ID: " + id));

        // ================= SOFT DELETE CHECK =================
        if (Boolean.TRUE.equals(memo.getDeleted())) {
            throw new ResourceNotFoundException("FuelMemo is deleted with ID: " + id);
        }

        // ================= DTO =================
        FuelMemoResponseDto dto = dataUtil.mapToFuelMemoDto(memo);

        // ================= RESPONSE =================
        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dto)
                .build();
    }

//@Override
//public HttpResponse<?> getAllFuelMemosByCompany(Integer companyId,
//                                                String keyword,
//                                                Integer pageNumber,
//                                                Integer pageSize,
//                                                String sortBy,
//                                                String sortDirection,
//                                                String isDeleted,
//                                                Integer month,
//                                                Integer year,
//                                                String monthStatus) {
//
//    if (pageNumber == null || pageNumber < 0) pageNumber = 0;
//    if (pageSize == null || pageSize <= 0) pageSize = 10;
//    if (sortBy == null || sortBy.isEmpty()) sortBy = "date";
//    if (sortDirection == null || sortDirection.isEmpty()) sortDirection = "desc";
//
//    Boolean deletedFilter = null;
//
//    if ("true".equalsIgnoreCase(isDeleted)) deletedFilter = true;
//    if ("false".equalsIgnoreCase(isDeleted)) deletedFilter = false;
//
//    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//
//    CriteriaQuery<FuelMemo> cq = cb.createQuery(FuelMemo.class);
//    Root<FuelMemo> root = cq.from(FuelMemo.class);
//
//    Join<FuelMemo, FuelMonth> monthJoin = root.join("fuelMonth", JoinType.LEFT);
//
//    List<Predicate> predicates = new ArrayList<>();
//
//    // Company Filter
//    predicates.add(cb.equal(root.get("company").get("companyId"), companyId));
//
//    // Deleted Filter
//    if (deletedFilter != null) {
//        predicates.add(cb.equal(root.get("deleted"), deletedFilter));
//    }
//
//    // ===== MONTH FILTER (FROM FUEL MONTH TABLE) =====
//
//    if (month != null) {
//        predicates.add(cb.equal(monthJoin.get("month"), month));
//    }
//
//    if (year != null) {
//        predicates.add(cb.equal(monthJoin.get("year"), year));
//    }
//
//    // ===== OPEN / CLOSED FILTER =====
//
//    if (monthStatus != null && !monthStatus.isEmpty()) {
//
//        Expression<Boolean> closedExpr = cb.coalesce(monthJoin.get("closed"), false);
//        Expression<java.time.LocalDateTime> reopenedExpr = monthJoin.get("reopenedAt");
//
//        if ("OPEN".equalsIgnoreCase(monthStatus)) {
//            predicates.add(cb.or(
//                    cb.equal(closedExpr, false),
//                    cb.isNotNull(reopenedExpr)
//            ));
//        }
//
//        if ("CLOSED".equalsIgnoreCase(monthStatus)) {
//            predicates.add(cb.and(
//                    cb.equal(closedExpr, true),
//                    cb.isNull(reopenedExpr)
//            ));
//        }
//    }
//
//    // ===== SEARCH =====
//
//    if (keyword != null && !keyword.trim().isEmpty()) {
//
//        String k = "%" + keyword.toLowerCase() + "%";
//
//        predicates.add(cb.or(
//
//                cb.like(cb.lower(root.get("vehicle").get("vehicleNumber")), k),
//                cb.like(cb.lower(root.get("driver").get("driverName")), k),
//                cb.like(cb.lower(root.get("site").get("siteName")), k),
//                cb.like(cb.lower(root.get("fuelStation").get("fuelStationName")), k),
//                cb.like(cb.lower(root.get("user").get("userName")), k)
//        ));
//    }
//
//    cq.where(predicates.toArray(new Predicate[0]));
//
//    Order order = sortDirection.equalsIgnoreCase("asc")
//            ? cb.asc(root.get(sortBy))
//            : cb.desc(root.get(sortBy));
//
//    cq.orderBy(order);
//
//    TypedQuery<FuelMemo> query = entityManager.createQuery(cq);
//
//    query.setFirstResult(pageNumber * pageSize);
//    query.setMaxResults(pageSize);
//
//    List<FuelMemo> memos = query.getResultList();
//
//    // ===== COUNT QUERY =====
//
//    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
//    Root<FuelMemo> countRoot = countQuery.from(FuelMemo.class);
//
//    Join<FuelMemo, FuelMonth> countMonthJoin = countRoot.join("fuelMonth", JoinType.LEFT);
//
//    List<Predicate> countPredicates = new ArrayList<>();
//
//    countPredicates.add(cb.equal(countRoot.get("company").get("companyId"), companyId));
//
//    if (deletedFilter != null) {
//        countPredicates.add(cb.equal(countRoot.get("deleted"), deletedFilter));
//    }
//
//    if (month != null) {
//        countPredicates.add(cb.equal(countMonthJoin.get("month"), month));
//    }
//
//    if (year != null) {
//        countPredicates.add(cb.equal(countMonthJoin.get("year"), year));
//    }
//
//    if (monthStatus != null && !monthStatus.isEmpty()) {
//
//        Expression<Boolean> closedExpr = cb.coalesce(countMonthJoin.get("closed"), false);
//        Expression<java.time.LocalDateTime> reopenedExpr = countMonthJoin.get("reopenedAt");
//
//        if ("OPEN".equalsIgnoreCase(monthStatus)) {
//            countPredicates.add(cb.or(
//                    cb.equal(closedExpr, false),
//                    cb.isNotNull(reopenedExpr)
//            ));
//        }
//
//        if ("CLOSED".equalsIgnoreCase(monthStatus)) {
//            countPredicates.add(cb.and(
//                    cb.equal(closedExpr, true),
//                    cb.isNull(reopenedExpr)
//            ));
//        }
//    }
//
//    countQuery.select(cb.count(countRoot))
//            .where(countPredicates.toArray(new Predicate[0]));
//
//    Long totalElements = entityManager.createQuery(countQuery).getSingleResult();
//
//    List<FuelMemoResponseDto> dtoList = dataUtil.mapToFuelMemoDtoList(memos);
//
//// ✅ TOTAL CALCULATE
//    double totalQty = 0;
//    for (FuelMemo m : memos) {
//        if (m.getQuantity() != null) {
//            totalQty += m.getQuantity().doubleValue();
//        }
//    }
//
    //// ✅ ADD LAST ROW
//    dtoList.add(
//            FuelMemoResponseDto.builder()
//                    .vehicleNo("TOTAL")
//                    .quantity(BigDecimal.valueOf(totalQty))
//                    .build()
//    );
//
//    int totalPages = (int) Math.ceil((double) totalElements / pageSize);
//
//    return HttpResponse.builder()
//            .success(true)
//            .successCode(HttpStatus.OK)
//            .message("Fuel Memo List")
//            .pageNumber(pageNumber)
//            .pageSize(pageSize)
//            .totalElements(totalElements)
//            .totalPages(totalPages)
//            .isLastPage(pageNumber >= totalPages - 1)
//            .data(dtoList)
//            .build();
//}
    @Override
    public HttpResponse<?> getAllFuelMemosByCompany(Integer companyId,
                                                    String keyword,
                                                    Integer pageNumber,
                                                    Integer pageSize,
                                                    String sortBy,
                                                    String sortDirection,
                                                    String isDeleted,
                                                    Integer month,
                                                    Integer year,
                                                    String monthStatus) {

        // ================= VALIDATION =================
        if (companyId == null) {
            throw new IllegalArgumentException("Company ID must not be null");
        }

        if (month != null ^ year != null) {
            throw new IllegalArgumentException("Both month and year must be provided together");
        }

        if (monthStatus != null &&
                !monthStatus.equalsIgnoreCase("OPEN") &&
                !monthStatus.equalsIgnoreCase("CLOSED")) {
            throw new IllegalArgumentException("Invalid monthStatus. Use OPEN or CLOSED");
        }

        // ================= DEFAULTS =================
        pageNumber = (pageNumber == null || pageNumber < 0) ? 0 : pageNumber;
        pageSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;
        sortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        sortDirection = (sortDirection == null || sortDirection.isEmpty()) ? "desc" : sortDirection;

        List<String> allowedSortFields = List.of("date", "quantity", "srNo");
        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "date";
        }

        Boolean deletedFilter = null;
        if ("true".equalsIgnoreCase(isDeleted)) deletedFilter = true;
        else if ("false".equalsIgnoreCase(isDeleted)) deletedFilter = false;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ================= MAIN QUERY =================
        CriteriaQuery<FuelMemo> cq = cb.createQuery(FuelMemo.class);
        Root<FuelMemo> root = cq.from(FuelMemo.class);
        Join<FuelMemo, FuelMonth> monthJoin = root.join("fuelMonth", JoinType.LEFT);

        List<Predicate> predicates = buildCompanyPredicates(
                cb, root, monthJoin, companyId, keyword, deletedFilter, month, year, monthStatus
        );

        cq.where(predicates.toArray(new Predicate[0]));

        cq.orderBy(sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(root.get(sortBy))
                : cb.desc(root.get(sortBy)));

        List<FuelMemo> memos = entityManager.createQuery(cq)
                .setFirstResult(pageNumber * pageSize)
                .setMaxResults(pageSize)
                .getResultList();

        // ================= COUNT QUERY =================
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<FuelMemo> countRoot = countQuery.from(FuelMemo.class);
        Join<FuelMemo, FuelMonth> countJoin = countRoot.join("fuelMonth", JoinType.LEFT);

        List<Predicate> countPredicates = buildCompanyPredicates(
                cb, countRoot, countJoin, companyId, keyword, deletedFilter, month, year, monthStatus
        );

        countQuery.select(cb.count(countRoot))
                .where(countPredicates.toArray(new Predicate[0]));

        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // ================= DTO =================
        List<FuelMemoResponseDto> dtoList = new ArrayList<>();
        double totalQty = 0;

        for (FuelMemo m : memos) {
            double qty = m.getQuantity() != null ? m.getQuantity().doubleValue() : 0;
            totalQty += qty;

            dtoList.add(dataUtil.mapToFuelMemoDto(m));
        }

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("rows", dtoList);
        response.put("totalQty", totalQty);

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Fuel Memo List")
                .data(response)
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .isLastPage(pageNumber >= totalPages - 1)
                .build();
    }
    private List<Predicate> buildCompanyPredicates(
            CriteriaBuilder cb,
            Root<FuelMemo> root,
            Join<FuelMemo, FuelMonth> monthJoin,
            Integer companyId,
            String keyword,
            Boolean deletedFilter,
            Integer month,
            Integer year,
            String monthStatus) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("company").get("companyId"), companyId));

        if (deletedFilter != null) {
            predicates.add(cb.equal(root.get("deleted"), deletedFilter));
        }

        if (month != null && year != null) {
            predicates.add(cb.and(
                    cb.equal(monthJoin.get("month"), month),
                    cb.equal(monthJoin.get("year"), year)
            ));
        }

        if (monthStatus != null) {
            Expression<Boolean> closedExpr = cb.coalesce(monthJoin.get("closed"), false);
            Expression<LocalDateTime> reopenedExpr = monthJoin.get("reopenedAt");

            if ("OPEN".equalsIgnoreCase(monthStatus)) {
                predicates.add(cb.or(
                        cb.equal(closedExpr, false),
                        cb.isNotNull(reopenedExpr)
                ));
            } else {
                predicates.add(cb.and(
                        cb.equal(closedExpr, true),
                        cb.isNull(reopenedExpr)
                ));
            }
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String k = "%" + keyword.toLowerCase() + "%";

            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("vehicle").get("vehicleNumber")), k),
                    cb.like(cb.lower(root.get("driver").get("driverName")), k),
                    cb.like(cb.lower(root.get("site").get("siteName")), k),
                    cb.like(cb.lower(root.get("fuelStation").get("fuelStationName")), k),
                    cb.like(cb.lower(root.get("user").get("userName")), k)
            ));
        }

        return predicates;
    }
}
