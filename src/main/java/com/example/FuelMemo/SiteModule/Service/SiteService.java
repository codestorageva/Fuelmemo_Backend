package com.example.FuelMemo.SiteModule.Service;

import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.SiteModule.Dto.SiteRequestDTO;
import com.example.FuelMemo.SiteModule.Dto.SiteUpdateDTO;

public interface SiteService {

    MessageResponse createSite(SiteRequestDTO dto);
    MessageResponse updateSiteById(SiteUpdateDTO dto, Integer siteId);
    MessageResponse softDeleteSiteById(Integer siteId);

    MessageResponse restoreSiteById(Integer siteId);

    DataResponse getSiteById(Integer siteId);

    HttpResponse getAllSites(
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted
    );
    HttpResponse getAllSitesByCompany(
            Integer companyId,
            String keyword,
            int pageNumber,
            int pageSize,
            String sortBy,
            String sortDirection,
            Boolean isDeleted
    );

}