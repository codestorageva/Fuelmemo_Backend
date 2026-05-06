package com.example.FuelMemo.MasterData.Repository;


import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubDistrictRepository extends JpaRepository<SubDistrict, Integer> {

    List<SubDistrict> findAllByDistrict(District district);

    List<SubDistrict> findAllByState(State state);

    List<SubDistrict> findAllByCountry(Country country);

    SubDistrict findBySubDistrictNameIgnoreCase(String name);

    SubDistrict findBySubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(String subDistrictName, String cityName, String stateName, String countryName);

    boolean existsBySubDistrictName(String name);

    Page<SubDistrict> findAllBySubDistrictIdOrStatusOrSubDistrictNameContainingIgnoreCaseOrDistrict_CityNameContainingIgnoreCaseOrState_StateNameContainingIgnoreCaseOrCountry_CountryNameContainingIgnoreCase
            (Integer subDistrictId, Boolean status, String subDistrictName, String cityName, String stateName, String countryName, Pageable p);

    Page<SubDistrict> findAllByIsDeleted(Boolean b, Pageable p);

    List<SubDistrict> findAllByStatusAndIsDeleted(Boolean b1, Boolean b2);

    List<SubDistrict> findAllByCountry_CountryId(Integer countryId);

    List<SubDistrict> findAllByState_StateId(Integer stateId);

    List<SubDistrict> findAllByDistrict_DistrictId(Integer districtId);

    boolean existsBySubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(String subDistrictName, String cityName, String stateName, String countryName);
}
