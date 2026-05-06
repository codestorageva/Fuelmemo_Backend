package com.example.FuelMemo.MasterData.Controller;


import com.example.FuelMemo.MasterData.Dto.UpdateDto.VillageUpdateDto;
import com.example.FuelMemo.MasterData.Dto.VillageDto;
import com.example.FuelMemo.MasterData.Service.VillageService;
import com.example.FuelMemo.Shared.Response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/database/village")
public class VillageController {

    private final VillageService villageService;

    @Autowired
    public VillageController(VillageService villageService) {
        this.villageService = villageService;
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('VILLAGE_WRITE')")
    @PostMapping
    public ResponseEntity<MessageResponse> addVillage(@Valid @RequestBody VillageDto villageDto, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.addVillage(villageDto, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("{villageId}")
    public ResponseEntity<DataResponse> getVillageById(@PathVariable int villageId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.getVillageById(villageId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('VILLAGE_READ')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAlLVillages(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
            @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
            @RequestParam(value = "status", defaultValue = "true", required = false) String status,
            @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.getAllVillages(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }


    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('VILLAGE_UPDATE')")
    @PutMapping("{villageId}")
    public ResponseEntity<MessageResponse> updateVillage(@Valid @RequestBody VillageUpdateDto villageDto, @PathVariable int villageId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.updateVillage(villageDto, villageId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{villageId}")
//    public ResponseEntity<MessageResponse> deleteVillageById(@PathVariable int villageId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(villageService.deleteVillageById(villageId, headers), HttpStatus.OK);
//    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('VILLAGE_DELETE')")
    @PutMapping("softDelete/{villageId}")
    public ResponseEntity<MessageResponse> softDeleteVillageById(@PathVariable int villageId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.softDeleteVillageById(villageId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('VILLAGE_RESTORE')")
    @PutMapping("restore/{villageId}")
    public ResponseEntity<MessageResponse> restoreVillageById(@PathVariable int villageId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.restoreVillageById(villageId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllVillagesBySubDistrictAndStatusAndIsDeleted(@RequestParam Integer subDistrictId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.getAllVillagesByStatusAndIsDeleted(subDistrictId, headers), HttpStatus.OK);

    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('SUB_DISTRICT_IMPORT')")
    @PostMapping("/UploadCsv")
    public ResponseEntity<MessageListResponse> uploadVillageCSV(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.uploadVillageCSV(file, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('VILLAGE_EXPORT')")
    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedVillageData(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(villageService.exportedVillageData(headers), HttpStatus.OK);
    }

    @GetMapping("/totalVillage")
    public ResponseEntity<LongResponse> getTotalVillage() {
        return new ResponseEntity<>(villageService.getTotalVillage(), HttpStatus.OK);
    }
}
