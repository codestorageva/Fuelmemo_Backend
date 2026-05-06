package com.example.FuelMemo.FuelStationModule.Controller;

import com.example.FuelMemo.FuelStationModule.Dto.FuelStationDto;
import com.example.FuelMemo.FuelStationModule.Dto.Request.FuelStationUpdateDto;
import com.example.FuelMemo.FuelStationModule.Service.FuelStationService;
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
@RequestMapping("/api/fuel-station")
@RequiredArgsConstructor
public class FuelStationController {

    private final FuelStationService fuelStationService;

    @PreAuthorize("hasAuthority('CREATE_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/insert")
    public ResponseEntity<MessageResponse> createFuelStation(@Valid @RequestBody FuelStationDto dto) {
        MessageResponse response = fuelStationService.createFuelStation(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('VIEW_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/select/{fuelStationId}")
    public ResponseEntity<DataResponse> getFuelStationById(@PathVariable Integer fuelStationId) {
        DataResponse response = fuelStationService.getFuelStationById(fuelStationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('VIEW_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/companies/{companyId}/fuel-stations")
    public ResponseEntity<DataResponse> getFuelStationsByCompanyId(@PathVariable Integer companyId) {
        return new ResponseEntity<>(fuelStationService.getFuelStationsByCompanyId(companyId), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('VIEW_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/deleted")
    public ResponseEntity<DataResponse> getAllDeletedFuelStations() {
        DataResponse response = fuelStationService.getAllDeletedFuelStations();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('VIEW_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/selectAll")
    public ResponseEntity<HttpResponse> getAllFuelStations(@RequestParam(required = false, defaultValue = "") String keyword,
                                                           @RequestParam(defaultValue = "0") Integer pageNumber,
                                                           @RequestParam(defaultValue = "2147483647") Integer pageSize,
                                                           @RequestParam(defaultValue = "fuelStationName") String sortBy,
                                                           @RequestParam(defaultValue = "asc") String sortDir) {
        HttpResponse response = fuelStationService.getAllFuelStations(keyword, pageNumber, pageSize, sortBy, sortDir);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('UPDATE_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/update/{fuelStationId}")
    public ResponseEntity<MessageResponse> updateFuelStation(@PathVariable Integer fuelStationId, @RequestBody FuelStationUpdateDto fuelStationDto) {
        MessageResponse response = fuelStationService.updateFuelStation(fuelStationId, fuelStationDto);
        return ResponseEntity.status(response.getSuccessCode()).body(response);
    }

    @PreAuthorize("hasAuthority('DELETE_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/softDelete/{fuelStationId}")
    public ResponseEntity<MessageResponse> softDeleteFuelStation(@PathVariable Integer fuelStationId) {
        MessageResponse response = fuelStationService.softDeleteFuelStation(fuelStationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('RESTORE_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/restore/{fuelStationId}")
    public ResponseEntity<MessageResponse> restoreFuelStation(@PathVariable Integer fuelStationId) {
        MessageResponse response = fuelStationService.restoreFuelStation(fuelStationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('DELETE_FUEL_STATION')or hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("/delete/{fuelStationId}")
    public ResponseEntity<MessageResponse> deleteFuelStation(@PathVariable Integer fuelStationId) {
        MessageResponse response = fuelStationService.deleteFuelStation(fuelStationId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}