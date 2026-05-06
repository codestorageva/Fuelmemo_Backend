package com.example.FuelMemo.VehicleModule.Service.Impl;


import com.example.FuelMemo.VehicleModule.Entity.Vehicle;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class VehicleSpecifications {

    public static Specification<Vehicle> byCompanyAndActive(Integer companyId) {
        return (root, query, cb) -> {
            root.fetch("companies", JoinType.INNER);
            Predicate active = cb.isTrue(root.get("active"));
            Predicate notDeleted = cb.isFalse(root.get("deleted"));
            Predicate company = cb.equal(root.join("companies").get("companyId"), companyId);
            return cb.and(active, notDeleted, company);
        };
    }
}
