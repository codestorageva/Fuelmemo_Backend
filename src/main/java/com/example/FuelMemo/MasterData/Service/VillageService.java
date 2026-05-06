package com.example.FuelMemo.MasterData.Service;


import com.example.FuelMemo.MasterData.Dto.UpdateDto.VillageUpdateDto;
import com.example.FuelMemo.MasterData.Dto.VillageDto;
import com.example.FuelMemo.Shared.Response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VillageService {
    MessageResponse addVillage(VillageDto villageDto, Map<String, String> headers);

    DataResponse getVillageById(int id, Map<String, String> headers);

    HttpResponse getAllVillages(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateVillage(VillageUpdateDto villageDto, int villageId, Map<String, String> headers);

//    MessageResponse deleteVillageById(int id, Map<String, String> headers);

    MessageResponse softDeleteVillageById(int villageId, Map<String, String> headers);

    MessageResponse restoreVillageById(int villageId, Map<String, String> headers);

    ListResponse getAllVillagesByStatusAndIsDeleted(int subDistrictId, Map<String, String> headers);

    ListResponse exportedVillageData(Map<String, String> headers);

    MessageListResponse uploadVillageCSV(MultipartFile file, Map<String, String> headers);

    LongResponse getTotalVillage();
}
