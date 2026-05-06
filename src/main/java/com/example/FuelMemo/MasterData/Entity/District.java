package com.example.FuelMemo.MasterData.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "db_districts", indexes = {
        @Index(name = "district_index", columnList = "district_id"),
        @Index(name = "idx_city_name", columnList = "city_name"),
        @Index(name = "idx_api_city_name", columnList = "api_city_name")
})
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Integer districtId;

    // 🔹 ORIGINAL CITY / DISTRICT NAME (as per master data)
    @Column(name = "city_name", nullable = false)
    private String cityName;

    // 🔥 NEW FIELD (VERY IMPORTANT)
    // 👉 API ma use thase (mapped city)
    @Column(name = "api_city_name")
    private String apiCityName;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "status")
    private Boolean status = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 🔗 RELATIONS

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubDistrict> subDistricts = new ArrayList<>();

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Village> villages = new ArrayList<>();

    // 🔥 AUTO TIMESTAMP

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = true;
        if (this.isDeleted == null) this.isDeleted = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}