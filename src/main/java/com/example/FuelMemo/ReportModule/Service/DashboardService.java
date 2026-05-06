package com.example.FuelMemo.ReportModule.Service;

import com.example.FuelMemo.ReportModule.DTO.ChartDto;
import com.example.FuelMemo.ReportModule.DTO.DashboardSummaryDto;
import com.example.FuelMemo.Shared.Response.DataResponse;

import java.util.List;

public interface DashboardService {

    DataResponse getRoyaltyPassStatusChart(Integer companyId);

    DataResponse getRecentFuelMemos(Integer companyId);

    DataResponse getSummary(Integer companyId);

    DataResponse getUsageGraph(String type, Integer companyId);
}
