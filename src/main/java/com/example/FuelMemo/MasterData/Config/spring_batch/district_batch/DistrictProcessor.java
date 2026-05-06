package com.example.FuelMemo.MasterData.Config.spring_batch.district_batch;


import com.example.FuelMemo.MasterData.Dto.DistrictDto;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DistrictProcessor implements ItemProcessor<DistrictDto, District> {
    @Getter
    private static final Map<Integer, String[]> districts = new HashMap<>();
    private final StateRepository stateRepository;
    private final String username;
    int counter = 0;

    @Autowired
    public DistrictProcessor(StateRepository stateRepository, String username) {
        this.stateRepository = stateRepository;
        this.username = username;
    }

    @Override
    public District process(DistrictDto districtDto) throws Exception {
        District district = new District();
        district.setCityName(districtDto.getCityName());
        if (districtDto.getApiCityName() != null && !districtDto.getApiCityName().isEmpty()) {
            district.setApiCityName(districtDto.getApiCityName().trim());
        } else {
            district.setApiCityName(districtDto.getCityName().trim());
        }
        district.setStatus(districtDto.getStatus());
        district.setIsDeleted(false);
        district.setOperatorName(username);
        district.setCreatedAt(LocalDateTime.now());
        district.setUpdatedAt(LocalDateTime.now());
        district.setDeletedAt(null);
        synchronized (this) {
            State state = stateRepository.findByStateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(districtDto.getStateName(), districtDto.getCountryName());
            if (state != null) {
                district.setState(state);
                if (state.getCountry() != null) {
                    district.setCountry(state.getCountry());
                }
            } else
                districts.put(counter++, new String[]{districtDto.getCountryName(), districtDto.getStateName(), districtDto.getCityName()});
        }
        return district;
    }
}