package com.example.FuelMemo.SiteModule.Service.Impl;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;
import com.example.FuelMemo.Shared.Exception.DuplicateEntryException;
import com.example.FuelMemo.Shared.Exception.ResourceNotFoundException;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.Shared.Util.DataUtil;
import com.example.FuelMemo.SiteModule.Dto.SiteRequestDTO;
import com.example.FuelMemo.SiteModule.Dto.SiteResponseDTO;
import com.example.FuelMemo.SiteModule.Dto.SiteUpdateDTO;
import com.example.FuelMemo.SiteModule.Entity.Site;
import com.example.FuelMemo.SiteModule.Repository.SiteRepository;
import com.example.FuelMemo.SiteModule.Service.SiteService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final DataUtil dataUtil;
    private final EntityManager entityManager;

    @Override
    public MessageResponse createSite(SiteRequestDTO dto) {

        try {

            // 1️⃣ Validate Company
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found"));

            // 2️⃣ Validate State
            State state = stateRepository.findById(dto.getStateId())
                    .orElseThrow(() -> new RuntimeException("State not found"));

            // 3️⃣ Validate District
            District district = districtRepository.findById(dto.getDistrictId())
                    .orElseThrow(() -> new RuntimeException("District not found"));

            // 4️⃣ Duplicate Check
            boolean exists = siteRepository
                    .existsBySiteNameAndCompanyCompanyIdAndDeletedFalse(
                            dto.getSiteName(), dto.getCompanyId());

            if (exists) {
                return MessageResponse.builder()
                        .success(false)
                        .successCode(HttpStatus.BAD_REQUEST)
                        .message("Site already exists for this company")
                        .build();
            }

            // 5️⃣ Create Site
            Site site = new Site();
            site.setSiteName(dto.getSiteName());
            site.setSiteCode(dto.getSiteCode());
            site.setSiteOwnerName(dto.getSiteOwnerName());
            site.setAddress(dto.getAddress());
            site.setPincode(dto.getPincode());
            site.setCompany(company);
            site.setState(state);
            site.setDistrict(district);

            siteRepository.save(site);

            return MessageResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.CREATED)
                    .message("Site created successfully")
                    .build();

        } catch (RuntimeException ex) {

            return MessageResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.BAD_REQUEST)
                    .message(ex.getMessage())
                    .build();

        } catch (Exception ex) {

            return MessageResponse.builder()
                    .success(false)
                    .successCode(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Something went wrong while creating site")
                    .build();
        }
    }

    @Override
    public MessageResponse updateSiteById(SiteUpdateDTO dto, Integer siteId) {

        // 1️⃣ Fetch Site
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Site with Id '" + siteId + "' not found!"));

        if (Boolean.TRUE.equals(site.getDeleted())) {
            throw new IllegalStateException("Cannot update a deleted site.");
        }

        boolean isUpdated = false;

        // 2️⃣ Site Name Update + Duplicate Check
        if (dto.getSiteName() != null && !dto.getSiteName().trim().isEmpty()) {

            String newName = dto.getSiteName().trim();

            if (!newName.equalsIgnoreCase(site.getSiteName())) {

                boolean exists = siteRepository
                        .existsBySiteNameIgnoreCaseAndCompanyCompanyIdAndDeletedFalse(
                                newName,
                                site.getCompany().getCompanyId());

                if (exists) {
                    throw new DuplicateEntryException(
                            "Site name '" + newName + "' already exists for this company!");
                }

                site.setSiteName(newName);
                isUpdated = true;
            }
        }

        // 3️⃣ Site Code
        if (dto.getSiteCode() != null && !dto.getSiteCode().trim().isEmpty()) {
            site.setSiteCode(dto.getSiteCode().trim());
            isUpdated = true;
        }

        // 4️⃣ Owner Name
        if (dto.getSiteOwnerName() != null && !dto.getSiteOwnerName().trim().isEmpty()) {
            site.setSiteOwnerName(dto.getSiteOwnerName().trim());
            isUpdated = true;
        }

        // 5️⃣ Address
        if (dto.getAddress() != null && !dto.getAddress().trim().isEmpty()) {
            site.setAddress(dto.getAddress().trim());
            isUpdated = true;
        }

        // 6️⃣ State Update
        if (dto.getStateId() != null &&
                (site.getState() == null ||
                        !dto.getStateId().equals(site.getState().getStateId()))) {

            State state = stateRepository.findById(dto.getStateId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("State not found with id " + dto.getStateId()));

            site.setState(state);
            isUpdated = true;
        }

        // 7️⃣ District Update
        if (dto.getDistrictId() != null &&
                (site.getDistrict() == null ||
                        !dto.getDistrictId().equals(site.getDistrict().getDistrictId()))) {

            District district = districtRepository.findById(dto.getDistrictId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("District not found with id " + dto.getDistrictId()));

            site.setDistrict(district);
            isUpdated = true;
        }

        // 8️⃣ Pincode
        if (dto.getPincode() != null &&
                dto.getPincode().matches("^[0-9]{6}$") &&
                !dto.getPincode().equals(site.getPincode())) {

            site.setPincode(dto.getPincode());
            isUpdated = true;
        }

        // 9️⃣ If Nothing Changed
        if (!isUpdated) {
            throw new IllegalArgumentException("No valid fields provided for update.");
        }

        site.setUpdatedAt(LocalDateTime.now());

        siteRepository.save(site);

        return new MessageResponse(true, HttpStatus.OK, "Site details updated successfully.");
    }
    @Override
    public MessageResponse softDeleteSiteById(Integer siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Site with Id '" + siteId + "' not found!"));

        if (Boolean.TRUE.equals(site.getDeleted())) {
            throw new IllegalStateException("Site is already deleted.");
        }

        site.setDeleted(true);
        site.setActive(false);
        site.setUpdatedAt(LocalDateTime.now());

        siteRepository.save(site);

        return new MessageResponse(true, HttpStatus.OK, "Site deleted successfully.");
    }
    @Override
    public MessageResponse restoreSiteById(Integer siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Site with Id '" + siteId + "' not found!"));

        if (!Boolean.TRUE.equals(site.getDeleted())) {
            throw new IllegalStateException("Site is not deleted.");
        }

        if (Boolean.TRUE.equals(site.getCompany().getIsDeleted())) {
            throw new IllegalStateException("Cannot restore site because company is deleted.");
        }

        site.setDeleted(false);
        site.setActive(true);
        site.setUpdatedAt(LocalDateTime.now());

        siteRepository.save(site);

        return new MessageResponse(true, HttpStatus.OK, "Site restored successfully.");
    }
    @Override
    @Transactional
    public DataResponse getSiteById(Integer siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Site not found with id: " + siteId)
                );

        if (Boolean.TRUE.equals(site.getDeleted())) {
            throw new ResourceNotFoundException("Site not found with id: " + siteId);
        }


        SiteResponseDTO responseDto = DataUtil.mapToSiteResponse(site);

        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(responseDto)
                .build();
    }

    @Override
    @Transactional
    public HttpResponse getAllSites(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Site> cq = cb.createQuery(Site.class);
        Root<Site> root = cq.from(Site.class);


        root.fetch("company", JoinType.LEFT);
        root.fetch("state", JoinType.LEFT);
        root.fetch("district", JoinType.LEFT);

        List<Predicate> predicates =
                buildDynamicSitePredicates(cb, root, keyword, isDeleted);

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);


        Path<?> sortPath;
        try {
            sortPath = root.get(sortBy);
        } catch (Exception e) {
            sortPath = root.get("siteId");
        }

        Order order = sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(sortPath)
                : cb.desc(sortPath);

        cq.orderBy(order);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Site> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();


        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Site> countRoot = countQuery.from(Site.class);
        countQuery.select(cb.countDistinct(countRoot));

        List<Predicate> countPredicates =
                buildDynamicSitePredicates(cb, countRoot, keyword, isDeleted);

        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        Page<Site> page = new PageImpl<>(resultList, pageable, totalCount);

        List<SiteResponseDTO> sites = page.getContent()
                .stream()
                .map(DataUtil::mapToSiteResponse)
                .toList();

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Sites fetched successfully")
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .data(sites)
                .build();
    }

    private List<Predicate> buildDynamicSitePredicates(
            CriteriaBuilder cb,
            Root<Site> root,
            String keyword,
            Boolean isDeleted) {

        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            Predicate name =
                    cb.like(cb.lower(root.get("siteName")), pattern);

            Predicate code =
                    cb.like(cb.lower(root.get("siteCode")), pattern);

            predicates.add(cb.or(name, code));
        }

        if (isDeleted != null) {
            predicates.add(cb.equal(root.get("deleted"), isDeleted));
        }

        return predicates;
    }

    @Override
    @Transactional
    public HttpResponse getAllSitesByCompany(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Site> cq = cb.createQuery(Site.class);
        Root<Site> root = cq.from(Site.class);

        root.fetch("company", JoinType.LEFT);
        root.fetch("state", JoinType.LEFT);
        root.fetch("district", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();


        predicates.add(
                cb.equal(root.get("company").get("companyId"), companyId)
        );


        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            Predicate name =
                    cb.like(cb.lower(root.get("siteName")), pattern);

            Predicate code =
                    cb.like(cb.lower(root.get("siteCode")), pattern);

            predicates.add(cb.or(name, code));
        }

        if (isDeleted != null) {
            predicates.add(cb.equal(root.get("deleted"), isDeleted));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.distinct(true);


        Path<?> sortPath;
        try {
            sortPath = root.get(sortBy);
        } catch (Exception e) {
            sortPath = root.get("siteId");
        }

        Order order = sortDirection.equalsIgnoreCase("asc")
                ? cb.asc(sortPath)
                : cb.desc(sortPath);

        cq.orderBy(order);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Site> resultList = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Site> countRoot = countQuery.from(Site.class);
        countQuery.select(cb.countDistinct(countRoot));

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(
                cb.equal(countRoot.get("company").get("companyId"), companyId)
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            String pattern = "%" + keyword.toLowerCase() + "%";

            Predicate name =
                    cb.like(cb.lower(countRoot.get("siteName")), pattern);

            Predicate code =
                    cb.like(cb.lower(countRoot.get("siteCode")), pattern);

            countPredicates.add(cb.or(name, code));
        }

        if (isDeleted != null) {
            countPredicates.add(cb.equal(countRoot.get("deleted"), isDeleted));
        }

        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        Page<Site> page = new PageImpl<>(resultList, pageable, totalCount);

        List<SiteResponseDTO> sites = page.getContent()
                .stream()
                .map(DataUtil::mapToSiteResponse)
                .toList();

        String message = sites.isEmpty()
                ? "No sites found for this company"
                : "Company sites fetched successfully";

        return HttpResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message(message)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLastPage(page.isLast())
                .data(sites)
                .build();
    }
}