package com.example.FuelMemo.MasterData.Service;


import com.example.FuelMemo.MasterData.Dto.DistrictDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.DistrictUpdateDto;
import com.example.FuelMemo.Shared.Response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface DistrictService {
    MessageResponse addDistrict(DistrictDto districtDto, Map<String, String> headers);

    DataResponse getDistrictById(int id, Map<String, String> headers);

    public HttpResponse getAllDistricts(String keyword,
                                        int pageNumber,
                                        int pageSize,
                                        String sortBy,
                                        String sortDirection,
                                        String isDeleted,
                                        String status,
                                        String authorization
                                        );

    MessageResponse updateDistrict(DistrictUpdateDto districtDto, int id, Map<String, String> headers);

//    MessageResponse deleteDistrictById(int id, Map<String, String> headers);

    MessageResponse softDeleteDistrictById(int id, Map<String, String> headers);

    MessageResponse restoreDistrictById(int id, Map<String, String> headers);

    ListResponse getAllDistrictsByStatusAndIsDeleted(int stateId, Map<String, String> headers);

    ListResponse exportedDistrictData(Map<String, String> headers);

    MessageListResponse uploadDistrictCSV(MultipartFile file, Map<String, String> headers);

    LongResponse getTotalDistrict();
}
