package com.example.FuelMemo.AuthModule.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permissionId;

    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;
    // Example: CREATE_COMPANY

    @Column(name = "permissionSuperGroup", nullable = false, length = 50)
    private String permissionSuperGroup;
    // Example: ADMINISTRATION

    @Column(name = "permissionGroup", nullable = false, length = 50)
    private String permissionGroup;
    // Example: COMPANY

    @Column(name = "action_name", nullable = false, length = 50)
    private String actionName;
    // Example: CREATE

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToMany(mappedBy = "permissions")
    private List<Role> roles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deletedAt=LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}