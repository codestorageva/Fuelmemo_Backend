package com.example.FuelMemo.SiteModule.Controller;

import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.SiteModule.Dto.SiteRequestDTO;
import com.example.FuelMemo.SiteModule.Dto.SiteUpdateDTO;
import com.example.FuelMemo.SiteModule.Service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    // ================= CREATE SITE =================
    @PreAuthorize("hasAuthority('CREATE_SITE')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/create")
    public ResponseEntity<MessageResponse> createSite(@Valid @RequestBody SiteRequestDTO dto) {
        MessageResponse response = siteService.createSite(dto);
        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    @PreAuthorize("hasAuthority('UPDATE_SITE')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/update/{siteId}")
    public ResponseEntity<MessageResponse> updateSite(
            @PathVariable Integer siteId,
            @RequestBody SiteUpdateDTO dto) {

        MessageResponse response = siteService.updateSiteById(dto, siteId);
        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    // Soft Delete
    @PreAuthorize("hasAuthority('DELETE_SITE')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/soft-delete/{siteId}")
    public ResponseEntity<MessageResponse> softDeleteSite(@PathVariable Integer siteId) {

        MessageResponse response = siteService.softDeleteSiteById(siteId);
        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    // Restore
    @PreAuthorize("hasAuthority('RESTORE_SITE')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/restore/{siteId}")
    public ResponseEntity<MessageResponse> restoreSite(@PathVariable Integer siteId) {

        MessageResponse response = siteService.restoreSiteById(siteId);
        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    @PreAuthorize("hasAuthority('VIEW_SITE')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/{siteId}")
    public ResponseEntity<DataResponse> getSiteById(
            @PathVariable Integer siteId) {

        DataResponse response = siteService.getSiteById(siteId);

        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    @PreAuthorize("hasAuthority('VIEW_SITE')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public HttpResponse getAllSites(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "siteId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isDeleted) {

        return siteService.getAllSites(
                keyword,
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                isDeleted
        );
    }

    @PreAuthorize("hasAuthority('VIEW_SITE')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/company/{companyId}")
    public HttpResponse getAllSitesByCompany(
            @PathVariable Integer companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "siteId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isDeleted) {

        return siteService.getAllSitesByCompany(
                companyId,
                keyword,
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                isDeleted
        );
    }

}