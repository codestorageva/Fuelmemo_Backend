package com.example.FuelMemo.ReportModule.Service.Impl;

import com.example.FuelMemo.AuthModule.Entity.User;
import com.example.FuelMemo.CompanyModule.Entity.Company;
import com.example.FuelMemo.AuthModule.Entity.UserCompany;
import com.example.FuelMemo.DriverModule.Repository.DriverRepository;
import com.example.FuelMemo.FuelMemoModule.Entity.FuelMemo;
import com.example.FuelMemo.FuelMemoModule.Repository.FuelMemoRepository;
import com.example.FuelMemo.ReportModule.DTO.ChartDto;
import com.example.FuelMemo.ReportModule.DTO.DashboardSummaryDto;
import com.example.FuelMemo.ReportModule.DTO.FuelMemoListDto;
import com.example.FuelMemo.ReportModule.DTO.UsageGraphDto;
import com.example.FuelMemo.ReportModule.Service.DashboardService;
import com.example.FuelMemo.RoyaltyModule.Repository.RoyaltyPassRepository;
import com.example.FuelMemo.Shared.Response.DataResponse;
import com.example.FuelMemo.VehicleModule.Repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//@Service
//public class DashboardServiceImpl implements DashboardService {
//
//    private final DriverRepository driverRepository;
//
//    public DashboardServiceImpl(DriverRepository driverRepository, VehicleRepository vehicleRepository, FuelMemoRepository fuelMemoRepository, RoyaltyPassRepository royaltyPassRepository) {
//        this.driverRepository = driverRepository;
//        this.vehicleRepository = vehicleRepository;
//        this.fuelMemoRepository = fuelMemoRepository;
//        this.royaltyPassRepository = royaltyPassRepository;
//    }
//
//    private final VehicleRepository vehicleRepository;
//    private final FuelMemoRepository fuelMemoRepository;
//    private final RoyaltyPassRepository royaltyPassRepository;
//
//
//
//    // ================= GET COMPANY =================
//    public Company getCurrentUserCompany() {
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User) auth.getPrincipal();
//
////        return user.getUserCompanies().stream()
////                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
////                .filter(uc -> uc.getCompany() != null)
////                .map(uc -> uc.getCompany())
////                .findFirst()
////                .orElseThrow(() -> new RuntimeException("No active company mapped to user"));
//        return user.getUserCompanies().stream()
//                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
//                .filter(uc -> uc.getCompany() != null)
//                .map(uc -> uc.getCompany())
//                .findFirst()
//                .orElse(null);
//    }
//    @Override
//    public DataResponse getRoyaltyPassStatusChart() {
//
//        Company company = getCurrentUserCompany();
//
////        if (company == null) {
////            return DataResponse.builder()
////                    .success(false)
////                    .message("Company not found")
////                    .build();
////        }
//
//        List<ChartDto> dbData = royaltyPassRepository.getStatusChart(company);
//
//        // Default values
//        Map<String, Long> map = new LinkedHashMap<>();
//        map.put("AVAILABLE", 0L);
//        map.put("USED", 0L);
//
//        for (ChartDto dto : dbData) {
//            map.put(dto.getLabel(), dto.getValue());
//        }
//
//        List<ChartDto> result = new ArrayList<>();
//        map.forEach((k, v) -> result.add(new ChartDto(k, v)));
//
//        return DataResponse.builder()
//                .success(true)
//                .successCode(HttpStatus.OK)
//                .data(result)
//                .build();
//    }
//
////    @Override
////    public DataResponse getRecentFuelMemos() {
////
////        Company company = getCurrentUserCompany();
////
//////        if (company == null) {
//////            return DataResponse.builder()
//////                    .success(false)
//////                    .message("Company not found")
//////                    .build();
//////        }
////
////        List<FuelMemoListDto> list = fuelMemoRepository.getRecentFuelMemos(company);
////
////        return DataResponse.builder()
////                .success(true)
////                .data(list)
////                .build();
////    }
//@Override
//public DataResponse getRecentFuelMemos(Integer companyId) {
//
//    Company company = null;
//
//    // ✅ jo companyId aavyo hoy to use karo
//    if (companyId != null) {
//        company = new Company();
//        company.setId(companyId);
//    }
//
//    // ✅ jo companyId na hoy → fallback (normal user)
//    if (company == null) {
//        company = getCurrentUserCompany();
//    }
//
//    // ✅ still null → return empty
//    if (company == null) {
//        return DataResponse.builder()
//                .success(true)
//                .data(new ArrayList<>())
//                .build();
//    }
//
//    List<FuelMemoListDto> list = fuelMemoRepository.getRecentFuelMemos(company);
//
//    return DataResponse.builder()
//            .success(true)
//            .data(list)
//            .build();
//}
//
//
//    @Override
//    public DataResponse getSummary() {
//
//        Company company = getCurrentUserCompany();
//
//        // ================= DATE =================
//        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
//        LocalDateTime tomorrowStart = todayStart.plusDays(1);
//
//        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
//        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
//        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
//
//        // ================= TOTAL =================
//        long totalDrivers = driverRepository.countByCompaniesContaining(company);
//        long totalVehicles = vehicleRepository.countByCompany(company);
//        long totalFuelMemos = fuelMemoRepository.countByCompany(company);
//
//        // ================= TODAY =================
//        long todayDrivers = driverRepository.countByCompaniesContainingAndCreatedAtBetween(company, todayStart, tomorrowStart);
//        long todayVehicles = vehicleRepository.countByCompanyAndCreatedAtBetween(company, todayStart, tomorrowStart);
//        long todayFuelMemos = fuelMemoRepository.countByCompanyAndCreatedAtBetween(company, todayStart, tomorrowStart);
//
//        // ================= GROWTH =================
//        double driversGrowth = calculateGrowth(todayDrivers, totalDrivers);
//        double vehiclesGrowth = calculateGrowth(todayVehicles, totalVehicles);
//        double fuelMemoGrowth = calculateGrowth(todayFuelMemos, totalFuelMemos);
//
//        // ================= USAGE =================
//        double currentMonthUsage = getSum(company, startOfMonth, startOfNextMonth);
//        double lastMonthUsage = getSum(company, startOfLastMonth, startOfMonth);
//
//        double usageGrowth = calculateGrowth(currentMonthUsage, lastMonthUsage);
//
//        // ================= RESPONSE =================
//        DashboardSummaryDto dto = DashboardSummaryDto.builder()
//                .totalDrivers(totalDrivers)
//                .driversGrowth(driversGrowth)
//                .totalVehicles(totalVehicles)
//                .vehiclesGrowth(vehiclesGrowth)
//                .totalFuelMemos(totalFuelMemos)
//                .fuelMemoGrowth(fuelMemoGrowth)
//                .monthlyUsage(currentMonthUsage)
//                .usageGrowth(usageGrowth)
//                .build();
//
//        return DataResponse.builder()
//                .success(true)
//                .successCode(HttpStatus.OK)
//                .data(dto)
//                .build();
//    }
//
//    @Override
//    public DataResponse getUsageGraph(String type) {
//
//        Company company = getCurrentUserCompany();
//
//        if (company == null) {
//            throw new RuntimeException("Please select a company");
//        }
//
//        List<UsageGraphDto> result = new ArrayList<>();
//
//        // ================= DAY =================
//        if ("DAY".equalsIgnoreCase(type)) {
//
//            for (int i = 6; i >= 0; i--) {
//
//                LocalDate currentDay = LocalDate.now().minusDays(i);
//
//                LocalDateTime start = currentDay.atStartOfDay();
//                LocalDateTime end = start.plusDays(1);
//
//                double sum = getSum(company, start, end);
//
//                result.add(new UsageGraphDto(
//                        currentDay.getDayOfWeek().name(), sum
//                ));
//            }
//        }
//
//        // ================= MONTH =================
//        else if ("MONTH".equalsIgnoreCase(type)) {
//
//            LocalDate today = LocalDate.now();
//            LocalDate startOfMonth = today.withDayOfMonth(1);
//
//            int daysInMonth = today.lengthOfMonth();
//
//            for (int i = 0; i < daysInMonth; i++) {
//
//                LocalDate currentDay = startOfMonth.plusDays(i);
//
//                LocalDateTime start = currentDay.atStartOfDay();
//                LocalDateTime end = start.plusDays(1);
//
//                double sum = getSum(company, start, end);
//
//                result.add(new UsageGraphDto(
//                        String.valueOf(currentDay.getDayOfMonth()), sum
//                ));
//            }
//        }
//
//        // ================= YEAR =================
//        else if ("YEAR".equalsIgnoreCase(type)) {
//
//            int year = LocalDate.now().getYear();
//
//            for (int i = 1; i <= 12; i++) {
//
//                LocalDate startMonth = LocalDate.of(year, i, 1);
//
//                LocalDateTime start = startMonth.atStartOfDay();
//                LocalDateTime end = start.plusMonths(1);
//
//                double sum = getSum(company, start, end);
//
//                result.add(new UsageGraphDto(
//                        startMonth.getMonth().name(), sum
//                ));
//            }
//        }
//
//        return DataResponse.builder()
//                .success(true)
//                .successCode(HttpStatus.OK)
//                .data(result)
//                .build();
//    }
//
//    // ================= COMMON =================
//    private double calculateGrowth(double current, double previous) {
//        if (previous == 0) return 0;
//        return ((current - previous) / previous) * 100;
//    }
//
//    // 🔥 COMPANY FILTERED SUM
//    private double getSum(Company company, LocalDateTime start, LocalDateTime end) {
//
//        List<FuelMemo> list =
//                fuelMemoRepository.findByCompanyAndCreatedAtBetween(company, start, end);
//
//        return list.stream()
//                .map(f -> f.getQuantity() != null ? f.getQuantity() : BigDecimal.ZERO)
//                .mapToDouble(BigDecimal::doubleValue)
//                .sum();
//    }
//}
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final FuelMemoRepository fuelMemoRepository;
    private final RoyaltyPassRepository royaltyPassRepository;

    public DashboardServiceImpl(
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            FuelMemoRepository fuelMemoRepository,
            RoyaltyPassRepository royaltyPassRepository) {

        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.fuelMemoRepository = fuelMemoRepository;
        this.royaltyPassRepository = royaltyPassRepository;
    }

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    // ================= AUTH HELPERS =================

