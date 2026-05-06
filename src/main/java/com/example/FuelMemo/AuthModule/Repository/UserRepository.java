package com.example.FuelMemo.AuthModule.Repository;


import com.example.FuelMemo.AuthModule.Entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Integer> {


    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions",
            "userCompanies",
            "userCompanies.role",
            "userCompanies.role.permissions"
    })
    User findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {
            "roles",
            "roles.permissions",
            "userCompanies",
            "userCompanies.role",
            "userCompanies.role.permissions"
    })

    Optional<User> findByUserNameIgnoreCase(String userName);
    boolean existsByEmailIgnoreCase(String email);// login by username


    boolean existsByUserNameIgnoreCase(String userName);


    User findByEmailIgnoreCaseOrUserNameIgnoreCase(String loginInput, String loginInput1);


    User findByEmail(String email);
    List<User> findDistinctByUserCompaniesCompanyCompanyIdInAndIsDeletedTrue(Set<Integer> companyIds);



    User findByUserNameOrEmail(String userName, String email);

    User findByUserName(String userName);



    List<User> findDistinctByUserCompaniesCompanyCompanyIdInAndIsDeletedFalse(Set<Integer> companyIds);



    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    List<User> findAllDeletedUsers();

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.userCompanies uc
    LEFT JOIN FETCH uc.role r
    LEFT JOIN FETCH r.permissions
    WHERE u.userId = :id
""")
    Optional<User> findByIdWithCompanies(Integer id);

    List<User> findByIsDeletedFalse();

    boolean existsByJwtTokenIgnoreCase(String token);
}
