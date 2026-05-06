package com.example.FuelMemo.CompanyModule.Dto;

import com.example.FuelMemo.AuthModule.Dto.RolesDTO;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {

    // ================= PRIMARY KEY =================
    private Integer companyId;

    // ================= BASIC INFO =================
    private String companyName;
    private String ownerName;
    private String logo;
    private String email;
    private String mobileNo;
    private String alternateMobileNumber;

//    private String passWord;
    private String pwd;

    private String cinNumber;
    private String gstNumber;
    private String panNumber;
    private String tanNumber;
    private String industry;

//    private LocalDate fromDate;
//    private LocalDate toDate;

    // ================= ADDRESS =================
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;

    private Integer stateId;
    private String stateName;      // helpful for UI

    private Integer districtId;
    private String cityName;   // helpful for UI

    private String pinCode;

    // ================= STATUS =================
    private Boolean active;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;

    // ================= EXTRA =================
    private String serviceDescription;

    // ================= AUDIT =================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RolesDTO> roles;
}