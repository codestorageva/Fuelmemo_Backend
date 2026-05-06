    package com.example.FuelMemo.CompanyModule.Entity;

    import com.example.FuelMemo.AuthModule.Entity.Role;
    import com.example.FuelMemo.AuthModule.Entity.UserCompany;
    import com.example.FuelMemo.DriverModule.Entity.Driver;
    import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
    import com.example.FuelMemo.FuelStationModule.Entity.FuelStation;
    import com.example.FuelMemo.MasterData.Entity.District;
    import com.example.FuelMemo.MasterData.Entity.State;
    import com.example.FuelMemo.SiteModule.Entity.Site;
    import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
    import jakarta.persistence.*;
    import lombok.*;
    import org.hibernate.annotations.CreationTimestamp;
    import org.hibernate.annotations.UpdateTimestamp;

    import java.time.LocalDateTime;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;

    @Entity
    @Table(name = "company")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Company {

        // ================= PRIMARY KEY =================

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer companyId;

        // ================= BASIC INFO =================

        @Column(nullable = false)
        private String companyName;

        private String ownerName;
        private String logo;
        private String passWord;
        private String pwd;
        private String email;


        private String mobileNo;
        private String alternateMobileNumber;

    //    private String displayName;
        private String cinNumber;
        private String gstNumber;
        private String panNumber;
        private String tanNumber;
        private String industry;




//        private LocalDate fromDate;
//        private LocalDate toDate;






        // ================= ADDRESS =================

        private String addressLine1;
        private String addressLine2;
        private String addressLine3;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "state_id")
        private State state;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "city_id")
        private District district;

        private String pinCode;

        // ================= STATUS =================

        @Column(name = "is_deleted")
        private Boolean isDeleted = false;

        @Column(name = "is_active")
        private Boolean active = true;

        private LocalDateTime deletedAt;

        // ================= EXTRA FIELDS =================
        @Column(length = 500)
        private String serviceDescription;

        // ================= AUDIT =================

        @CreationTimestamp
        @Column(updatable = false)
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;


        // ================= USER RELATION =================

        @OneToMany(mappedBy = "company", orphanRemoval = true)
        private Set<UserCompany> userCompanies = new HashSet<>();
        // ================= CHILD RELATIONS =================

        @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
        private Set<Role> roles = new HashSet<>();

        @OneToMany(mappedBy = "company")
        private Set<Vehicle> vehicles;

        @ManyToMany(mappedBy = "companies", fetch = FetchType.LAZY)
        private Set<Driver> drivers = new HashSet<>();

        @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
        private List<Site> sites;

        @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
        private List<FuelStation> fuelStations;

        @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
        private List<FuelMemo> fuelMemos;



        // ================= SOFT DELETE =================

        public void softDelete() {
            this.isDeleted = true;
            this.active = false;
            this.deletedAt = LocalDateTime.now();
        }


    }
