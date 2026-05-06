package com.example.FuelMemo.MasterData.Repository;


import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    Page<District> findAllByState(State state, Pageable p);

    Page<District> findAllByCountry(Country country, Pageable p);

    District findByCityNameIgnoreCase(String name);

    District findByCityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(String districtName, String stateName, String countryName);

    boolean existsByCityNameIgnoreCase(String name);

    List<District> findAllByState_Status(Boolean b);

    Page<District> findAllByDistrictIdOrStatusOrCityNameContainingIgnoreCaseOrState_StateNameContainingIgnoreCaseOrCountry_CountryNameContainingIgnoreCase
            (Integer districtId, Boolean status, String cityName, String stateName, String countryName, Pageable p);

    Page<District> findAllByIsDeleted(Boolean b, Pageable p);

    List<District> findAllByStatusAndIsDeleted(Boolean b1, Boolean b2);

    List<District> findAllByCountry_CountryId(Integer countryId);

    List<District> findAllByState_StateId(Integer stateId);

    District findByCityName(String DistricName);

    boolean existsByCityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(String cityName, String stateName, String countryName);

    List<District> findByIsDeletedFalseAndStatusTrue();
}
