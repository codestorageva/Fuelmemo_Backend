package com.example.FuelMemo.AuthModule.Repository;

import com.example.FuelMemo.AuthModule.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Role findByRoleName(String roleName);
    Optional<Role> findByRoleNameIgnoreCaseAndCompanyCompanyId(String roleName, Integer companyId);

    boolean existsByRoleName(String roleName);

    boolean existsByRoleNameIgnoreCase(String roleName);

    Role findByRoleNameIgnoreCase(String roleName);

    List<Role> findByIsDeletedFalseAndActiveTrue();

    boolean existsByRoleNameIgnoreCaseAndCompanyCompanyId(String newRoleName, Integer companyId);

    List<Role> findByCompanyCompanyIdAndIsDeletedFalseAndActiveTrue(Integer companyId);

    boolean existsByCompanyCompanyId(Integer companyId);
    Role findByRoleNameAndCompany_CompanyId(String roleName, Integer companyId);

    List<Role> findByCompanyCompanyIdAndIsDeletedTrue(Integer companyId);
    Optional<Role> findByRoleNameIgnoreCaseAndCompanyIsNull(String roleName);

    boolean existsByRoleNameIgnoreCaseAndCompanyIsNull(String roleName);


}