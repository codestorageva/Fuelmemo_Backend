package com.example.FuelMemo.MasterData.Config.spring_batch.sub_district_batch;

import com.example.FuelMemo.MasterData.Entity.SubDistrict;
import com.example.FuelMemo.MasterData.Repository.SubDistrictRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SubDistrictWriter implements ItemWriter<SubDistrict> {
    @Getter
    @Setter
    private static long insertedCounter = 0;
    @Getter
    @Setter
    private static long failedCounter = 0;
    @Autowired
    private SubDistrictRepository subDistrictRepository;

    @Override
    public void write(Chunk<? extends SubDistrict> chunk) throws Exception {
        for (SubDistrict subDistrict : chunk) {
            synchronized (this) {
                try {
                    if (subDistrict.getDistrict() != null && (!subDistrictRepository.existsBySubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase
                            (subDistrict.getSubDistrictName(), subDistrict.getDistrict().getCityName(), subDistrict.getState().getStateName(), subDistrict.getCountry().getCountryName()))) {
                        subDistrictRepository.save(subDistrict);
                        insertedCounter++;
                    } else {
                        failedCounter++;
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

}