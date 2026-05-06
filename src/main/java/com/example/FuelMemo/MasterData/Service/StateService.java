package com.example.FuelMemo.MasterData.Service;


import com.example.FuelMemo.MasterData.Dto.StateDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.StateUpdateDto;
import com.example.FuelMemo.Shared.Response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface StateService {

    MessageResponse addState(StateDto stateDto, Map<String, String> headers);

    DataResponse getStateById(int id, Map<String, String> headers);

    HttpResponse getAllStates(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateState(StateUpdateDto stateDto, int id, Map<String, String> headers);

//    MessageResponse deleteStateById(int id, Map<String, String> headers);

    MessageResponse softDeleteStateById(int id, Map<String, String> headers);

    MessageResponse restoreStateById(int id, Map<String, String> headers);

    MessageListResponse uploadStateCSV(MultipartFile file, Map<String, String> headers);

    ListResponse getAllStatesByStatusAndIsDeleted(Map<String, String> headers);

    ListResponse exportedStateData(Map<String, String> headers);

    LongResponse getTotalState();
}
