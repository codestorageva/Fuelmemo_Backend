package com.example.FuelMemo.DriverModule.Service;

import com.example.FuelMemo.DriverModule.Dto.DriverRequestDto;
import com.example.FuelMemo.DriverModule.Dto.DriverUpdateDto;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;

public interface DriverService {

    // Create / Add driver to a company
    MessageResponse createDriver(DriverRequestDto dto);

    // Update driver info / add company to existing driver
    MessageResponse updateDriver(Integer driverId, DriverUpdateDto dto);

    // Soft delete driver for a company
    MessageResponse deleteDriver(Integer driverId, Integer companyId);

    // Restore driver for a company
    MessageResponse restoreDriver(Integer driverId, Integer companyId);

    // Get driver by ID (company validated)
    DataResponse getDriverById(Integer driverId, Integer companyId);

    // Get all drivers of a company
    HttpResponse getAllDrivers(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted
    );

    // Get all drivers filtered by a specific company
    HttpResponse getDriversByCompanyId(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted
    );
}