package com.dionialves.snapdogdelivery.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dionialves.snapdogdelivery.dashboard.dto.DashboardTopProductDTO;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(po.priceAtTime * po.quantity), 0) " +
            "FROM Order o JOIN o.productOrders po " +
            "WHERE o.createdAt BETWEEN :start AND :end")
    BigDecimal sumRevenueByCreatedAtBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    List<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT new com.dionialves.snapdogdelivery.dashboard.dto.DashboardTopProductDTO(" +
            "p.id, p.name, p.price, SUM(po.quantity)) " +
            "FROM ProductOrder po JOIN po.product p " +
            "GROUP BY p.id, p.name, p.price " +
            "ORDER BY SUM(po.quantity) DESC")
    List<DashboardTopProductDTO> findTopSellingProducts(Pageable pageable);
}
