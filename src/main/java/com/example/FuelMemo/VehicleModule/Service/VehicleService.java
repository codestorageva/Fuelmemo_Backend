package com.example.FuelMemo.VehicleModule.Service;

import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.VehicleModule.Dto.CompanyVehicleResponseDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleCreateDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleUpdateDTO;

public interface VehicleService {


    MessageResponse createVehicle(VehicleCreateDTO dto);


    MessageResponse updateVehicle(Integer vehicleId, VehicleUpdateDTO dto);


    MessageResponse deleteVehicle(Integer vehicleId, Integer companyId);


    MessageResponse restoreVehicle(Integer vehicleId, Integer companyId);

    DataResponse getVehicleById(Integer vehicleId, Integer companyId);


    HttpResponse getAllVehicles(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted
    );

    // ✅ Get By Company
    HttpResponse<CompanyVehicleResponseDTO> getVehiclesByCompanyId(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted
    );
}