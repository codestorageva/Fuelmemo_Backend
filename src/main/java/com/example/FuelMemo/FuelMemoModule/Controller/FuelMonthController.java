package com.example.FuelMemo.FuelMemoModule.Controller;

import com.example.FuelMemo.FuelMemoModule.Service.FuelMonthService;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fuel-month")
public class FuelMonthController {

    private final FuelMonthService fuelMonthService;

    public FuelMonthController(FuelMonthService fuelMonthService) {
        this.fuelMonthService = fuelMonthService;
    }

    @PreAuthorize("hasAuthority('CLOSE_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/close")
    public MessageResponse closeMonth(
            @RequestParam Integer month,
            @RequestParam Integer year
//            @RequestParam String username
    ) {
        return fuelMonthService.closeFuelMonth(month, year);
    }


    @PreAuthorize("hasAuthority('REOPEN_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/reopen")
    public MessageResponse reopenMonth(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        return fuelMonthService.reopenFuelMonth(month, year);
    }
}