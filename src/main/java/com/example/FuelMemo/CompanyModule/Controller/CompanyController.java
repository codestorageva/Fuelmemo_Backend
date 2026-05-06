package com.example.FuelMemo.CompanyModule.Controller;

import com.example.FuelMemo.CompanyModule.Service.CompanyService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")

public class CompanyController {

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    private final CompanyService companyService;

    @PreAuthorize("hasAuthority('CREATE_COMPANY') or hasRole('ROLE_SUPERADMIN')")
    @PostMapping(value = "/create")
    public ResponseEntity<MessageResponse> addCompany(

            @RequestParam String companyName,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String gstNumber,
            @RequestParam String email,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String alternateMobileNumber,
            @RequestParam(required = false) String cinNumber,
            @RequestParam(required = false) String tanNumber,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String addressLine1,
            @RequestParam(required = false) String addressLine2,
            @RequestParam(required = false) String addressLine3,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String pinCode,
            @RequestParam Integer stateId,
            @RequestParam Integer districtId

    ) {

        MessageResponse response = companyService.addCompany(
                companyName,
                ownerName,
                password,
                logo,
                panNumber,
                gstNumber,
                email,
                mobileNo,
                alternateMobileNumber,
                cinNumber,
                tanNumber,
                industry,
                addressLine1,
                addressLine2,
                addressLine3,
                serviceDescription,
                status,
                pinCode,
                stateId,
                districtId
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('VIEW_COMPANY')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/select/{companyId}")
    public ResponseEntity<DataResponse> getCompanyById(@PathVariable Integer companyId) {
        DataResponse response = companyService.getCompanyById(companyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('VIEW_COMPANY')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/deleted")
    public ResponseEntity<DataResponse> getAllDeletedCompanies() {
        DataResponse response = companyService.getAllDeletedCompanies();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('VIEW_COMPANY') or hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllCompanies(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "companyName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Boolean isDeleted,
            Authentication authentication
    ) {

        HttpResponse response = companyService.getAllCompanies(
                keyword,
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                isDeleted,
                authentication
        );

        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    @PreAuthorize("hasAuthority('UPDATE_COMPANY')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping(value = "/update/{companyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> updateCompany(

            @PathVariable int companyId,

            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) MultipartFile logo,
            @RequestParam(required = false) String panNumber,
            @RequestParam(required = false) String gstNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String alternateMobileNumber,
            @RequestParam(required = false) String cinNumber,
            @RequestParam(required = false) String tanNumber,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String addressLine1,
            @RequestParam(required = false) String addressLine2,
            @RequestParam(required = false) String addressLine3,
            @RequestParam(required = false) String serviceDescription,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String pinCode,
            @RequestParam(required = false) Integer stateId,
            @RequestParam(required = false) Integer districtId,

            @RequestHeader Map<String, String> headers

    ) {

        MessageResponse response = companyService.updateCompany(
                companyName,
                ownerName,
                password,
                logo,
                panNumber,
                gstNumber,
                email,
                mobileNo,
                alternateMobileNumber,
                cinNumber,
                tanNumber,
                industry,
                addressLine1,
                addressLine2,
                addressLine3,
                serviceDescription,
                status,
                pinCode,
                stateId,
                districtId,
                companyId,
                headers
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DELETE_COMPANY')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/softDelete/{companyId}")
    public ResponseEntity<MessageResponse> softDeleteCompany(@PathVariable Integer companyId) {
        MessageResponse response = companyService.softDeleteCompany(companyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('RESTORE_COMPANY')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/restore/{companyId}")
    public ResponseEntity<MessageResponse> restoreCompany(@PathVariable Integer companyId) {
        MessageResponse response = companyService.restoreCompany(companyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DELETE_COMPANY')or hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("/delete/{companyId}")
    public ResponseEntity<MessageResponse> deleteCompany(@PathVariable Integer companyId) {
        MessageResponse response = companyService.deleteCompany(companyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}