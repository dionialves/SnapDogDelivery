package com.dionialves.snapdogdelivery.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    long countByCreatedAt(LocalDate date);
}
