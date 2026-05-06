package com.example.FuelMemo.MasterDashboard.Controller;

import com.example.FuelMemo.MasterDashboard.Dto.DashboardSummaryResponseDto;
import com.example.FuelMemo.MasterDashboard.Service.MasterDashboardService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/master-dashboard")
@RequiredArgsConstructor
public class MasterDashboardController {

    private final MasterDashboardService masterDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DataResponse> getDashboardSummary(
            @RequestParam(required = false) Integer companyId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(
                masterDashboardService.getDashboardSummary(companyId, fromDate, toDate)
        );
    }
}