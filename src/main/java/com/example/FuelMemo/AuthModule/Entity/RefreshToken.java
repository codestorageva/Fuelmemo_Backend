package com.example.FuelMemo.AuthModule.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @Column
    private String deviceId;

    @ManyToOne
    private User user;

    @Column
    private Instant createdAt;
}

