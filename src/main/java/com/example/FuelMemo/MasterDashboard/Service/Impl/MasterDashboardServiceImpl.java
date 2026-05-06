package com.example.FuelMemo.MasterDashboard.Service.Impl;



import com.example.FuelMemo.CompanyModule.Repository.CompanyRepository;

import com.example.FuelMemo.FuelMemoModule.Repository.FuelMemoRepository;
import com.example.FuelMemo.MasterDashboard.Dto.DashboardSummaryResponseDto;
import com.example.FuelMemo.MasterDashboard.Service.MasterDashboardService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.VehicleModule.Repository.VehicleRepository;
import com.example.FuelMemo.AuthModule.Repository.UserCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MasterDashboardServiceImpl implements MasterDashboardService {

    private final CompanyRepository companyRepository;
    private final FuelMemoRepository fuelMemoRepository;
    private final VehicleRepository vehicleRepository;
    private final UserCompanyRepository userCompanyRepository;

    @Override
    public DataResponse getDashboardSummary(
            Integer companyId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        Long totalCompanies;
        Long activeCompanies;
        Long inactiveCompanies;
        Long totalUsers;
        Long totalFuelMemos;
        Long totalVehicles;

        // ================= COMPANY COUNTS =================
        if (companyId == null) {
            totalCompanies = companyRepository.countByIsDeletedFalse();
            activeCompanies = companyRepository.countByIsDeletedFalseAndActiveTrue();
            inactiveCompanies = companyRepository.countByIsDeletedFalseAndActiveFalse();
        } else {
            totalCompanies = companyRepository.existsByCompanyIdAndIsDeletedFalse(companyId) ? 1L : 0L;
            activeCompanies = companyRepository.existsByCompanyIdAndIsDeletedFalseAndActiveTrue(companyId) ? 1L : 0L;
            inactiveCompanies = companyRepository.existsByCompanyIdAndIsDeletedFalseAndActiveFalse(companyId) ? 1L : 0L;
        }

        // ================= TOTAL USERS =================
        if (companyId == null) {
            totalUsers = userCompanyRepository.countDistinctActiveUsersAcrossCompanies();
        } else {
            totalUsers = userCompanyRepository.countDistinctActiveUsersByCompanyId(companyId);
        }

        // ================= TOTAL FUEL MEMOS =================
        if (companyId == null) {
            if (fromDate != null && toDate != null) {
                totalFuelMemos = fuelMemoRepository.countByDeletedFalseAndDateBetween(fromDate, toDate);
            } else {
                totalFuelMemos = fuelMemoRepository.countByDeletedFalse();
            }
        } else {
            if (fromDate != null && toDate != null) {
                totalFuelMemos = fuelMemoRepository
                        .countByCompanyCompanyIdAndDeletedFalseAndDateBetween(companyId, fromDate, toDate);
            } else {
                totalFuelMemos = fuelMemoRepository.countByCompanyCompanyIdAndDeletedFalse(companyId);
            }
        }

        // ================= TOTAL VEHICLES =================
        if (companyId == null) {
            totalVehicles = vehicleRepository.countActiveVehiclesForDashboard();
        } else {
            totalVehicles = vehicleRepository.countActiveVehiclesByCompanyId(companyId);
        }

        DashboardSummaryResponseDto responseDto = DashboardSummaryResponseDto.builder()
                .totalCompanies(totalCompanies)
                .activeCompanies(activeCompanies)
                .inactiveCompanies(inactiveCompanies)
                .totalUsers(totalUsers)
                .totalFuelMemos(totalFuelMemos)
                .totalVehicles(totalVehicles)
                .build();

        return DataResponse.<DashboardSummaryResponseDto>builder()
                .success(true)
                .data(responseDto)
                .build();
    }
}
