package com.example.FuelMemo.ReportModule.Controller;

import com.example.FuelMemo.ReportModule.DTO.SiteFuelReportDto;
import com.example.FuelMemo.ReportModule.DTO.SiteFuelReportFilterDto;
import com.example.FuelMemo.ReportModule.Service.FuelReportService;

import com.example.FuelMemo.Shared.Response.HttpResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class FuelReportController {

    private final FuelReportService fuelReportService;

    @PreAuthorize("hasAuthority('VIEW_SITE_REPORT')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/site/excel")
    public ResponseEntity<byte[]> getSiteReport(@RequestBody SiteFuelReportFilterDto filter) {
        return fuelReportService.getSiteWiseFuelReportExcel(filter);
    }

    @PreAuthorize("hasAuthority('VIEW_SITE_REPORT')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/site-wise")
    public ResponseEntity<HttpResponse<?>> getSiteWiseReport(
            @RequestBody SiteFuelReportFilterDto filter) {

        return ResponseEntity.ok(
                fuelReportService.getSiteWiseFuelReportData(filter)
        );
    }

//    @PreAuthorize("hasAuthority('VIEW_FUEL_MEMO_REPORT')")
//    @GetMapping("/detail/excel")
//    public ResponseEntity<byte[]> getDetailReport(@RequestParam Integer companyId) {
//        return fuelReportService.getFuelMemoDetailReportExcel(companyId);
//    }

    @GetMapping("/date/excel")
    @PreAuthorize("hasAuthority('VIEW_FUEL_MEMO_REPORT')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> getDateWiseReport(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam




            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam Integer companyId
    ) {
        return fuelReportService.getDateWiseFuelMemoReportExcel(companyId, fromDate, toDate);
    }

    @GetMapping("/fuel-memo-report")
    @PreAuthorize("hasAuthority('VIEW_FUEL_MEMO_REPORT')or hasRole('ROLE_SUPERADMIN')")
    public HttpResponse getReport(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer srNo,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return fuelReportService.getFuelMemoDetailReport(
                companyId, srNo, pageNumber, pageSize, sortBy, sortDirection
        );
    }
    @GetMapping("/fuel-memo-report/excel")
    @PreAuthorize("hasAuthority('EXPORT_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<byte[]> downloadExcel(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer srNo
    ) {
        return fuelReportService.getFuelMemoDetailExcel(companyId, srNo);
    }
}