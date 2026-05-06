package com.example.FuelMemo.FuelMemoModule.Service.Impl;

import com.example.FuelMemo.FuelMemoModule.Entity.FuelMonth;
import com.example.FuelMemo.FuelMemoModule.Repository.FuelMonthRepository;
import com.example.FuelMemo.FuelMemoModule.Service.FuelMonthService;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class FuelMonthServiceImpl implements FuelMonthService {

    private final FuelMonthRepository fuelMonthRepository;

    public FuelMonthServiceImpl(FuelMonthRepository fuelMonthRepository) {
        this.fuelMonthRepository = fuelMonthRepository;
    }

    @Override
    @Transactional
    public MessageResponse closeFuelMonth(Integer month, Integer year) {

        FuelMonth fuelMonth = fuelMonthRepository
                .findByMonthAndYearAndDeletedFalse(month, year)
                .orElseThrow(() -> new RuntimeException("Fuel month not found"));

        if (fuelMonth.getClosed()) {
            throw new RuntimeException("Month already closed");
        }

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        fuelMonth.closeMonth(username);

        fuelMonthRepository.save(fuelMonth);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Fuel Month Closed Successfully")
                .build();
    }

    @Override
    public MessageResponse reopenFuelMonth(Integer month, Integer year) {

        FuelMonth fuelMonth = fuelMonthRepository
                .findByMonthAndYearAndDeletedFalse(month, year)
                .orElseThrow(() -> new RuntimeException("Fuel month not found"));

        if (!fuelMonth.getClosed()) {
            throw new RuntimeException("Month is already open");
        }

        // 🔐 Get username from JWT
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        fuelMonth.reopenMonth(username);

        fuelMonthRepository.save(fuelMonth);

        return MessageResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .message("Fuel Month Reopened Successfully")
                .build();
    }
}