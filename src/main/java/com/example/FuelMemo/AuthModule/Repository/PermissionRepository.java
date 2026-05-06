package com.example.FuelMemo.AuthModule.Repository;

import com.example.FuelMemo.AuthModule.Entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    List<Permission> findByPermissionNameIn(List<String> viewCompany);

    boolean existsByPermissionName(String name);
    List<Permission> findByPermissionGroupIgnoreCase(String permissionGroup);


    Permission findByPermissionName(String permissionName);

    List<Permission> findByPermissionNameContainingIgnoreCaseAndIsDeletedAndActive(
            String keyword,
            Boolean deleted,
            Boolean active
    );

    List<Permission> findByIsDeletedAndActive(
            Boolean deleted,
            Boolean active
    );
}
