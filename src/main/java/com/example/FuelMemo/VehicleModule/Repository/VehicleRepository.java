package com.example.FuelMemo.VehicleModule.Repository;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer>,
        JpaSpecificationExecutor<Vehicle> {

    boolean existsByVehicleNumberAndCompanyCompanyIdAndDeletedFalse(
            String vehicleNumber,
            Integer companyId
    );

    boolean existsByVehicleNumberAndCompanyCompanyIdAndDeletedFalseAndVehicleIdNot(
            String vehicleNumber,
            Integer companyId,
            Integer vehicleId
    );

    boolean existsByCompanyCompanyId(Integer companyId);

    List<Vehicle> findByCompanyCompanyIdAndDeletedTrue(Integer companyId);

    long countByCreatedAtBetween(LocalDateTime todayStart, LocalDateTime tomorrowStart);

    long countByCompany(Company company);

    long countByCompanyAndCreatedAtBetween(Company company, LocalDateTime todayStart, LocalDateTime tomorrowStart);


    @Query("""
        SELECT COUNT(v)
        FROM Vehicle v
        WHERE (v.deleted = false OR v.deleted IS NULL)
          AND (v.active = true OR v.active IS NULL)
    """)
    Long countActiveVehiclesForDashboard();

    @Query("""
        SELECT COUNT(v)
        FROM Vehicle v
        WHERE v.company.companyId = :companyId
          AND (v.deleted = false OR v.deleted IS NULL)
          AND (v.active = true OR v.active IS NULL)
    """)
    Long countActiveVehiclesByCompanyId(@Param("companyId") Integer companyId);
}
