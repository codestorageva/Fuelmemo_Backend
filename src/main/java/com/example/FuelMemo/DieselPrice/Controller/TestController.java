package com.example.FuelMemo.DieselPrice.Controller;

import com.example.FuelMemo.DieselPrice.Service.FuelPriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final FuelPriceService service;

    public TestController(FuelPriceService service) {
        this.service = service;
    }

    @GetMapping("/test-fetch")
    public String test() {
        service.fetchFuelPricesDaily(); // ✅ correct method name
        return "Data Fetched Successfully";
    }
}