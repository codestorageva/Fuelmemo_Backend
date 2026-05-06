package com.example.FuelMemo.MasterData.Controller;


import com.example.FuelMemo.MasterData.Dto.StateDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.StateUpdateDto;
import com.example.FuelMemo.MasterData.Repository.CountryRepository;
import com.example.FuelMemo.MasterData.Repository.StateRepository;
import com.example.FuelMemo.MasterData.Service.StateService;
import com.example.FuelMemo.Shared.Response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/database/state")
@CrossOrigin(origins = "*")
public class StateController {

    //---------------------------------------------------CONSTRUCTOR INJECTION------------------------------------------
    private final StateService stateService;
    private final StateRepository stateRepository;
    private final CountryRepository countryRepository;

    @Autowired
    public StateController(StateService stateService, StateRepository stateRepository, CountryRepository countryRepository) {
        this.stateService = stateService;
        this.stateRepository = stateRepository;
        this.countryRepository = countryRepository;
    }

    //---------------------------------------------------URL ENDPOINTS--------------------------------------------------

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_WRITE')")
    @PostMapping
    public ResponseEntity<MessageResponse> addState(@Valid @RequestBody StateDto dto, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.addState(dto, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("{stateId}")
    public ResponseEntity<DataResponse> getStateById(@PathVariable int stateId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.getStateById(stateId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_READ')")
    @GetMapping
    public ResponseEntity<HttpResponse> getAllStates
    (@RequestParam(value = "keyword", required = false) String keyword,
     @RequestParam(value = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
     @RequestParam(value = "pageSize", defaultValue = "2147483647", required = false) Integer pageSize,
     @RequestParam(value = "sortBy", defaultValue = "updatedAt", required = false) String sortBy,
     @RequestParam(value = "sortDirection", defaultValue = "asc", required = false) String sortDirection,
     @RequestParam(value = "isDeleted", defaultValue = "false", required = false) String isDeleted,
     @RequestParam(value = "status", defaultValue = "true", required = false) String status,
     @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.getAllStates(keyword, pageNumber, pageSize, sortBy, sortDirection, isDeleted, status, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_UPDATE')")
    @PutMapping("{stateId}")
    public ResponseEntity<MessageResponse> updateState(@Valid @RequestBody StateUpdateDto stateDto, @PathVariable int stateId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.updateState(stateDto, stateId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
//    @DeleteMapping("hardDelete/{stateId}")
//    public ResponseEntity<MessageResponse> deleteStateById(@PathVariable int stateId, @RequestHeader Map<String, String> headers) {
//        return new ResponseEntity<>(stateService.deleteStateById(stateId, headers), HttpStatus.OK);
//    }


    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_DELETE')")
    @PutMapping("softDelete/{stateId}")
    public ResponseEntity<MessageResponse> softDeleteStateById(@PathVariable int stateId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.softDeleteStateById(stateId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_RESTORE')")
    @PutMapping("restore/{stateId}")
    public ResponseEntity<MessageResponse> restoreStateById(@PathVariable int stateId, @RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.restoreStateById(stateId, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_IMPORT')")
    @PostMapping("/UploadCsv")
//    public ResponseEntity<MessageListResponse> uploadStateCSV(@RequestParam(name = "file", required = false) MultipartFile file, @AuthenticationPrincipal User loggedInUser) throws IOException {
    public ResponseEntity<MessageListResponse> uploadStateCSV(@RequestParam(name = "file", required = false) MultipartFile file, @RequestHeader Map<String, String> headers) throws IOException {
        return new ResponseEntity<>(stateService.uploadStateCSV(file, headers), HttpStatus.OK);
    }

    //    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<ListResponse> getAllStatesByCountryIDAndActiveAndNonSoftDeleted(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.getAllStatesByStatusAndIsDeleted(headers), HttpStatus.OK);

    }

    //    @PreAuthorize("hasRole('ROLE_SUPERADMIN') or hasAuthority('STATE_EXPORT')")
    @GetMapping("/exportData")
    public ResponseEntity<ListResponse> exportedStateData(@RequestHeader Map<String, String> headers) {
        return new ResponseEntity<>(stateService.exportedStateData(headers), HttpStatus.OK);
    }

    @GetMapping("/totalState")
    public ResponseEntity<LongResponse> getTotalState() {
        return new ResponseEntity<>(stateService.getTotalState(), HttpStatus.OK);
    }
}
