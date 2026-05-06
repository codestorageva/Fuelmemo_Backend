package com.example.FuelMemo.RoyaltyModule.Service;

import com.example.FuelMemo.RoyaltyModule.Dto.Request.RoyaltyPassUpdateDto;
import com.example.FuelMemo.RoyaltyModule.Dto.RoyaltyPassDto;
import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageListResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;

public interface RoyaltyPassService {
    MessageResponse createRoyaltyPass(RoyaltyPassDto dto);
    DataResponse getRoyaltyPassById(Integer royaltyId);
    public DataResponse getRoyaltyPassesByCompanyId(Integer companyId, Boolean isDeleted, String status);
    MessageListResponse getAllDeletedRoyaltyPasses();
    public HttpResponse getAllRoyaltyPasses(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortDir,
            Boolean isDeleted);
    MessageResponse updateRoyaltyPass(Integer royaltyId, RoyaltyPassUpdateDto dto);
    MessageResponse softDeleteRoyaltyPass(Integer royaltyId);
    MessageResponse restoreRoyaltyPass(Integer royaltyId);
    MessageResponse deleteRoyaltyPass(Integer royaltyId);
    DataResponse getRoyaltyDropdown(Integer companyId, Integer selectedRoyaltyId);
}
