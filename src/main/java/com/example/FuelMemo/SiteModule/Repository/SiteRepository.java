package com.example.FuelMemo.SiteModule.Repository;

import com.example.FuelMemo.SiteModule.Entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Integer> {

    boolean existsBySiteNameAndCompanyCompanyIdAndDeletedFalse(String siteName, Integer companyId);

    Site findBySiteNameIgnoreCaseAndCompanyCompanyId(String trim, Integer companyId);

    boolean existsBySiteNameIgnoreCaseAndCompanyCompanyIdAndDeletedFalse(String newName, Integer companyId);

    boolean existsByCompanyCompanyId(Integer companyId);

    List<Site> findByCompanyCompanyIdAndDeletedTrue(Integer companyId);
}
