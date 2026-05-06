package com.example.FuelMemo.CompanyModule.Repository;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {

    boolean existsByCompanyNameIgnoreCase(String companyName);

    boolean existsByGstNumber(String gstNumber);

    boolean existsByEmail(String email);

    boolean existsByCompanyName(String companyName);

    Company findByCompanyIdAndIsDeletedFalse(Integer companyId);

    List<Company> findByCompanyIdInAndIsDeletedFalse(List<Integer> ids);

    List<Company> findByIsDeletedTrue();   // for soft-deleted companies

    boolean existsByPanNumber(String panNumber);

    boolean existsByCinNumber(String cinNumber);

    Company findByCompanyName(String companyName);

    List<Company> findByIsDeletedFalse();

    // ================= QUERY METHODS WITH KEYWORD & DELETED =================
    @Query("SELECT c FROM Company c " +
            "WHERE (:keyword IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.ownerName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.mobileNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isDeleted IS NULL OR c.isDeleted = :isDeleted)")
    Page<Company> findAllByKeywordAndDeleted(@Param("keyword") String keyword,
                                             @Param("isDeleted") Boolean isDeleted,
                                             Pageable pageable);

    @Query("SELECT c FROM Company c " +
            "WHERE c.companyId IN :ids " +
            "AND (:keyword IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.ownerName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.mobileNo) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isDeleted IS NULL OR c.isDeleted = :isDeleted)")
    Page<Company> findAllByIdInAndKeywordAndDeleted(@Param("ids") List<Integer> ids,
                                                    @Param("keyword") String keyword,
                                                    @Param("isDeleted") Boolean isDeleted,
                                                    Pageable pageable);

    Long countByIsDeletedFalse();

    Long countByIsDeletedFalseAndActiveTrue();

    Long countByIsDeletedFalseAndActiveFalse();

    boolean existsByCompanyIdAndIsDeletedFalse(Integer companyId);

    boolean existsByCompanyIdAndIsDeletedFalseAndActiveTrue(Integer companyId);

    boolean existsByCompanyIdAndIsDeletedFalseAndActiveFalse(Integer companyId);
}