package com.example.FuelMemo.FuelMemoModule.Controller;

import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoCreateResponse;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoRequestDto;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoResponseDto;
import com.example.FuelMemo.FuelMemoModule.Dto.FuelMemoUpdateRequestDto;
import com.example.FuelMemo.FuelMemoModule.Service.FuelMemoService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fuel-memos")
public class FuelMemoController {

    private final FuelMemoService fuelMemoService;

    public FuelMemoController(FuelMemoService fuelMemoService) {
        this.fuelMemoService = fuelMemoService;
    }
    @PreAuthorize("hasAuthority('CREATE_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/create")
    public ResponseEntity<FuelMemoCreateResponse> createFuelMemo(@RequestBody FuelMemoRequestDto request) {
        FuelMemoCreateResponse response = fuelMemoService.createFuelMemo(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PreAuthorize("hasAuthority('UPDATE_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateFuelMemo(
            @PathVariable Integer id,
            @Valid @RequestBody FuelMemoUpdateRequestDto request) {

        MessageResponse response = fuelMemoService.updateFuelMemo(id, request);
        return ResponseEntity.status(response.getSuccessCode()).body(response);
    }

    @PreAuthorize("hasAuthority('DELETE_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
        @PutMapping("soft-delete/{id}")
    public ResponseEntity<MessageResponse> deleteFuelMemo(
            @PathVariable Integer id) {

        MessageResponse response = fuelMemoService.deleteFuelMemo(id);
        return new ResponseEntity<>(response, response.getSuccessCode());
    }
    // ================= RESTORE =================
    @PreAuthorize("hasAuthority('RESTORE_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/restore/{id}")
    public ResponseEntity<MessageResponse> restoreFuelMemo(
            @PathVariable Integer id) {

        MessageResponse response = fuelMemoService.restoreFuelMemo(id);
        return new ResponseEntity<>(response, response.getSuccessCode());
    }
    @PreAuthorize("hasAuthority('VIEW_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/all")
    public HttpResponse<?> getAllFuelMemos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String isDeleted,
            @RequestParam(required = false) Integer month,
            @RequestParam(required=false) Integer year,
            @RequestParam(required = false) String monthStatus

    ) {
        return fuelMemoService.getAllFuelMemos(
                keyword,
                pageNumber,
                pageSize,
                sortBy,
                sortDirection,
                isDeleted,
                month,year,monthStatus
        );
    }

    @PreAuthorize("hasAuthority('VIEW_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/{id}")
    public DataResponse getFuelMemoById(@PathVariable Integer id) {
        return fuelMemoService.getFuelMemoById(id);
    }


    @PreAuthorize("hasAuthority('ADJUSTMENT_CREATE_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/adjustment")
    public MessageResponse createAdjustmentEntry(
            @RequestBody FuelMemoRequestDto request) {
        return fuelMemoService.createAdjustmentEntry(request);
    }

    @PreAuthorize("hasAuthority('VIEW_FUEL_MEMO')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/company/{companyId}")
    public HttpResponse<?> getCompanyFuelMemos(
            @PathVariable Integer companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String isDeleted,
            @RequestParam(required = false) Integer month,
            @RequestParam(required=false) Integer year,
            @RequestParam(required = false) String monthStatus
    ) {
        return fuelMemoService.getAllFuelMemosByCompany(
                companyId, keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, month,year,monthStatus
        );
    }


}