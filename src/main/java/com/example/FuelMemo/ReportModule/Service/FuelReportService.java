package com.example.FuelMemo.ReportModule.Service;

import com.example.FuelMemo.ReportModule.DTO.FuelMemoDetailReportDto;
import com.example.FuelMemo.ReportModule.DTO.SiteFuelReportDto;
import com.example.FuelMemo.ReportModule.DTO.SiteFuelReportFilterDto;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface FuelReportService {



//    ResponseEntity<byte[]> getFuelMemoDetailReportExcel(Integer companyId);

    // =========================================================
    // ✅ JSON DATA (TABLE VIEW)
    // =========================================================
    List<FuelMemoDetailReportDto> getFuelMemoDetailData(
            Integer companyId,
            Integer srNo
    );

    // =========================================================
    // ✅ JSON REPORT WITH PAGINATION + TOTAL (FRONTEND TABLE)
    // =========================================================
    HttpResponse getFuelMemoDetailReport(
            Integer companyId,
            Integer srNo,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDirection
    );

    // =========================================================
    // ✅ EXCEL DOWNLOAD (DETAIL REPORT)
    // =========================================================
    ResponseEntity<byte[]> getFuelMemoDetailExcel(
            Integer companyId,
            Integer srNo
    );

    // =========================================================
    // ✅ SITE WISE REPORT (EXCEL)
    // =========================================================
    ResponseEntity<byte[]> getSiteWiseFuelReportExcel(
            SiteFuelReportFilterDto filter
    );

    // =========================================================
    // ✅ DATE WISE REPORT (EXCEL)
    // =========================================================
    ResponseEntity<byte[]> getDateWiseFuelMemoReportExcel(
            Integer companyId,
            LocalDate fromDate,
            LocalDate toDate);

    HttpResponse<List<SiteFuelReportDto>> getSiteWiseFuelReportData(SiteFuelReportFilterDto filter);


}