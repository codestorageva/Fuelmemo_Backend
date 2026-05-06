package com.example.FuelMemo.MasterData.Config.spring_batch.state_batch;


import com.example.FuelMemo.MasterData.Dto.StateDto;
import com.example.FuelMemo.MasterData.Entity.Country;
import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.CountryRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StateProcessor implements ItemProcessor<StateDto, State> {
    @Getter
    private static final Map<Integer, String[]> countries = new HashMap<>();
    private final CountryRepository countryRepository;
    private final String userName;
    int counter = 0;

    @Autowired
    public StateProcessor(CountryRepository countryRepository, String userName) {
        this.countryRepository = countryRepository;
        this.userName = userName;
    }

    @Override
    public State process(StateDto stateDto) throws Exception {
        State state = new State();
        state.setStateName(stateDto.getStateName());
        state.setStatus(stateDto.getStatus());
        state.setIsDeleted(false);
        state.setOperatorName(userName);
        state.setCreatedAt(LocalDateTime.now());
        state.setUpdatedAt(LocalDateTime.now());
        state.setDeletedAt(null);
        synchronized (this) {
            Country country = countryRepository.findByCountryNameIgnoreCase(stateDto.getCountryName());
            if (country != null)
                state.setCountry(country);
            else
                countries.put(counter++, new String[]{stateDto.getCountryName(), stateDto.getStateName()});
        }
        return state;
    }
}