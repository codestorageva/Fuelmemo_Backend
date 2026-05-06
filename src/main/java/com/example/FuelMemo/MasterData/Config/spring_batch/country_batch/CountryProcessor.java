package com.example.FuelMemo.MasterData.Config.spring_batch.country_batch;


import com.example.FuelMemo.MasterData.Entity.Country;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalDateTime;

@Slf4j
public class CountryProcessor implements ItemProcessor<Country, Country> {
    private final String userName;

    public CountryProcessor(String userName) {
        this.userName = userName;
    }

    @Override
    public Country process(Country country) throws Exception {
        country.setCountryName(country.getCountryName().trim());
        country.setStatus(country.getStatus());
        country.setCreatedAt(LocalDateTime.now());
        country.setUpdatedAt(LocalDateTime.now());
        country.setOperatorName(userName);
        country.setDeletedAt(null);
        country.setIsDeleted(false);
        return country;
    }
}