package com.dionialves.snapdogdelivery.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    Page<Client> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone, Pageable pageable);

    long countByCreatedAt(LocalDate date);
}
