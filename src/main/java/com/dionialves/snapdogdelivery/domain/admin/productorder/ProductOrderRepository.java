package com.dionialves.snapdogdelivery.domain.admin.productorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    boolean existsByProductId(Long productId);
}
