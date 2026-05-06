package com.example.FuelMemo.SiteModule.Entity;


import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
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
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "site")
public class Site {

    // ================= PRIMARY KEY =================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Integer siteId;

    // ================= SITE DETAILS =================

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "site_code")
    private String siteCode;

    @Column(name = "site_owner_name")
    private String siteOwnerName;

    @Column(length = 200)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private District district;

    @Column(length = 10)
    private String pincode;

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

    // ================= RELATION WITH FUEL MEMO =================

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
    private List<FuelMemo> fuelMemos;

    // ================= SOFT DELETE =================

    public void softDelete() {
        this.deleted = true;
        this.active = false;
    }
}
