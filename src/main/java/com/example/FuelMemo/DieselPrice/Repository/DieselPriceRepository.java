package com.example.FuelMemo.DieselPrice.Repository;

import com.example.FuelMemo.DieselPrice.Entity.DieselPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DieselPriceRepository extends JpaRepository<DieselPrice, Integer> {

    boolean existsByDistrict_DistrictIdAndDate(Integer districtId, LocalDate date);

    Optional<DieselPrice> findTopByDistrict_DistrictIdOrderByDateDesc(Integer districtId);

    List<DieselPrice> findByDistrict_DistrictIdOrderByDateDesc(Integer districtId);

}
