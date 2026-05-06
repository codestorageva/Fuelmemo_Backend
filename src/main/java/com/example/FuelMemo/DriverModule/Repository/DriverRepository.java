    package com.example.FuelMemo.DriverModule.Repository;

    import com.example.FuelMemo.CompanyModule.Entity.Company;
    import com.example.FuelMemo.DriverModule.Entity.Driver;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.time.LocalDateTime;

    public interface DriverRepository extends JpaRepository<Driver, Integer> {

        Driver findByDriverNameIgnoreCase(String driverName);

        boolean existsByCompaniesCompanyId(Integer companyId);

        long countByCreatedAtBetween(LocalDateTime todayStart, LocalDateTime tomorrowStart);

        long countByCompaniesContaining(Company company);

        long countByCompaniesContainingAndCreatedAtBetween(
                Company company,
                LocalDateTime start,
                LocalDateTime end
        );
    }