package com.example.FuelMemo.FuelStationModule.Entity;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Entity.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fuel_station")
public class FuelStation {

    // ================= PRIMARY KEY =================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fuel_station_id")
    private Integer fuelStationId;

    // ================= BASIC DETAILS =================

    @Column(name = "fuel_station_name", nullable = false, length = 150)
    private String fuelStationName;

//    @Column(name = "state", nullable = false, length = 100)
//    private String state;
//
//    @Column(name = "district", nullable = false, length = 100)
//    private String district;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private District district;

    @Column(name = "sub_district", nullable = false, length = 100)
    private String subDistrict;

    @Column(name = "village", nullable = false, length = 100)
    private String village;

    @Column(name = "owner_name", length = 100)
    private String ownerName;

    @Column(name = "owner_mobile_number", length = 15)
    private String ownerMobileNumber;

    // ================= STATUS =================

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    // ================= AUDIT =================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= RELATION WITH COMPANY =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // ================= SOFT DELETE =================

    public void softDelete() {
        this.deleted = true;
        this.active = false;
    }
}

