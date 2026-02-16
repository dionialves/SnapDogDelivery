package com.dionialves.snapdogdelivery.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("SELECT c FROM Client c " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.phone LIKE CONCAT('%', :search, '%')")
    List<Client> searchByNameOrPhone(@Param("search") String search);

    List<Client> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    long countByCreatedAt(LocalDate date);
}
