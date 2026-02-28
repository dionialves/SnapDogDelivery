package com.dionialves.snapdogdelivery.domain.admin.order;

import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> customerSearchContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("customer").get("name")), pattern),
                    cb.like(cb.lower(root.get("customer").get("email")), pattern),
                    cb.like(root.get("customer").get("phone"), pattern));
        };
    }
}
