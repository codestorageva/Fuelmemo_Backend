        package com.example.FuelMemo.FuelMemoModule.Entity;

        import com.example.FuelMemo.AuthModule.Entity.User;
        import com.example.FuelMemo.CompanyModule.Entity.Company;
        import com.example.FuelMemo.DriverModule.Entity.Driver;
        import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
        import com.example.FuelMemo.RoyaltyModule.Entity.RoyaltyPass;
        import com.example.FuelMemo.SiteModule.Entity.Site;
        import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
        import jakarta.persistence.*;
        import lombok.*;
        import org.hibernate.annotations.CreationTimestamp;
        import org.hibernate.annotations.UpdateTimestamp;

        import java.math.BigDecimal;
        import java.time.LocalDate;
        import java.time.LocalDateTime;

        @Entity
        @Table(name = "fuel_memo")
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public class FuelMemo {

            // ================= PRIMARY KEY =================

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            @Column(name = "fuel_memo_id")
            private Integer fuelMemoId;

            // ================= BASIC INFO =================

            @Column(name = "sr_no", nullable = false)
            private Integer srNo;

            @Column(nullable = false)
            private LocalDate date;

            @OneToOne
            @JoinColumn(name = "royalty_pass_id")
            private RoyaltyPass royaltyPass;

            @Column(nullable = false, precision = 10, scale = 2)
            private BigDecimal quantity;

            @ManyToOne
            @JoinColumn(name = "prepared_by")
            private User user;

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

            // ================= RELATIONSHIPS =================

            @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "vehicle_id", nullable = false)
            private Vehicle vehicle;

            @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "driver_id", nullable = false)
            private Driver driver;

            @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "site_id", nullable = false)
            private Site site;

            @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "party_id", nullable = false)
            private FuelStation fuelStation;

            @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "company_id", nullable = false)
            private Company company;

            @Enumerated(EnumType.STRING)
            private EntryType entryType;

            // ================= SOFT DELETE =================

            public void softDelete() {
                this.deleted = true;
                this.active = false;
            }
            @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name = "fuel_month_id")
            private FuelMonth fuelMonth;


        }

