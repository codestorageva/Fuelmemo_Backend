package com.example.FuelMemo.RoyaltyModule.Repository;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.ReportModule.DTO.ChartDto;
import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import com.example.FuelMemo.RoyaltyModule.Entity.RoyaltyPass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoyaltyPassRepository extends JpaRepository<RoyaltyPass, Integer> {

    boolean existsByRoyaltyNumberAndRoyaltyIdNot(String royaltyNumber, Integer royaltyId);

    boolean existsByRoyaltyNumber(String royaltyNumber);

    RoyaltyPass findByRoyaltyIdAndDeletedFalse(Integer royaltyId);

    List<RoyaltyPass> findByCompanyCompanyIdAndDeletedFalse(Integer companyId);

    List<RoyaltyPass> findByDeletedTrue();

    List<RoyaltyPass> findByCompanyCompanyIdAndDeletedFalseAndCompanyIsDeletedFalse(Integer companyId);

    List<RoyaltyPass> findByCompanyCompanyId(Integer companyId);

    // ✅ CORRECT METHODS
    boolean existsByRoyaltyNumberAndCompany_CompanyId(
            String royaltyNumber,
            Integer companyId
    );

    boolean existsByRoyaltyNumberAndCompany_CompanyIdAndRoyaltyIdNot(
            String royaltyNumber,
            Integer companyId,
            Integer royaltyId
    );
    List<RoyaltyPass> findByCompanyCompanyIdAndStatus(Integer companyId, PassStatus status);

    boolean existsByCompanyCompanyIdAndDeletedFalse(Integer companyId);

    List<RoyaltyPass> findByCompanyCompanyIdAndDeletedTrue(Integer companyId);

    List<RoyaltyPass> findByCompanyCompanyIdAndDeletedFalseAndStatus(
            Integer companyId,
            PassStatus status
    );

    boolean existsByRoyaltyNumberAndDeletedFalse(String royaltyNumber);

    boolean existsByRoyaltyNumberAndRoyaltyIdNotAndDeletedFalse(String royaltyNumber, Integer royaltyId);

    RoyaltyPass findByRoyaltyIdAndDeletedFalseAndStatus(Integer royaltyId, PassStatus passStatus);

    List<RoyaltyPass> findByCompanyCompanyIdAndDeletedTrueAndStatus(Integer companyId, PassStatus status);


    @Query("""
    SELECT r FROM RoyaltyPass r
    WHERE r.company.companyId = :companyId
    AND (
        (r.deleted = false AND r.status = :status)
        OR r.royaltyId = :royaltyId
    )
""")
    List<RoyaltyPass> findAvailableOrSelected(
            @Param("companyId") Integer companyId,
            @Param("status") PassStatus status,
            @Param("royaltyId") Integer royaltyId
    );

    @Query("""
    SELECT new com.example.FuelMemo.ReportModule.DTO.ChartDto(
        r.status, COUNT(r)
    )
    FROM RoyaltyPass r
    WHERE r.company = :company
      AND r.status IS NOT NULL
    GROUP BY r.status
""")
    List<ChartDto> getStatusChart(@Param("company") Company company);

}
