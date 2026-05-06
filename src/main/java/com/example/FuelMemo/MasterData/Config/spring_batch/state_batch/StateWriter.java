package com.example.FuelMemo.MasterData.Config.spring_batch.state_batch;


import com.example.FuelMemo.MasterData.Entity.State;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class StateWriter implements ItemWriter<State> {
    @Getter
    @Setter
    private static long insertedCounter = 0;
    @Getter
    @Setter
    private static long FailedCounter = 0;
    @Autowired
    private StateRepository stateRepository;

    @Override
    public void write(Chunk<? extends State> chunk) throws Exception {
        for (State state : chunk) {
            synchronized (this) {
                try {
                    if (state.getCountry() != null && (!stateRepository.existsByStateNameIgnoreCaseAndCountry_CountryNameIgnoreCase(state.getStateName(), state.getCountry().getCountryName()))) {
                        stateRepository.save(state);
                        insertedCounter++;
                    } else {
                        FailedCounter++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}