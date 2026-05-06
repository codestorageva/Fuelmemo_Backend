package com.example.FuelMemo.MasterData.Controller;


import com.example.FuelMemo.MasterData.Dto.CountryDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.CountryUpdateDto;
import com.example.FuelMemo.MasterData.Service.CountryService;
import com.example.FuelMemo.Shared.Response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/database/country")
public class CountryController {

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------

    private final CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_WRITE')")
    @PostMapping
    public ResponseEntity<MessageResponse> addCountry(@Valid @RequestBody CountryDto country, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.addCountry(country, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_READ')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllCountries(@RequestParam(value = "keyword", required = false) String keyword,
                                                        @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                        @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
                                                        @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
                                                        @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
                                                        @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
                                                        @RequestParam(value = "status", defaultValue = "true", required = false) String status,
                                                        @RequestHeader Map<String, String> headers) {

        return new ResponseEntity<>(countryService.getAllCountries(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("{countryId}")
    public ResponseEntity<DataResponse> getCountryById(@PathVariable int countryId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.getCountryById(countryId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_UPDATE')")
    @PutMapping("{countryId}")
//    public ResponseEntity<MessageResponse> updateCountry(@Valid @RequestBody CountryUpdateDto country, @PathVariable int countryId, @AuthenticationPrincipal User loggedInUser) {
    public ResponseEntity<MessageResponse> updateCountry(@Valid @RequestBody CountryUpdateDto country, @PathVariable int countryId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.updateCountry(country, countryId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{countryId}")
//    public ResponseEntity<MessageResponse> deleteCountryById(@PathVariable int countryId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(countryService.deleteCountryById(countryId, headers), HttpStatus.OK);
//    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_DELETE')")
    @PutMapping("softDelete/{countryId}")
    public ResponseEntity<MessageResponse> softDeleteById(@PathVariable int countryId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.softDeleteCountryById(countryId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_RESTORE')")
    @PutMapping("restore/{countryId}")
    public ResponseEntity<MessageResponse> restoreCountryById(@PathVariable int countryId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.restoreCountryById(countryId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_IMPORT')")
    @PostMapping("/UploadCsv")
//    public ResponseEntity<MessageResponse> uploadCountryCSV(@RequestParam(name = "file", required = false) MultipartFile file, @AuthenticationPrincipal User loggedInUser) {
    public ResponseEntity<MessageResponse> uploadCountryCSV(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.uploadCountryCSV(file, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('COUNTRY_EXPORT')")
    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedCountryData(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.exportedCountryData(headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllCountriesByActiveAndNonSoftDeleted(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(countryService.getAllCountriesByStatusAndIsDeleted(headers), HttpStatus.OK);

    }

    @GetMapping("/totalCountry")
    public ResponseEntity<LongResponse> getTotalCountry() {
        return new ResponseEntity<>(countryService.getTotalCountry(), HttpStatus.OK);
    }

}
