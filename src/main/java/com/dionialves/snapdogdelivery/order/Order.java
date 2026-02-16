package com.dionialves.snapdogdelivery.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.dionialves.snapdogdelivery.client.Client;
import com.dionialves.snapdogdelivery.product.Product;
import com.dionialves.snapdogdelivery.productorder.ProductOrder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOrder> productOrders = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private BigDecimal totalValue;

    public void addProduct(Product product, Integer quantity, BigDecimal priceAtTime) {
        ProductOrder productOrder = new ProductOrder(product, this, quantity, priceAtTime);
        this.productOrders.add(productOrder);
    }

    public BigDecimal getTotalValue() {
        return this.getProductOrders().stream()
                .map(productOrder -> productOrder.getPriceAtTime()
                        .multiply(BigDecimal.valueOf(productOrder.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
