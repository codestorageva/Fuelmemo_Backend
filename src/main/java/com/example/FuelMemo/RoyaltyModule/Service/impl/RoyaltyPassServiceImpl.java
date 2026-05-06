package com.example.FuelMemo.RoyaltyModule.Service.impl;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.RoyaltyModule.Dto.Request.RoyaltyPassUpdateDto;
import com.example.FuelMemo.RoyaltyModule.Dto.Response.RoyaltyPassResponse;
import com.example.FuelMemo.RoyaltyModule.Dto.RoyaltyPassDto;
import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import com.example.FuelMemo.RoyaltyModule.Entity.RoyaltyPass;
import com.example.FuelMemo.RoyaltyModule.Repository.RoyaltyPassRepository;
import com.example.FuelMemo.RoyaltyModule.Service.RoyaltyPassService;
import com.example.FuelMemo.Shared.Exception.*;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageListResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service

public class RoyaltyPassServiceImpl implements RoyaltyPassService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RoyaltyPassRepository royaltyPassRepository;

    public RoyaltyPassServiceImpl(RoyaltyPassRepository royaltyPassRepository, CompanyRepository companyRepository, DataUtil dataUtil) {
        this.royaltyPassRepository = royaltyPassRepository;
        this.companyRepository = companyRepository;
        this.dataUtil = dataUtil;
    }

    private final CompanyRepository companyRepository;
    private final DataUtil dataUtil;

    @Transactional
    @Override
    public MessageResponse createRoyaltyPass(RoyaltyPassDto dto) {

        if (dto == null || dto.getRoyaltyNumber() == null || dto.getCompanyId() == null) {
            throw new IllegalArgumentException("Invalid input for creating Royalty Pass");
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with id: " + dto.getCompanyId()));

        if (Boolean.TRUE.equals(company.getIsDeleted())) {
            throw new InactiveStatusException("Cannot assign deleted company.");
        }

        String royaltyNumber = dto.getRoyaltyNumber().trim().toUpperCase();

        if (royaltyNumber.isEmpty()) {
            throw new IllegalArgumentException("Royalty number cannot be empty");
        }

        if (royaltyPassRepository.existsByRoyaltyNumberAndDeletedFalse(royaltyNumber)) {
            throw new DuplicateEntryException("Royalty number already exists: " + royaltyNumber);
        }

//        RoyaltyPass royaltyPass = new RoyaltyPass();
//        royaltyPass.setRoyaltyNumber(royaltyNumber);
//        royaltyPass.setStatus(PassStatus.AVAILABLE);
//        royaltyPass.setCompany(company);
//        royaltyPass.setDeleted(false);
//
//        royaltyPassRepository.save(royaltyPass);
        RoyaltyPass royaltyPass = new RoyaltyPass();
        royaltyPass.setRoyaltyNumber(royaltyNumber);
        royaltyPass.setStatus(PassStatus.AVAILABLE);
        royaltyPass.setCompany(company);
        royaltyPass.setDeleted(false);
        royaltyPass.setCreatedAt(LocalDateTime.now());

        royaltyPassRepository.save(royaltyPass);

        return new MessageResponse(true, HttpStatus.CREATED, "Royalty Pass created successfully");
    }
    @Transactional
    @Override
    public MessageResponse updateRoyaltyPass(Integer royaltyId, RoyaltyPassUpdateDto dto) {

        // ---------------- INPUT VALIDATION ----------------
        if (dto == null || dto.getRoyaltyNumber() == null || dto.getRoyaltyNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Royalty number is required");
        }

        String royaltyNumber = dto.getRoyaltyNumber().trim().toUpperCase();

        // ---------------- FETCH ROYALTY PASS ----------------
        RoyaltyPass royaltyPass = royaltyPassRepository.findById(royaltyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Royalty Pass not found with ID: " + royaltyId));

        // ---------------- CHECK DELETED ----------------
        if (Boolean.TRUE.equals(royaltyPass.getDeleted())) {
            throw new InactiveStatusException("Cannot update a deleted Royalty Pass. Restore it first.");
        }

        // ---------------- CHECK USED ----------------
        if (royaltyPass.getStatus() == PassStatus.USED) {
            throw new InactiveStatusException("Cannot update a used Royalty Pass");
        }

        // ---------------- DUPLICATE CHECK (ONLY IF CHANGED) ----------------
        if (!royaltyPass.getRoyaltyNumber().equals(royaltyNumber)) {

            if (royaltyPassRepository
                    .existsByRoyaltyNumberAndRoyaltyIdNotAndDeletedFalse(royaltyNumber, royaltyId)) {

                throw new DuplicateEntryException("Royalty number already exists: " + royaltyNumber);
            }

            royaltyPass.setRoyaltyNumber(royaltyNumber);
        }

        // ---------------- UPDATE COMPANY (OPTIONAL) ----------------
        if (dto.getCompanyId() != null) {

            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Company not found with id: " + dto.getCompanyId()));

            if (Boolean.TRUE.equals(company.getIsDeleted())) {
                throw new InactiveStatusException("Cannot assign deleted company.");
            }

            royaltyPass.setCompany(company);
        }

        // ---------------- UPDATE TIMESTAMP ----------------
        royaltyPass.setUpdatedAt(LocalDateTime.now());

        // ---------------- SAVE ----------------
        royaltyPassRepository.save(royaltyPass);

        // ---------------- RESPONSE ----------------
        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Royalty Pass updated successfully.")
                .build();
    }


    @Transactional
    @Override
    public DataResponse getRoyaltyPassById(Integer royaltyId) {

        RoyaltyPass royaltyPass = royaltyPassRepository
                .findByRoyaltyIdAndDeletedFalseAndStatus(royaltyId, PassStatus.AVAILABLE);

        if (royaltyPass == null) {
            throw new ResourceNotFoundException(
                    "Active Royalty Pass not found with id: " + royaltyId
            );
        }

        return new DataResponse(true, HttpStatus.OK,
                dataUtil.toRoyaltyPassResponse(royaltyPass));
    }

    @Transactional
    @Override
    public DataResponse getRoyaltyPassesByCompanyId(Integer companyId, Boolean isDeleted, String status) {

        List<RoyaltyPass> royaltyPasses;

        // ✅ Case-insensitive conversion (STRICT)
        PassStatus passStatus = null;

        if (status != null && !status.isBlank()) {
            passStatus = Arrays.stream(PassStatus.values())
                    .filter(e -> e.name().equalsIgnoreCase(status))
                    .findFirst()
                    .orElseThrow(() ->
                            new BadRequestException("Invalid status. Allowed: AVAILABLE, USED")
                    );
        }

        // 🔹 CASE 1: isDeleted = null
        if (isDeleted == null) {

            royaltyPasses = (passStatus == null)
                    ? royaltyPassRepository.findByCompanyCompanyId(companyId)
                    : royaltyPassRepository.findByCompanyCompanyIdAndStatus(companyId, passStatus);

        }

        // 🔹 CASE 2: isDeleted = true
        else if (isDeleted) {

            royaltyPasses = (passStatus == null)
                    ? royaltyPassRepository.findByCompanyCompanyIdAndDeletedTrue(companyId)
                    : royaltyPassRepository.findByCompanyCompanyIdAndDeletedTrueAndStatus(companyId, passStatus);

        }

        // 🔹 CASE 3: isDeleted = false
        else {

            royaltyPasses = (passStatus == null)
                    ? royaltyPassRepository.findByCompanyCompanyIdAndDeletedFalse(companyId)
                    : royaltyPassRepository.findByCompanyCompanyIdAndDeletedFalseAndStatus(companyId, passStatus);
        }

        // ✅ EMPTY LIST RETURN (NO EXCEPTION)
        List<RoyaltyPassResponse> responseList = royaltyPasses.stream()
                .map(dataUtil::toRoyaltyPassResponse)
                .toList();

        return new DataResponse(true, HttpStatus.OK, responseList);
    }
//    @Transactional()
//    @Override
//    public DataResponse getRoyaltyPassesByCompanyId(Integer companyId, Boolean isDeleted) {
//
//        List<RoyaltyPass> royaltyPasses;
//
//        if (isDeleted == null) {
//            royaltyPasses = royaltyPassRepository.findByCompanyCompanyId(companyId);
//        } else if (isDeleted) {
//            royaltyPasses = royaltyPassRepository.findByCompanyCompanyIdAndDeletedTrue(companyId);
//        } else {
//            royaltyPasses = royaltyPassRepository.findByCompanyCompanyIdAndDeletedFalse(companyId);
//        }
//
//        if (royaltyPasses.isEmpty()) {
//            throw new ResourceNotFoundException(
//                    "No royalty passes found for company id: " + companyId
//            );
//        }
//        if (isDeleted == null) {
//            royaltyPasses = royaltyPassRepository
//                    .findByCompanyCompanyIdAndStatus(companyId, PassStatus.AVAILABLE);
//
//        } else if (isDeleted) {
//            royaltyPasses = royaltyPassRepository
//                    .findByCompanyCompanyIdAndDeletedTrue(companyId);
//
//        } else {
//            royaltyPassRepository
//                    .findByCompanyCompanyIdAndDeletedFalseAndStatus(companyId, PassStatus.AVAILABLE);
//        }
//
//        List<RoyaltyPassResponse> responseList = royaltyPasses.stream()
//                .map(dataUtil::toRoyaltyPassResponse)
//                .toList();
//
//        return new DataResponse(true, HttpStatus.OK, responseList);
//    }

    @Transactional
    @Override
    public MessageListResponse getAllDeletedRoyaltyPasses() {

        List<RoyaltyPass> deletedPasses = royaltyPassRepository.findByDeletedTrue();

        if (deletedPasses.isEmpty()) {
            throw new ResourceNotFoundException("No deleted royalty passes found");
        }

        List<RoyaltyPassResponse> responseList = deletedPasses.stream()
                .map(dataUtil::toRoyaltyPassResponse)
                .toList();

        return MessageListResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Deleted royalty passes fetched successfully")
                .data(responseList)
                .build();
    }

    @Transactional
    @Override
    public HttpResponse getAllRoyaltyPasses(String keyword, Integer pageNumber,
                                            Integer pageSize, String sortBy,
                                            String sortDir, Boolean isDeleted) {

        // ---------------- DEFAULT VALUES ----------------
        pageNumber = (pageNumber == null || pageNumber < 0) ? 0 : pageNumber;
        pageSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;
        sortDir = (sortDir == null) ? "desc" : sortDir;

        List<String> allowedSortFields =
                List.of("royaltyNumber", "createdAt", "updatedAt", "company.companyName");

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // ---------------- MAIN QUERY ----------------
        CriteriaQuery<RoyaltyPass> cq = cb.createQuery(RoyaltyPass.class);
        Root<RoyaltyPass> root = cq.from(RoyaltyPass.class);

        List<Predicate> predicates = buildPredicates(cb, root, keyword, isDeleted);

        cq.where(predicates.toArray(new Predicate[0]));

        // Sorting
        Path<?> sortPath = "company.companyName".equals(sortBy)
                ? root.join("company").get("companyName")
                : root.get(sortBy);

        Order order = "asc".equalsIgnoreCase(sortDir)
                ? cb.asc(sortPath)
                : cb.desc(sortPath);

        cq.orderBy(order);

        List<RoyaltyPass> passes = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ---------------- COUNT QUERY ----------------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<RoyaltyPass> countRoot = countQuery.from(RoyaltyPass.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, keyword, isDeleted);

        countQuery.select(cb.count(countRoot))
                .where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        // ---------------- PAGE ----------------
        Page<RoyaltyPass> passPage = new PageImpl<>(passes, pageable, totalCount);

        // ---------------- RESPONSE ----------------
        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Royalty passes fetched successfully")
                .pageNumber(passPage.getNumber())
                .pageSize(passPage.getSize())
                .totalElements(passPage.getTotalElements())
                .totalPages(passPage.getTotalPages())
                .isLastPage(passPage.isLast())
                .data(passes.stream().map(dataUtil::toRoyaltyPassResponse).toList())
                .build();
    }
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<RoyaltyPass> root,
                                            String keyword, Boolean isDeleted) {

        List<Predicate> predicates = new ArrayList<>();

        // -------- SEARCH --------
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("royaltyNumber")), like),
                    cb.like(cb.lower(root.join("company").get("companyName")), like)
            ));
        }

        // -------- DELETED + STATUS LOGIC --------
        if (Boolean.TRUE.equals(isDeleted)) {
            // Only deleted
            predicates.add(cb.equal(root.get("deleted"), true));
        } else {
            // Active + AVAILABLE
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("status"), PassStatus.AVAILABLE));
        }

        return predicates;
    }

    @Transactional
    @Override
    public MessageResponse softDeleteRoyaltyPass(Integer royaltyId) {

        // ---------------- FETCH ----------------
        RoyaltyPass royaltyPass = royaltyPassRepository.findById(royaltyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Royalty Pass not found with ID: " + royaltyId));

        // ---------------- ALREADY DELETED ----------------
        if (Boolean.TRUE.equals(royaltyPass.getDeleted())) {
            throw new IllegalStateException("Royalty Pass is already deleted");
        }

        // ---------------- BUSINESS RULE ----------------
        if (royaltyPass.getStatus() == PassStatus.USED) {
            throw new IllegalStateException("Used royalty pass cannot be deleted");
        }

        // ---------------- SOFT DELETE ----------------
        royaltyPass.setDeleted(true);
        royaltyPass.setUpdatedAt(LocalDateTime.now());
        royaltyPassRepository.save(royaltyPass);
        // royaltyPass.setDeletedAt(LocalDateTime.now()); // optional

        return new MessageResponse(true, HttpStatus.OK,
                "Royalty Pass soft deleted successfully");
    }
    @Transactional
    @Override
    public MessageResponse restoreRoyaltyPass(Integer royaltyId) {

        // ---------------- FETCH ----------------
        RoyaltyPass royaltyPass = royaltyPassRepository.findById(royaltyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Royalty Pass not found with ID: " + royaltyId));

        // ---------------- ALREADY ACTIVE ----------------
        if (!Boolean.TRUE.equals(royaltyPass.getDeleted())) {
            throw new IllegalStateException("Royalty Pass is already active");
        }

        // ---------------- BUSINESS RULE (OPTIONAL) ----------------
        // if (royaltyPass.getStatus() == PassStatus.USED) {
        //     throw new IllegalStateException("Used royalty pass cannot be restored");
        // }

        // ---------------- RESTORE ----------------
        royaltyPass.setDeleted(false);
        royaltyPass.setUpdatedAt(LocalDateTime.now());
        // royaltyPass.setDeletedAt(null); // optional
        royaltyPassRepository.save(royaltyPass);

        return new MessageResponse(true, HttpStatus.OK,
                "Royalty Pass restored successfully");
    }

    @Transactional
    @Override
    public MessageResponse deleteRoyaltyPass(Integer royaltyId) {

        // ---------------- FETCH ----------------
        RoyaltyPass royaltyPass = royaltyPassRepository.findById(royaltyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Royalty Pass not found with ID: " + royaltyId));

        // ---------------- VALIDATION ----------------
        if (!Boolean.TRUE.equals(royaltyPass.getDeleted())) {
            throw new IllegalStateException(
                    "Please soft delete the Royalty Pass before permanent deletion");
        }

        // ---------------- BUSINESS RULE ----------------
        if (royaltyPass.getStatus() == PassStatus.USED) {
            throw new IllegalStateException(
                    "Used royalty pass cannot be permanently deleted");
        }

        // ---------------- HARD DELETE ----------------
        royaltyPassRepository.delete(royaltyPass);

        return new MessageResponse(true, HttpStatus.OK,
                "Royalty Pass permanently deleted successfully");
    }
    @Override
    public DataResponse getRoyaltyDropdown(Integer companyId, Integer selectedRoyaltyId) {

        List<RoyaltyPass> royaltyPasses;

        if (selectedRoyaltyId != null) {
            // 🔥 UPDATE MODE
            royaltyPasses = royaltyPassRepository.findAvailableOrSelected(
                    companyId,
                    PassStatus.AVAILABLE,
                    selectedRoyaltyId
            );
        } else {
            // 🔹 ADD MODE
            royaltyPasses = royaltyPassRepository
                    .findByCompanyCompanyIdAndDeletedFalseAndStatus(
                            companyId,
                            PassStatus.AVAILABLE
                    );
        }

        List<RoyaltyPassResponse> response = royaltyPasses.stream()
                .map(dataUtil::toRoyaltyPassResponse)
                .toList();

        return new DataResponse(true, HttpStatus.OK, response);
    }
}
