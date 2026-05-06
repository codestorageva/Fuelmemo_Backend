package com.example.FuelMemo.FuelStationModule.Service;

import com.example.FuelMemo.FuelStationModule.Dto.FuelStationDto;
import com.example.FuelMemo.FuelStationModule.Dto.Request.FuelStationUpdateDto;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;

public interface FuelStationService {
    MessageResponse createFuelStation(FuelStationDto dto);
    DataResponse getFuelStationById(Integer fuelStationId);
    DataResponse getFuelStationsByCompanyId(Integer companyId);
    DataResponse getAllDeletedFuelStations();
    HttpResponse getAllFuelStations(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir);
    MessageResponse updateFuelStation(Integer fuelStationId, FuelStationUpdateDto dto);
    MessageResponse softDeleteFuelStation(Integer fuelStationId);
    MessageResponse restoreFuelStation(Integer fuelStationId);
    MessageResponse deleteFuelStation(Integer fuelStationId);
}
