package com.example.FuelMemo.FuelMemoModule.Entity;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "fuel_month",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"month", "year", "company_id"})
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class FuelMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ================= BASIC =================

    @Column(nullable = false)
    private Integer month;   // 1 to 12

    @Column(nullable = false)
    private Integer year;

    // ================= STATUS =================

    @Column(nullable = false)
    private Boolean closed = false;   // false = OPEN, true = CLOSED

    @Column(nullable = false)
    private Boolean deleted = false;  // soft delete support

    // ================= CLOSE INFO =================

    private String closedBy;

    private LocalDateTime closedAt;

    // ================= REOPEN INFO =================

    private String reopenedBy;

    private LocalDateTime reopenedAt;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    // ================= AUDIT =================

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ================= HELPER METHODS =================

    public void closeMonth(String username) {
        this.closed = true;
        this.closedBy = username;
        this.closedAt = LocalDateTime.now();
    }

    public void reopenMonth(String username) {
        this.closed = false;
        this.reopenedBy = username;
        this.reopenedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deleted = true;
    }
}
