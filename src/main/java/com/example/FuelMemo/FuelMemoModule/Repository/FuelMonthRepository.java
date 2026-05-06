package com.example.FuelMemo.FuelMemoModule.Repository;



import com.example.FuelMemo.FuelMemoModule.Entity.FuelMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FuelMonthRepository extends JpaRepository<FuelMonth, Integer> {

    // 🔍 Direct return (null if not found)
    FuelMonth findByMonthAndYear(Integer month, Integer year);

    // 🔍 Only active (not deleted)
    Optional<FuelMonth> findByMonthAndYearAndDeletedFalse(Integer month, Integer year);

    Optional<FuelMonth> findByMonthAndYearAndCompany_CompanyIdAndDeletedFalse(
                Integer month,
                Integer year,
                Integer companyId
    );

}
