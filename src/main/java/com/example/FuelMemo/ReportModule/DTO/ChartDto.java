package com.example.FuelMemo.ReportModule.DTO;

import com.example.FuelMemo.RoyaltyModule.Entity.PassStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class ChartDto {

    private String label;
    private Long value;

    // JPQL constructor (ENUM)
    public ChartDto(PassStatus status, Long value) {
        this.label = (status != null) ? status.name().toLowerCase() : "unknown";
        this.value = value;
    }

    // 🔥 THIS WAS MISSING
    public ChartDto(String label, Long value) {
        this.label = label;
        this.value = value;
    }
}