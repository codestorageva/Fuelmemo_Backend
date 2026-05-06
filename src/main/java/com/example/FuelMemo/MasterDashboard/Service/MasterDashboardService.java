package com.example.FuelMemo.MasterDashboard.Service;


import com.example.FuelMemo.Shared.Response.DataResponse;

import java.time.LocalDate;

public interface MasterDashboardService {

     DataResponse getDashboardSummary(
            Integer companyId,
            LocalDate fromDate,
            LocalDate toDate
    );
}