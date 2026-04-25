package com.customer.backend.repository;

import com.customer.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByNic(String nic);

    // Fetch single customer with all associations in one query
    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.mobiles " +
            "LEFT JOIN FETCH c.addresses a " +
            "LEFT JOIN FETCH a.city ci " +
            "LEFT JOIN FETCH ci.country " +
            "LEFT JOIN FETCH c.familyLinks fl " +
            "LEFT JOIN FETCH fl.familyMember " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    // Paginated summary list — no heavy joins
    @Query(value = "SELECT c FROM Customer c ORDER BY c.createdAt DESC",
            countQuery = "SELECT COUNT(c) FROM Customer c")
    Page<Customer> findAllSummary(Pageable pageable);

    // Used during bulk upload to check existing NICs in one DB call
    @Query("SELECT c.nic FROM Customer c WHERE c.nic IN :nicList")
    List<String> findExistingNics(@Param("nicList") List<String> nicList);
}