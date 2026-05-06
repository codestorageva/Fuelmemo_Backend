package com.example.FuelMemo.DriverModule.Entity;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driver")
public class Driver {

    // ================= PRIMARY KEY =================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Integer driverId;

    // ================= DRIVER DETAILS =================

    @Column(name = "driver_name", nullable = false, length = 100)
    private String driverName;

    // ================= STATUS =================

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "isDeleted", nullable = false)
    private Boolean isDeleted = false;

    // ================= AUDIT =================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= RELATION WITH COMPANY =================

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "driver_companies",
            joinColumns = @JoinColumn(name = "driver_id"),
            inverseJoinColumns = @JoinColumn(name = "company_id")
    )
    private Set<Company> companies = new HashSet<>();

    // ================= SOFT DELETE =================


    public void softDelete() {
        this.isDeleted = true;
        this.active = false;
    }
}











