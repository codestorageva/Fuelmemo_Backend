package com.example.FuelMemo.MasterData.Repository;

import com.example.FuelMemo.MasterData.Entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, Integer> {
    List<Village> findAllBySubDistrict(SubDistrict subDistrict);

    List<Village> findAllByState(State state);

    List<Village> findAllByCountry(Country country);

    List<Village> findAllByDistrict(District district);

    List<Village> findAllBySubDistrict_Status(boolean b);

    boolean existsByVillageName(String name);

    Village findByVillageNameIgnoreCase(String name);

    Page<Village> findAllByVillageIdOrStatusOrVillageNameContainingIgnoreCaseOrSubDistrict_SubDistrictNameContainingIgnoreCaseOrDistrict_CityNameContainingIgnoreCaseOrState_StateNameContainingIgnoreCaseOrCountry_CountryNameContainingIgnoreCase
            (Integer villageId, Boolean status, String villageName, String subDistrictName, String cityName, String stateName, String countryName, Pageable p);

    Page<Village> findAllByIsDeleted(Boolean b, Pageable p);

    List<Village> findAllByStatusAndIsDeleted(Boolean b1, Boolean b2);

    List<Village> findAllByCountry_CountryId(Integer countryId);

    List<Village> findAllByState_StateId(Integer stateId);

    List<Village> findAllByDistrict_DistrictId(Integer districtId);

    List<Village> findAllBySubDistrict_SubDistrictId(Integer subDistrictId);

    boolean existsByVillageNameIgnoreCaseAndSubDistrict_SubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(String villageName, String subDistrictName, String cityName, String stateName, String countryName);

    Village findByVillageNameIgnoreCaseAndSubDistrict_SubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(String trim, String trim1, String trim2, String trim3, String trim4);
}
