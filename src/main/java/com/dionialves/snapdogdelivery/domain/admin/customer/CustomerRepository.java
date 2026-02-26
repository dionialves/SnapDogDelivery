package com.dionialves.snapdogdelivery.domain.admin.customer;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    Page<Customer> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone, Pageable pageable);

    long countByCreatedAt(LocalDate date);
}
