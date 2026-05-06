package com.example.FuelMemo.FuelStationModule.Repository;

import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelStationRepository extends JpaRepository<FuelStation, Integer> {
    FuelStation findByFuelStationIdAndDeletedFalse(Integer fuelStationId);
    List<FuelStation> findByCompanyCompanyIdAndDeletedFalse(Integer companyId);
    List<FuelStation> findByDeletedTrue();
    boolean existsByFuelStationNameIgnoreCaseAndFuelStationIdNot(String fuelStationName, Integer fuelStationId);

    boolean existsByCompanyCompanyId(Integer companyId);

    List<FuelStation> findByCompanyCompanyIdAndDeletedTrue(Integer companyId);
}
