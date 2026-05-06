package com.example.FuelMemo.CompanyModule.Service;

import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

public interface CompanyService {

    MessageResponse addCompany(
            String companyName,
            String ownerName,
            String password,
            MultipartFile logo,
            String panNumber,
            String gstNumber,
            String email,
            String mobileNo,
            String alternateMobileNumber,
            String cinNumber,
            String tanNumber,
            String industry,
            String addressLine1,
            String addressLine2,
            String addressLine3,
            String serviceDescription,
            Boolean status,
            String pinCode,
            Integer stateId,
            Integer districtId

    );

    DataResponse getCompanyById(Integer companyId);

    DataResponse getAllDeletedCompanies();


    HttpResponse getAllCompanies(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted,
            Authentication authentication
    );
    MessageResponse updateCompany(
            String companyName,
            String ownerName,
            String password,
            MultipartFile logo,
            String panNumber,
            String gstNumber,
            String email,
            String mobileNo,
            String alternateMobileNumber,
            String cinNumber,
            String tanNumber,
            String industry,
            String addressLine1,
            String addressLine2,
            String addressLine3,
            String serviceDescription,
            Boolean status,
            String pinCode,
            Integer stateId,
            Integer districtId,
            Integer companyId,
            Map<String, String> headers
    );

    MessageResponse softDeleteCompany(Integer companyId);

    MessageResponse restoreCompany(Integer companyId);

    MessageResponse deleteCompany(Integer companyId);
}