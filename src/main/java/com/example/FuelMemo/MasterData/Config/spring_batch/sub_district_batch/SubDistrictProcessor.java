package com.example.FuelMemo.MasterData.Config.spring_batch.sub_district_batch;

import com.example.FuelMemo.MasterData.Dto.SubDistrictDto;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SubDistrictProcessor implements ItemProcessor<SubDistrictDto, SubDistrict> {
    @Getter
    private static final Map<Integer, String[]> subDistricts = new HashMap<>();
    private final DistrictRepository districtRepository;
    private final String username;
    int counter = 0;

    @Autowired
    public SubDistrictProcessor(DistrictRepository districtRepository, String username) {
        this.districtRepository = districtRepository;
        this.username = username;
    }

    @Override
    public SubDistrict process(SubDistrictDto subDistrictDto) throws Exception {
        SubDistrict subDistrict = new SubDistrict();
        subDistrict.setSubDistrictName(subDistrictDto.getSubDistrictName());
        subDistrict.setStatus(subDistrictDto.getStatus());
        subDistrict.setIsDeleted(false);
        subDistrict.setOperatorName(username);
        subDistrict.setCreatedAt(LocalDateTime.now());
        subDistrict.setUpdatedAt(LocalDateTime.now());
        subDistrict.setDeletedAt(null);
        synchronized (this) {
            District district = districtRepository.findByCityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(subDistrictDto.getCityName(), subDistrictDto.getStateName(), subDistrictDto.getCountryName());
            if (district != null) {
                subDistrict.setDistrict(district);
                if (district.getState() != null)
                    subDistrict.setState(district.getState());
                if (district.getCountry() != null) {
                    assert district.getState() != null;
                    subDistrict.setCountry(district.getState().getCountry());
                }
            } else
                subDistricts.put(counter++, new String[]{subDistrictDto.getCountryName(), subDistrictDto.getStateName(), subDistrictDto.getCityName(), subDistrictDto.getSubDistrictName()});
        }
        return subDistrict;
    }
}