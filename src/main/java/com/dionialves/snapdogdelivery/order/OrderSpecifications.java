package com.dionialves.snapdogdelivery.order;

import org.springframework.data.jpa.domain.Specification;

public class OrderSpecifications {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> clientSearchContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("client").get("name")), pattern),
                    cb.like(cb.lower(root.get("client").get("email")), pattern),
                    cb.like(root.get("client").get("phone"), pattern));
        };
    }
}
