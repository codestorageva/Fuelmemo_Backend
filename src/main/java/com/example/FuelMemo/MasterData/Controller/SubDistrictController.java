package com.example.FuelMemo.MasterData.Controller;


import com.example.FuelMemo.MasterData.Dto.SubDistrictDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.SubDistrictUpdateDto;
import com.example.FuelMemo.MasterData.Service.SubDistrictService;
import com.example.FuelMemo.Shared.Response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/database/subDistrict")
public class SubDistrictController {

    private final SubDistrictService subDistrictService;

    @Autowired
    public SubDistrictController(SubDistrictService subDistrictService) {
        this.subDistrictService = subDistrictService;
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_WRITE')")
    @PostMapping
    public ResponseEntity<MessageResponse> addSubDistrict(@Valid @RequestBody SubDistrictDto subDistrictDto, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.addSubDistrict(subDistrictDto, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("{subDistrictId}")
    public ResponseEntity<DataResponse> getSubDistrictById(@PathVariable int subDistrictId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.getSubDistrictById(subDistrictId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_READ')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllSubDistricts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
            @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
            @RequestParam(value = "status", defaultValue = "true", required = false) String status,
            @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.getAllSubDistricts(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_UPDATE')")
    @PutMapping("{subDistrictId}")
    public ResponseEntity<MessageResponse> updateSubDistrictId(@Valid @RequestBody SubDistrictUpdateDto subDistrictDto, @PathVariable int subDistrictId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.updateSubDistrict(subDistrictDto, subDistrictId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{subDistrictId}")
//    public ResponseEntity<MessageResponse> deleteSubDistrict(@PathVariable int subDistrictId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(subDistrictService.deleteSubDistrictById(subDistrictId, headers), HttpStatus.OK);
//    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_DELETE')")
    @PutMapping("softDelete/{subDistrictId}")
    public ResponseEntity<MessageResponse> softDeleteSubDistrictById(@PathVariable int subDistrictId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.softDeleteSubDistrictById(subDistrictId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_RESTORE')")
    @PutMapping("restore/{subDistrictId}")
    public ResponseEntity<MessageResponse> restoreSubDistrictById(@PathVariable int subDistrictId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.restoreSubDistrictById(subDistrictId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllSubDistrictsByDistrictIdAndStatusAndIsDeleted(@RequestParam Integer districtId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.getAllSubDistrictsByStatusAndIsDeleted(districtId, headers), HttpStatus.OK);

    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_IMPORT')")
    @PostMapping("/UploadCsv")
    public ResponseEntity<MessageListResponse> uploadSubDistrictCSV(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.uploadSubDistrictCSV(file, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_EXPORT')")
    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedSubDistrictData(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(subDistrictService.exportedSubDistrictData(headers), HttpStatus.OK);
    }

    @GetMapping("/totalSubDistrict")
    public ResponseEntity<LongResponse> getTotalSubDistrict() {
        return new ResponseEntity<>(subDistrictService.getTotalSubDistrict(), HttpStatus.OK);
    }
}

