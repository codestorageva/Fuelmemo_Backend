package com.example.FuelMemo.MasterData.Repository;


import com.example.FuelMemo.MasterData.Entity.Country;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CountryRepository extends JpaRepository<Country, Integer> {
    Country findByCountryNameIgnoreCase(String name);

    Page<Country> findAllByStatusAndIsDeleted(Boolean b, boolean isDeleted, Pageable p);

    Page<Country> findByCountryIdOrCountryNameContainingIgnoreCaseAndStatusAndIsDeleted(Integer countryId, String keyword, Boolean isStatus, Boolean isDeleted, Pageable p);

    boolean existsByCountryNameIgnoreCase(String countryName);

//    boolean findByIdAndIsDeleted(Integer countryId);

    Page<Country> findAllByIsDeleted(Boolean b, Pageable p);

    List<Country> findAllByStatusAndIsDeleted(Boolean b1, Boolean b2);

}
