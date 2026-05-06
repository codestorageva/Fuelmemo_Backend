package com.example.FuelMemo.DieselPrice.scheduler;

import com.example.FuelMemo.DieselPrice.Service.FuelPriceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DieselScheduler {

    private final FuelPriceService service;

    public DieselScheduler(FuelPriceService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void runDaily() {
        service.fetchFuelPricesDaily(); // ✅ correct method name
    }
}