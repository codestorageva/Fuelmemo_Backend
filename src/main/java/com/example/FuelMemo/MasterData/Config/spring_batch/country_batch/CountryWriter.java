package com.example.FuelMemo.MasterData.Config.spring_batch.country_batch;


import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Repository.CountryRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CountryWriter implements ItemWriter<Country> {
    @Getter
    @Setter
    private static long counter = 0;
    @Autowired
    private CountryRepository countryRepository;

    @Override
    public void write(Chunk<? extends Country> chunk) throws Exception {
        for (Country country : chunk) {
            synchronized (this) {
                if (!countryRepository.existsByCountryNameIgnoreCase(country.getCountryName())) {
                    countryRepository.save(country);
                    counter++;
                }
            }
        }
    }
}