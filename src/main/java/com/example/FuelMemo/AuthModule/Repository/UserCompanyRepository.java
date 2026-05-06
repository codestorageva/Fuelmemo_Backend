package com.example.FuelMemo.AuthModule.Repository;

import com.example.FuelMemo.AuthModule.Entity.Role;
import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCompanyRepository
        extends JpaRepository<UserCompany, Integer> {


    void deleteByUser_UserIdAndCompany_CompanyId(Integer userId, Integer companyId);

    List<UserCompany> findByUserUserId(Integer userId);
    List<UserCompany> findByCompanyCompanyIdAndRoleIsDeletedFalseAndRoleActiveTrue(Integer companyId);

    void deleteByUser_UserId(Integer userId);

    boolean existsByUserAndRole(User superAdminUser, Role superAdminRole);

    boolean existsByCompanyCompanyId(Integer companyId);


    void deleteByUserUserIdAndCompanyCompanyId(Integer userId, Integer companyId);

    void deleteByUserUserId(Integer userId);

    boolean existsByUserAndCompany(User user, Company company);

    boolean existsByUserAndRoleAndCompany(User superAdminUser, Role superAdminRole, Company defaultCompany);

    @Query("""
    SELECT uc FROM UserCompany uc
    JOIN FETCH uc.role r
    JOIN FETCH uc.company c
    WHERE uc.user = :user
""")
    List<UserCompany> findByUserWithRoleAndCompany(@Param("user") User user);

    @Query("""
        SELECT COUNT(DISTINCT uc.user.userId)
        FROM UserCompany uc
        WHERE uc.active = true
          AND uc.user.isDeleted = false
          AND uc.company.isDeleted = false
    """)
    Long countDistinctActiveUsersAcrossCompanies();

    @Query("""
        SELECT COUNT(DISTINCT uc.user.userId)
        FROM UserCompany uc
        WHERE uc.active = true
          AND uc.user.isDeleted = false
          AND uc.company.companyId = :companyId
          AND uc.company.isDeleted = false
    """)
    Long countDistinctActiveUsersByCompanyId(@Param("companyId") Integer companyId);
}

