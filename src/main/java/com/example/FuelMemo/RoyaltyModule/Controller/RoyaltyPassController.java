package com.example.FuelMemo.RoyaltyModule.Controller;

import com.example.FuelMemo.RoyaltyModule.Dto.Request.RoyaltyPassUpdateDto;
import com.example.FuelMemo.RoyaltyModule.Dto.RoyaltyPassDto;
import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import com.example.FuelMemo.RoyaltyModule.Service.RoyaltyPassService;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.Shared.Response.HttpResponse;
import com.example.FuelMemo.Shared.Response.MessageListResponse;
import com.example.FuelMemo.Shared.Response.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/royalty-pass")
@RequiredArgsConstructor
public class RoyaltyPassController {

    private final RoyaltyPassService royaltyPassService;

    // ---------------- CREATE ROYALTY PASS ----------------
    @PreAuthorize("hasAuthority('CREATE_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @PostMapping("/insert")
    public ResponseEntity<MessageResponse> createRoyaltyPass(@Valid @RequestBody RoyaltyPassDto dto) {
        MessageResponse response = royaltyPassService.createRoyaltyPass(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ---------------- GET ROYALTY PASS BY ID ----------------
    @PreAuthorize("hasAuthority('VIEW_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/select/{royaltyId}")
    public ResponseEntity<DataResponse> getRoyaltyPassById(@PathVariable Integer royaltyId) {
        DataResponse response = royaltyPassService.getRoyaltyPassById(royaltyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ---------------- GET ALL ROYALTY PASSES BY COMPANY ----------------
    @PreAuthorize("hasAuthority('VIEW_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
//    @GetMapping("/companies/{companyId}/royalty-passes")
//    public ResponseEntity<DataResponse> getRoyaltyPassesByCompanyId(@PathVariable Integer companyId) {
//        return new ResponseEntity<>(royaltyPassService.getRoyaltyPassesByCompanyId(companyId), HttpStatus.OK);
//    }
    @GetMapping("/companies/{companyId}/royalty-passes")
    public ResponseEntity<DataResponse> getRoyaltyPassesByCompanyId(
            @PathVariable Integer companyId,

            @RequestParam(required = false) Boolean isDeleted,

            @RequestParam(required = false) String status
    ) {

        DataResponse response = royaltyPassService
                .getRoyaltyPassesByCompanyId(companyId, isDeleted, status);

        return new ResponseEntity<>(response, response.getSuccessCode());
    }

    // ---------------- GET ALL DELETED ROYALTY PASSES  ----------------
    @PreAuthorize("hasAuthority('VIEW_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/deleted")
    public ResponseEntity<MessageListResponse> getAllDeletedRoyaltyPasses() {
        MessageListResponse response = royaltyPassService.getAllDeletedRoyaltyPasses();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PreAuthorize("hasAuthority('VIEW_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/dropdown")
    public ResponseEntity<DataResponse> getRoyaltyDropdown(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer selectedRoyaltyId
    ) {
        return ResponseEntity.ok(
                royaltyPassService.getRoyaltyDropdown(companyId, selectedRoyaltyId)
        );
    }

    // ---------------- GET ALL ROYALTY PASSES (WITH SEARCH & PAGINATION) ----------------
    @PreAuthorize("hasAuthority('VIEW_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @GetMapping("/selectAll")
    public ResponseEntity<HttpResponse> getAllRoyaltyPasses(

            @RequestParam(required = false) String keyword,

            @RequestParam(defaultValue = "0") Integer pageNumber,

            @RequestParam(defaultValue = "10") Integer pageSize,

            @RequestParam(defaultValue = "royaltyNumber") String sortBy,

            @RequestParam(defaultValue = "asc") String sortDir,

            @RequestParam(required = false) Boolean isDeleted
    ) {

        HttpResponse response = royaltyPassService.getAllRoyaltyPasses(
                keyword,
                pageNumber,
                pageSize,
                sortBy,
                sortDir,
                isDeleted
        );

        return ResponseEntity.ok(response);
    }

    // ---------------- UPDATE ROYALTY PASS ----------------
    @PreAuthorize("hasAuthority('UPDATE_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/update/{royaltyId}")
    public ResponseEntity<MessageResponse> updateRoyaltyPass(@PathVariable Integer royaltyId, @RequestBody RoyaltyPassUpdateDto dto) {
        MessageResponse response = royaltyPassService.updateRoyaltyPass(royaltyId, dto);
        return ResponseEntity.status(response.getSuccessCode()).body(response);
    }

    // ---------------- SOFT DELETE ROYALTY PASS ----------------
    @PreAuthorize("hasAuthority('DELETE_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/softDelete/{royaltyId}")
    public ResponseEntity<MessageResponse> softDeleteRoyaltyPass(@PathVariable Integer royaltyId) {
        MessageResponse response = royaltyPassService.softDeleteRoyaltyPass(royaltyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ---------------- RESTORE ROYALTY PASS ----------------
    @PreAuthorize("hasAuthority('RESTORE_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @PutMapping("/restore/{royaltyId}")
    public ResponseEntity<MessageResponse> restoreRoyaltyPass(@PathVariable Integer royaltyId) {
        MessageResponse response = royaltyPassService.restoreRoyaltyPass(royaltyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ---------------- DELETE ROYALTY PASS ----------------
    @PreAuthorize("hasAuthority('DELETE_ROYALTY_PASS')or hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping("/delete/{royaltyId}")
    public ResponseEntity<MessageResponse> deleteRoyaltyPass(@PathVariable Integer royaltyId) {
        MessageResponse response = royaltyPassService.deleteRoyaltyPass(royaltyId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}