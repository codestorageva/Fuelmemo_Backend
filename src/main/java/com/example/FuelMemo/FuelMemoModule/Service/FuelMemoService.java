package com.example.FuelMemo.FuelMemoModule.Service;

import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoCreateResponse;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoRequestDto;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoUpdateRequestDto;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;


public interface FuelMemoService {

    FuelMemoCreateResponse createFuelMemo(FuelMemoRequestDto request);

    MessageResponse updateFuelMemo(Integer id, FuelMemoUpdateRequestDto request);
    MessageResponse deleteFuelMemo(Integer id);
    MessageResponse restoreFuelMemo(Integer id);
//    List<FuelMemoResponseDto> getAllFuelMemos();

    HttpResponse<?> getAllFuelMemos(String keyword,
                                    Integer pageNumber,
                                    Integer pageSize,
                                    String sortBy,
                                    String sortDirection,
                                    String isDeleted,
                                    Integer month,
                                    Integer year,String monthStatus);

    DataResponse getFuelMemoById(Integer id);

    MessageResponse createAdjustmentEntry(FuelMemoRequestDto request);
    HttpResponse<?> getAllFuelMemosByCompany(Integer companyId,
                                             String keyword,
                                             Integer pageNumber,
                                             Integer pageSize,
                                             String sortBy,
                                             String sortDirection,
                                             String isDeleted,
                                             Integer month,
                                             Integer year,String monthStatus);

}
