package com.example.FuelMemo.VehicleModule.Service.Impl;

import com.example.FuelMemo.VehicleModule.Dto.VehicleSummaryDto;
import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import com.example.FuelMemo.VehicleModule.Repository.VehicleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class VehicleSummaryService {

    private final VehicleRepository vehicleRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VehicleSummaryService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<VehicleSummaryDto> getVehicleSummary(Integer companyId, Integer month, Integer year) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<VehicleSummaryDto> cq = cb.createQuery(VehicleSummaryDto.class);

        Root<Vehicle> vehicle = cq.from(Vehicle.class);

        // ✅ Correct join
        Join<Vehicle, Object> companyJoin = vehicle.join("company", JoinType.INNER);
        Join<Vehicle, Object> fuelMemoJoin = vehicle.join("fuelMemos", JoinType.LEFT);

        // ✅ Aggregations
        Expression<BigDecimal> totalFuel =
                cb.coalesce(cb.sum(fuelMemoJoin.get("quantity")), BigDecimal.ZERO);

        Expression<Long> memoCount =
                cb.count(fuelMemoJoin.get("fuelMemoId"));

        Expression<LocalDate> lastFuelDate =
                cb.greatest(fuelMemoJoin.<LocalDate>get("date"));

        Expression<Boolean> monthClosed = cb.literal(false);

        cq.select(cb.construct(
                VehicleSummaryDto.class,
                vehicle.get("vehicleNumber"),
                vehicle.get("ownerName"),
                vehicle.get("active"),
                totalFuel,
                memoCount,
                lastFuelDate,
                monthClosed
        ));

        // ✅ Base conditions
        Predicate predicate = cb.and(
                cb.isTrue(vehicle.get("active")),
                cb.isFalse(vehicle.get("deleted")),
                cb.equal(companyJoin.get("companyId"), companyId)
        );

        // ✅ Month/Year filter
        if (month != null && year != null) {

            Expression<Integer> monthExp =
                    cb.function("MONTH", Integer.class, fuelMemoJoin.get("date"));

            Expression<Integer> yearExp =
                    cb.function("YEAR", Integer.class, fuelMemoJoin.get("date"));

            predicate = cb.and(predicate,
                    cb.equal(monthExp, month),
                    cb.equal(yearExp, year)
            );
        }

        cq.where(predicate);

        // ✅ GROUP BY
        cq.groupBy(
                vehicle.get("vehicleNumber"),
                vehicle.get("ownerName"),
                vehicle.get("active")
        );

        return entityManager.createQuery(cq).getResultList();
    }
}