//    private User getCurrentUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return (User) auth.getPrincipal();
//    }
//
//    private boolean isSuperAdmin(User user) {
//        return user.getUserCompanies().stream()
//                .anyMatch(uc ->
//                        uc.getRole() != null &&
//                                uc.getRole().getRoleName() != null &&
//                                uc.getRole().getRoleName().equalsIgnoreCase("SUPERADMIN")
//                );
//    }
//
//    private Company getUserCompany(User user) {
//        return user.getUserCompanies().stream()
//                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
//                .map(UserCompany::getCompany)
//                .findFirst()
//                .orElse(null);
//    }
//
//
//    private Company resolveCompany(Integer companyId) {
//
//        User user = getCurrentUser();
//
//        if (isSuperAdmin(user)) {
//            if (companyId == null) return null;
//
//            Company c = new Company();
//            c.setCompanyId(companyId);
//            return c;
//        }
//
//        return getUserCompany(user);
//    }
private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (User) auth.getPrincipal();
}

    private Company getUserCompany(User user) {
        return user.getUserCompanies().stream()
                .filter(uc -> Boolean.TRUE.equals(uc.getActive()))
                .map(uc -> uc.getCompany())
                .findFirst()
                .orElse(null);
    }
    private Company resolveCompany(Integer companyId) {

        // ✅ jo frontend thi companyId aave → direct use karo
        if (companyId != null) {
            Company c = new Company();
            c.setCompanyId(companyId);
            return c;
        }

        // ✅ fallback → logged user
        User user = getCurrentUser();
        return getUserCompany(user);
    }
    // ================= CHART =================
    @Override
    public DataResponse getRoyaltyPassStatusChart(Integer companyId) {

        Company company = resolveCompany(companyId);

        if (company == null) {
            List<ChartDto> result = new ArrayList<>();
            result.add(new ChartDto("AVAILABLE", 0L));
            result.add(new ChartDto("USED", 0L));

            return DataResponse.builder()
                    .success(true)
                    .data(result)
                    .build();
        }

        List<ChartDto> dbData = royaltyPassRepository.getStatusChart(company);

        Map<String, Long> map = new LinkedHashMap<>();
        map.put("AVAILABLE", 0L);
        map.put("USED", 0L);

        for (ChartDto dto : dbData) {
            map.put(dto.getLabel().toUpperCase(), dto.getValue());
        }

        List<ChartDto> result = new ArrayList<>();
        map.forEach((k, v) -> result.add(new ChartDto(k, v)));

        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(result)
                .build();
    }

    // ================= RECENT MEMOS =================
    @Override
    public DataResponse getRecentFuelMemos(Integer companyId) {

        Company company = resolveCompany(companyId);

        if (company == null) {
            return DataResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .data(new ArrayList<>())
                    .build();
        }

        List<FuelMemoListDto> list = fuelMemoRepository.getRecentFuelMemos(company);

        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(list)
                .build();
    }

    // ================= SUMMARY =================
    @Override
    public DataResponse getSummary(Integer companyId) {

        Company company = resolveCompany(companyId);

        if (company == null) {
            DashboardSummaryDto dto = DashboardSummaryDto.builder()
                    .totalDrivers(0)
                    .driversGrowth(0)
                    .totalVehicles(0)
                    .vehiclesGrowth(0)
                    .totalFuelMemos(0)
                    .fuelMemoGrowth(0)
                    .monthlyUsage(0)
                    .usageGrowth(0)
                    .build();

            return DataResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .data(dto)
                    .build();
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        long totalDrivers = driverRepository.countByCompaniesContaining(company);
        long totalVehicles = vehicleRepository.countByCompany(company);
        long totalFuelMemos = fuelMemoRepository.countByCompany(company);

        long todayDrivers = driverRepository.countByCompaniesContainingAndCreatedAtBetween(company, todayStart, tomorrowStart);
        long todayVehicles = vehicleRepository.countByCompanyAndCreatedAtBetween(company, todayStart, tomorrowStart);
        long todayFuelMemos = fuelMemoRepository.countByCompanyAndCreatedAtBetween(company, todayStart, tomorrowStart);

        double driversGrowth = calculateGrowth(todayDrivers, totalDrivers);
        double vehiclesGrowth = calculateGrowth(todayVehicles, totalVehicles);
        double fuelMemoGrowth = calculateGrowth(todayFuelMemos, totalFuelMemos);

        double currentMonthUsage = getSum(
                company,
                startOfMonth.toLocalDate(),
                startOfNextMonth.toLocalDate()
        );

        double lastMonthUsage = getSum(
                company,
                startOfLastMonth.toLocalDate(),
                startOfMonth.toLocalDate()
        );

        double usageGrowth = calculateGrowth(currentMonthUsage, lastMonthUsage);

        DashboardSummaryDto dto = DashboardSummaryDto.builder()
                .totalDrivers(totalDrivers)
                .driversGrowth(driversGrowth)
                .totalVehicles(totalVehicles)
                .vehiclesGrowth(vehiclesGrowth)
                .totalFuelMemos(totalFuelMemos)
                .fuelMemoGrowth(fuelMemoGrowth)
                .monthlyUsage(currentMonthUsage)
                .usageGrowth(usageGrowth)
                .build();

        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(dto)
                .build();
    }

    // ================= GRAPH =================
    @Override
    public DataResponse getUsageGraph(String type, Integer companyId) {

        Company company = resolveCompany(companyId);

        if (company == null) {
            return DataResponse.builder()
                    .success(true)
                    .successCode(HttpStatus.OK)
                    .data(new ArrayList<>())
                    .build();
        }

        List<UsageGraphDto> result = new ArrayList<>();
        LocalDate today = LocalDate.now(ZONE);

        // ================= DAY =================
        if ("DAY".equalsIgnoreCase(type)) {

            for (int i = 6; i >= 0; i--) {

                LocalDate d = today.minusDays(i);

                double sum = getSum(company, d, d.plusDays(1));

                result.add(new UsageGraphDto(
                        d.getDayOfWeek().name(), sum
                ));
            }
        }

        // ================= MONTH =================
        else if ("MONTH".equalsIgnoreCase(type)) {

            LocalDate startMonth = today.withDayOfMonth(1);
            int days = today.lengthOfMonth();

            for (int i = 0; i < days; i++) {

                LocalDate d = startMonth.plusDays(i);

                double sum = getSum(company, d, d.plusDays(1));

                result.add(new UsageGraphDto(
                        String.valueOf(d.getDayOfMonth()), sum
                ));
            }
        }

        // ================= YEAR =================
        else if ("YEAR".equalsIgnoreCase(type)) {

            int year = today.getYear();

            for (int m = 1; m <= 12; m++) {

                LocalDate start = LocalDate.of(year, m, 1);
                LocalDate end = start.plusMonths(1);

                double sum = getSum(company, start, end);

                result.add(new UsageGraphDto(
                        start.getMonth().name(), sum
                ));
            }
        }


        return DataResponse.builder()
                .success(true)
                .successCode(HttpStatus.OK)
                .data(result)
                .build();
    }


    // ================= COMMON =================
    private double calculateGrowth(double current, double previous) {
        if (previous == 0) return 0;
        return ((current - previous) / previous) * 100;
    }

//    private double getSum(Company company, LocalDateTime start, LocalDateTime end) {
//
//        List<FuelMemo> list =
//                fuelMemoRepository.findByCompanyAndCreatedAtBetween(company, start, end);
//
//        return list.stream()
//                .map(f -> f.getQuantity() != null ? f.getQuantity() : BigDecimal.ZERO)
//                .mapToDouble(BigDecimal::doubleValue)
//                .sum();
//    }
//private double getSum(Company company, LocalDate start, LocalDate end) {
//
//    List<FuelMemo> list = fuelMemoRepository.findByCompany(company);
//
//    LocalDate finalEnd = end.plusDays(1); // 🔥 IMPORTANT
//
//    return list.stream()
//            .filter(f -> f.getDate() != null)
//            .filter(f -> {
//                LocalDate d = f.getDate();
//                return !d.isBefore(start) && d.isBefore(finalEnd);
//            })
//            .map(f -> f.getQuantity() != null ? f.getQuantity() : BigDecimal.ZERO)
//            .mapToDouble(BigDecimal::doubleValue)
//            .sum();
//}
//private double getSum(Company company, LocalDate start, LocalDate end) {
//
//    //  IMPORTANT: end inclusive banavvu
//    LocalDate endDate = end.minusDays(1);
//
//    List<FuelMemo> list = fuelMemoRepository
//            .findByCompanyAndDateBetween(company, start, endDate);
//
//    return list.stream()
//            .map(f -> f.getQuantity() != null ? f.getQuantity() : BigDecimal.ZERO)
//            .mapToDouble(BigDecimal::doubleValue)
//            .sum();
//}

    private double getSum(Company company, LocalDate start, LocalDate end) {

        Double sum = fuelMemoRepository
                .getTotalByDateRange(company, start, end);

        return sum != null ? sum : 0.0;
    }
}