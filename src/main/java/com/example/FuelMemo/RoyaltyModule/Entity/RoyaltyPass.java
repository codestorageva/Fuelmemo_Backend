package com.example.FuelMemo.RoyaltyModule.Entity;

import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "royalty_pass")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoyaltyPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer royaltyId;

    @Column(name = "royalty_number", nullable = false)
    private String royaltyNumber;

    @Column(nullable = false)
    private Boolean deleted = false;


    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PassStatus status = PassStatus.AVAILABLE;

    // ================= AUDIT =================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    // One-to-One with FuelMemo
    @OneToOne(mappedBy = "royaltyPass")
    private FuelMemo fuelMemo;
}

