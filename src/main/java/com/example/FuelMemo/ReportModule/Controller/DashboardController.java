package com.example.FuelMemo.ReportModule.Controller;

import com.example.FuelMemo.ReportModule.DTO.ChartDto;
import com.example.FuelMemo.ReportModule.DTO.DashboardSummaryDto;
import com.example.FuelMemo.ReportModule.Service.DashboardService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RestController
//@RequestMapping("/api/dashboard")
//public class DashboardController {
//
//    private final DashboardService dashboardService;
//
//    public DashboardController(DashboardService dashboardService) {
//        this.dashboardService = dashboardService;
//    }
//
//    @GetMapping("/summary")
//    public ResponseEntity<DataResponse> getSummary() {
//        DataResponse response = dashboardService.getSummary();
//        return ResponseEntity.ok(response);
//    }
//
//    // ================= USAGE GRAPH =================
//    @GetMapping("/usage-graph")
//    public ResponseEntity<DataResponse> getUsageGraph(
//            @RequestParam(defaultValue = "DAY") String type
//    ) {
//        DataResponse response = dashboardService.getUsageGraph(type);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/royalty-pass-status")
//    public DataResponse getRoyaltyPassStatus() {
//        return dashboardService.getRoyaltyPassStatusChart();
//    }
//
//    @GetMapping("/recent-fuel-memos")
//    public DataResponse getRecentFuelMemos() {
//        return dashboardService.getRecentFuelMemos();
//    }
//}
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ================= CHART =================
    @GetMapping("/royalty-pass-status")
    public ResponseEntity<DataResponse> getRoyaltyPassStatusChart(
            @RequestParam(required = false) Integer companyId) {

        return ResponseEntity.ok(
                dashboardService.getRoyaltyPassStatusChart(companyId)
        );
    }

    // ================= RECENT MEMOS =================
    @GetMapping("/recent-fuel-memos")
    public ResponseEntity<DataResponse> getRecentFuelMemos(
            @RequestParam(required = false) Integer companyId) {

        return ResponseEntity.ok(
                dashboardService.getRecentFuelMemos(companyId)
        );
    }

    // ================= SUMMARY =================
    @GetMapping("/summary")
    public ResponseEntity<DataResponse> getSummary(
            @RequestParam(required = false) Integer companyId) {

        return ResponseEntity.ok(
                dashboardService.getSummary(companyId)
        );
    }

    // ================= USAGE GRAPH =================
    @GetMapping("/usage-graph")
    public ResponseEntity<DataResponse> getUsageGraph(
            @RequestParam String type,
            @RequestParam(required = false) Integer companyId) {

        return ResponseEntity.ok(
                dashboardService.getUsageGraph(type, companyId)
        );
    }
}