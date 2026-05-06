package com.example.FuelMemo.AuthModule.Repository;

import com.example.FuelMemo.AuthModule.Entity.RefreshToken;
import com.example.FuelMemo.AuthModule.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    RefreshToken findByToken(String token);

    void deleteByUser(User user);
}
