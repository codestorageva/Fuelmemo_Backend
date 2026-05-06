package com.example.FuelMemo.MasterData.Config.spring_batch.district_batch;


import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DistrictWriter implements ItemWriter<District> {
    @Getter
    @Setter
    private static long insertedCounter = 0;
    @Getter
    @Setter
    private static long failedCounter = 0;
    @Autowired
    private DistrictRepository districtRepository;

    @Override
    public void write(Chunk<? extends District> chunk) throws Exception {
        for (District district : chunk) {
            synchronized (this) {
                try {
                    if (district.getState() != null && (!districtRepository.existsByCityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(district.getCityName(), district.getState().getStateName(), district.getCountry().getCountryName()))) {
                        districtRepository.save(district);
                        insertedCounter++;
                    } else {
                        failedCounter++;
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

}