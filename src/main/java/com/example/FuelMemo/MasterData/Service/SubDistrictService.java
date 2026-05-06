package com.example.FuelMemo.MasterData.Service;


import com.example.FuelMemo.MasterData.Dto.SubDistrictDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.SubDistrictUpdateDto;
import com.example.FuelMemo.Shared.Response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface SubDistrictService {

    MessageResponse addSubDistrict(SubDistrictDto subDistrictDto, Map<String, String> headers);

    DataResponse getSubDistrictById(int id, Map<String, String> headers);

    HttpResponse getAllSubDistricts(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateSubDistrict(SubDistrictUpdateDto subDistrictDto, int id, Map<String, String> headers);

//    MessageResponse deleteSubDistrictById(int id, Map<String, String> headers);

    MessageResponse softDeleteSubDistrictById(int id, Map<String, String> headers);

    MessageResponse restoreSubDistrictById(int id, Map<String, String> headers);

    ListResponse getAllSubDistrictsByStatusAndIsDeleted(int districtId, Map<String, String> headers);

    ListResponse exportedSubDistrictData(Map<String, String> headers);

    MessageListResponse uploadSubDistrictCSV(MultipartFile file, Map<String, String> headers);

    LongResponse getTotalSubDistrict();
}
