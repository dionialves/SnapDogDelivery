package com.dionialves.snapdogdelivery.domain.admin.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /** Retorna apenas produtos marcados como ativos (visíveis no catálogo público). */
    Page<Product> findByActiveTrue(Pageable pageable);

    /** Retorna os N primeiros produtos ativos ordenados por nome (para a landing page). */
    List<Product> findTop6ByActiveTrueOrderByNameAsc();

}
