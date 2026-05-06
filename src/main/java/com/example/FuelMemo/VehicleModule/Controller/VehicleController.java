package com.example.FuelMemo.VehicleModule.Controller;

import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import com.example.FuelMemo.VehicleModule.Dto.CompanyVehicleResponseDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleCreateDTO;
import com.example.FuelMemo.VehicleModule.Dto.VehicleSummaryDto;
import com.example.FuelMemo.VehicleModule.Dto.VehicleUpdateDTO;
import com.example.FuelMemo.VehicleModule.Service.Impl.VehicleSummaryService;
import com.example.FuelMemo.VehicleModule.Service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleSummaryService vehicleSummaryService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> createVehicle(@RequestBody VehicleCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleService.createVehicle(dto));
    }

    @PutMapping("/update/{vehicleId}")
    @PreAuthorize("hasAuthority('UPDATE_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> updateVehicle(
            @PathVariable Integer vehicleId,
            @RequestBody VehicleUpdateDTO dto) {
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicleId, dto));
    }

    @PutMapping("/softDelete/{vehicleId}")
    @PreAuthorize("hasAuthority('DELETE_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> deleteVehicle(
            @PathVariable Integer vehicleId,
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                vehicleService.deleteVehicle(vehicleId, companyId)
        );
    }

    @PutMapping("/restore/{vehicleId}")
    @PreAuthorize("hasAuthority('RESTORE_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> restoreVehicle(
            @PathVariable Integer vehicleId,
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(
                vehicleService.restoreVehicle(vehicleId, companyId)
        );
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAuthority('VIEW_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    public DataResponse getById(
            @PathVariable Integer vehicleId,
            @RequestParam Integer companyId) {

        return vehicleService.getVehicleById(vehicleId, companyId);
    }

    @PreAuthorize("hasAuthority('VIEW_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping
    public HttpResponse getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "vehicleId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isDeleted) {

        return vehicleService.getAllVehicles(
                keyword, pageNumber, pageSize,
                sortBy, sortDirection, isDeleted
        );
    }

    @PreAuthorize("hasAuthority('VIEW_VEHICLE')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/company/{companyId}")
    public HttpResponse<CompanyVehicleResponseDTO> getByCompany(
            @PathVariable Integer companyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "vehicleId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isDeleted) {

        return vehicleService.getVehiclesByCompanyId(
                companyId, keyword, pageNumber, pageSize,
                sortBy, sortDirection, isDeleted
        );
    }

    @GetMapping("/summary")
    @PreAuthorize("@securityService.canAccessCompany(authentication, #companyId)")
    public ResponseEntity<List<VehicleSummaryDto>> getVehicleSummary(
            Authentication authentication,
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        List<VehicleSummaryDto> summary = vehicleSummaryService.getVehicleSummary(companyId, month, year);

        // Filter DTO fields based on role
        summary = summary.stream().map(v -> {
            if (!authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"))) {
                // Hide ownerName for non-admin users
                v.setOwnerName(null);
            }
            return v;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(summary);
    }
}