package com.example.FuelMemo.MasterData.Controller;


import com.example.FuelMemo.MasterData.Dto.DistrictDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.DistrictUpdateDto;
import com.example.FuelMemo.MasterData.Service.DistrictService;
import com.example.FuelMemo.Shared.Response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/database/district")
public class DistrictController {

    private final DistrictService districtService;

    @Autowired
    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_WRITE')")
    @PostMapping
    public ResponseEntity<MessageResponse> addDistrict(@Valid @RequestBody DistrictDto districtDto, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.addDistrict(districtDto, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_READ')")
    @GetMapping("{districtId}")
    public ResponseEntity<DataResponse> getDistrictById(@PathVariable int districtId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.getDistrictById(districtId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_READ')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllDistricts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "2147483647") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "updatedAt") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection,
            @RequestParam(value = "isDeleted", defaultValue = "false") String isDeleted,
            @RequestParam(value = "status", defaultValue = "true") String status,
            @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok(
                districtService.getAllDistricts(
                        keyword,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection,
                        isDeleted,
                        status,
                        authorization
                )
        );
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_UPDATE')")
    @PutMapping("{districtId}")
    public ResponseEntity<MessageResponse> updateDistrict(@Valid @RequestBody DistrictUpdateDto districtDto, @PathVariable int districtId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.updateDistrict(districtDto, districtId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_DELETE')")
    @PutMapping("softDelete/{districtId}")
    public ResponseEntity<MessageResponse> softDeleteDistrictById(@PathVariable int districtId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.softDeleteDistrictById(districtId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{districtId}")
//    public ResponseEntity<MessageResponse> hardDeleteDistrictById(@PathVariable int districtId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(districtService.deleteDistrictById(districtId, headers), HttpStatus.OK);
//    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_RESTORE')")
    @PutMapping("restore/{districtId}")
    public ResponseEntity<MessageResponse> restoreDistrictById(@PathVariable int districtId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.restoreDistrictById(districtId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_IMPORT')")
    @PostMapping("/UploadCsv")
    public ResponseEntity<MessageListResponse> uploadDistrictCSV(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.uploadDistrictCSV(file, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllDistrictsByStateIdAndStatusAndIsDeleted(@RequestParam Integer StateId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.getAllDistrictsByStatusAndIsDeleted(StateId, headers), HttpStatus.OK);

    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('DISTRICT_EXPORT')")
    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedDistrictData(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(districtService.exportedDistrictData(headers), HttpStatus.OK);
    }

    @GetMapping("/totalDistrict")
    public ResponseEntity<LongResponse> getTotalDistrict() {
        return new ResponseEntity<>(districtService.getTotalDistrict(), HttpStatus.OK);
    }

}
