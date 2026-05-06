package com.example.FuelMemo.DriverModule.Controller;

import com.example.FuelMemo.DriverModule.Dto.DriverRequestDto;
import com.example.FuelMemo.DriverModule.Dto.DriverUpdateDto;
import com.example.FuelMemo.DriverModule.Service.DriverService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")

public class DriverController {

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    private final DriverService driverService;

    // ================= CREATE DRIVER =================
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('CREATE_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> createDriver(
            @Valid @RequestBody DriverRequestDto dto) {

        MessageResponse response = driverService.createDriver(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ================= UPDATE DRIVER =================
    @PutMapping("/update/{driverId}")
    @PreAuthorize("hasAuthority('UPDATE_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> updateDriver(
            @PathVariable Integer driverId,
            @Valid @RequestBody DriverUpdateDto dto) {

        MessageResponse response = driverService.updateDriver(driverId, dto);
        return ResponseEntity.ok(response);
    }

    // ================= SOFT DELETE DRIVER =================

    @PutMapping("/softDelete/{driverId}")
    @PreAuthorize("hasAuthority('DELETE_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> deleteDriver(
            @PathVariable Integer driverId,
            @RequestParam Integer companyId) {

        MessageResponse response = driverService.deleteDriver(driverId, companyId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/restore/{driverId}")
    @PreAuthorize("hasAuthority('RESTORE_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<MessageResponse> restoreDriver(
            @PathVariable Integer driverId,
            @RequestParam Integer companyId) {

        MessageResponse response = driverService.restoreDriver(driverId, companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{driverId}")
    @PreAuthorize("hasAuthority('VIEW_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<DataResponse> getDriverById(
            @PathVariable Integer driverId,
            @RequestParam Integer companyId) {

        return ResponseEntity.ok(driverService.getDriverById(driverId, companyId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<HttpResponse> getAllDrivers(
            @RequestParam Integer companyId,  // <-- new required param
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "driverId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Boolean isDeleted
    ) {
        return ResponseEntity.ok(
                driverService.getAllDrivers(
                        companyId,
                        keyword,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection,
                        isDeleted
                )
        );
    }

    @GetMapping("/{companyId}/drivers")
    @PreAuthorize("hasAuthority('VIEW_DRIVER')or hasRole('ROLE_SUPERADMIN')")
    public ResponseEntity<HttpResponse> getDriversByCompanyId(

            @PathVariable Integer companyId,

            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0") int pageNumber,

            @RequestParam(defaultValue = "10") int pageSize,

            @RequestParam(defaultValue = "driverId") String sortBy,

            @RequestParam(defaultValue = "desc") String sortDirection,

            @RequestParam(required = false) Boolean isDeleted
    ) {

        return ResponseEntity.ok(
                driverService.getDriversByCompanyId(
                        companyId,
                        keyword,
                        pageNumber,
                        pageSize,
                        sortBy,
                        sortDirection,
                        isDeleted
                )
        );
    }


}