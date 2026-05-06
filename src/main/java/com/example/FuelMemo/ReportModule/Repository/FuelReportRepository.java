package com.example.FuelMemo.ReportModule.Repository;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.ReportModule.DTO.DateWiseFuelMemoReportDto;
import com.example.FuelMemo.ReportModule.DTO.FuelMemoListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface FuelReportRepository extends JpaRepository<FuelMemo, Long> {

    List<FuelMemo> findByDeletedFalse();

    List<FuelMemo> findByDeletedFalseAndDateBetween(LocalDate fromDate, LocalDate toDate);

    List<FuelMemo> findByDeletedFalseAndActiveTrue();
    List<FuelMemo> findByDateBetweenAndDeletedFalse(
            LocalDate fromDate,
            LocalDate toDate
    );
    List<FuelMemo> findByDeletedFalseAndActiveTrueAndDateBetween(
            LocalDate fromDate,
            LocalDate toDate
    );

    List<FuelMemo> findByCompany_CompanyIdAndDeletedFalseAndActiveTrue(Integer companyId);
//    @Query("SELECT new com.example.FuelMemo.FuelMemoModule.Dto.DateWiseFuelMemoReportDto(" +
//            "fm.date, fm.srNo, v.registrationNumber, d.name, s.name, f.name, fm.quantity, c.name, fm.entryType, fm.fuelMonth.closed) " +
//            "FROM FuelMemo fm " +
//            "JOIN fm.vehicle v " +
//            "JOIN fm.driver d " +
//            "JOIN fm.site s " +
//            "JOIN fm.fuelStation f " +
//            "JOIN fm.company c " +
//            "WHERE fm.date BETWEEN :fromDate AND :toDate AND fm.deleted = false " +
//            "ORDER BY fm.date ASC, fm.srNo ASC")
//    List<DateWiseFuelMemoReportDto> getDateWiseReport(@Param("fromDate") LocalDate fromDate,
//                                                      @Param("toDate") LocalDate toDate);
//@Query("SELECT new com.example.FuelMemo.ReportModule.DTO.DateWiseFuelMemoReportDto(" +
//        "fm.date, fm.srNo, v.vehicleNumber, d.driverName, s.siteName, f.fuelStationName, fm.quantity, c.companyName, fm.entryType, fm.fuelMonth.closed) " +
//        "FROM FuelMemo fm " +
//        "JOIN fm.vehicle v " +
//        "JOIN fm.driver d " +
//        "JOIN fm.site s " +
//        "JOIN fm.fuelStation f " +
//        "JOIN fm.company c " +
//        "LEFT JOIN fm.fuelMonth fmMonth " +
//        "WHERE fm.date BETWEEN :fromDate AND :toDate AND fm.deleted = false " +
//        "ORDER BY fm.date ASC, fm.srNo ASC")
//List<DateWiseFuelMemoReportDto> getDateWiseReport(@Param("fromDate") LocalDate fromDate,
//                                                  @Param("toDate") LocalDate toDate);

    List<FuelMemo> findByCompany_CompanyIdAndSrNoAndDeletedFalseAndActiveTrue(Integer companyId, Integer srNo);


}
