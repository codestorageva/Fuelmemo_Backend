    package com.example.FuelMemo.AuthModule.Entity;

    import com.example.FuelMemo.CompanyModule.Entity.Company;
    import jakarta.persistence.*;
    import lombok.*;

    import java.time.LocalDateTime;
    import java.util.HashSet;
    import java.util.Set;

    @Entity
    @Table(name = "roles",
            uniqueConstraints = {
                    @UniqueConstraint(columnNames = {"roleName", "company_id"})
            })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Role {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer roleId;

        @Column(nullable = false, length = 50)
        private String roleName;

        @Column(length = 200)
        private String description;

        @Column(nullable = false)
        private Boolean active;

        @Column(nullable = false)
        private Boolean systemDefined;

        @Column(name = "is_deleted")
        private Boolean isDeleted;

        @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH})
        @JoinTable(
                name = "role_permissions",
                joinColumns = @JoinColumn(name = "role_id"),
                inverseJoinColumns = @JoinColumn(name = "permission_id")
        )
        private Set<Permission> permissions = new HashSet<>();

//        @ManyToMany(fetch = FetchType.EAGER)
//        @JoinTable(
//                name = "company_roles",
//                joinColumns = @JoinColumn(name = "role_id"),
//                inverseJoinColumns = @JoinColumn(name = "company_id")
//        )
//        private Set<Company> companies = new HashSet<>();

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "company_id")
        private Company company;

        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(nullable = false)
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.deletedAt = LocalDateTime.now();
        }

        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }
    }
