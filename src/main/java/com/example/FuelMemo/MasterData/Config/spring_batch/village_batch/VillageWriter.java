package com.example.FuelMemo.MasterData.Config.spring_batch.village_batch;


import com.example.FuelMemo.MasterData.Entity.Village;
import com.example.FuelMemo.MasterData.Repository.VillageRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class VillageWriter implements ItemWriter<Village> {
    @Getter
    @Setter
    private static long insertedCounter = 0;
    @Getter
    @Setter
    private static long failedCounter = 0;
    @Autowired
    private VillageRepository villageRepository;

    @Override
    public void write(Chunk<? extends Village> chunk) throws Exception {
        for (Village village : chunk) {
            synchronized (this) {
                try {
                    if (village.getSubDistrict() != null && (!villageRepository.existsByVillageNameIgnoreCaseAndSubDistrict_SubDistrictNameIgnoreCaseAndDistrict_CityNameIgnoreCaseAndState_StateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(village.getVillageName(), village.getSubDistrict().getSubDistrictName(), village.getDistrict().getCityName(), village.getState().getStateName(), village.getCountry().getCountryName()))) {
                        villageRepository.save(village);
                        insertedCounter++;
                    } else
                        failedCounter++;
                } catch (Exception ignore) {
                }
            }
        }
    }
}