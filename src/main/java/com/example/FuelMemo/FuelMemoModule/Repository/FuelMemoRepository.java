package com.example.FuelMemo.FuelMemoModule.Repository;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.ReportModule.DTO.FuelMemoListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FuelMemoRepository extends JpaRepository<FuelMemo, Integer> {



    boolean existsByRoyaltyPass_RoyaltyIdAndDeletedFalseAndFuelMemoIdNot(
            Integer royaltyId, Integer fuelMemoId
    );

    FuelMemo findTopByCompany_CompanyIdAndDateBetweenAndDeletedFalseOrderBySrNoDesc(
            Integer companyId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByRoyaltyPass_RoyaltyIdAndDeletedFalse(Integer royaltyId);
    boolean existsByRoyaltyPass_RoyaltyIdAndFuelMemoIdNotAndDeletedFalse(
            Integer royaltyId,
            Integer fuelMemoId
    );


    boolean existsByCompanyCompanyId(Integer companyId);

    List<FuelMemo> findByCompanyCompanyIdAndDeletedTrue(Integer companyId);

    long countByCompany(Company company);

    long countByCompanyAndCreatedAtBetween(
            Company company,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
SELECT new com.example.FuelMemo.ReportModule.DTO.FuelMemoListDto(
    CAST(f.fuelMemoId AS string),
    v.vehicleNumber,
    d.driverName,
    CONCAT(f.quantity, ' L'),
    f.active,
    TO_CHAR(f.date, 'DD Mon YYYY')
)
FROM FuelMemo f
JOIN f.vehicle v
JOIN f.driver d
WHERE f.company = :company
ORDER BY f.date DESC
""")
    List<FuelMemoListDto> getRecentFuelMemos(@Param("company") Company company);


    @Query("""
    SELECT COALESCE(SUM(f.quantity), 0)
    FROM FuelMemo f
    WHERE f.company = :company
      AND f.date >= :start
      AND f.date < :end
""")
    Double getTotalByDateRange(
            @Param("company") Company company,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    Long countByDeletedFalseAndDateBetween(LocalDate fromDate, LocalDate toDate);

    Long countByDeletedFalse();

    Long countByCompanyCompanyIdAndDeletedFalseAndDateBetween(Integer companyId, LocalDate fromDate, LocalDate toDate);

    Long countByCompanyCompanyIdAndDeletedFalse(Integer companyId);
}