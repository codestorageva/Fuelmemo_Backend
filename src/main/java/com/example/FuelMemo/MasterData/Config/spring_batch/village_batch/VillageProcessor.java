package com.example.FuelMemo.MasterData.Config.spring_batch.village_batch;


import com.example.FuelMemo.MasterData.Dto.VillageDto;
import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import com.example.FuelMemo.MasterData.Entity.Village;
import com.example.FuelMemo.MasterData.Repository.SubDistrictRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VillageProcessor implements ItemProcessor<VillageDto, Village> {
    @Getter
    private static final Map<Integer, String[]> villages = new HashMap<>();
    private final SubDistrictRepository subDistrictRepository;
    private final String username;
    int counter = 0;

    @Autowired
    public VillageProcessor(SubDistrictRepository subDistrictRepository, String username) {
        this.subDistrictRepository = subDistrictRepository;
        this.username = username;
    }

    @Override
    public Village process(VillageDto villageDto) throws Exception {
        Village village = new Village();
        village.setVillageName(villageDto.getVillageName());
        village.setStatus(villageDto.getStatus());
        village.setIsDeleted(false);
        village.setOperatorName(username);
        village.setCreatedAt(LocalDateTime.now());
        village.setUpdatedAt(LocalDateTime.now());
        village.setDeletedAt(null);
        synchronized (this) {
            SubDistrict subDistrict = subDistrictRepository.findBySubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase
                    (villageDto.getSubDistrictName(), villageDto.getCityName(), villageDto.getStateName(), villageDto.getCountryName());
            if (subDistrict != null) {
                village.setSubDistrict(subDistrict);
                if (subDistrict.getDistrict() != null)
                    village.setDistrict(subDistrict.getDistrict());
                if (subDistrict.getState() != null) {
                    assert subDistrict.getDistrict() != null;
                    village.setState(subDistrict.getDistrict().getState());
                }
                if (subDistrict.getCountry() != null) {
                    assert subDistrict.getDistrict() != null;
                    village.setCountry(subDistrict.getDistrict().getState().getCountry());
                }
            } else
                villages.put(counter++, new String[]{villageDto.getCountryName(), villageDto.getStateName(), villageDto.getCityName(), villageDto.getSubDistrictName()});
        }
        return village;
    }
}