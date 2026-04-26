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

    // ── Single customer with all associations in one query ───────────
    // LEFT JOIN FETCH prevents N+1 on mobiles, addresses, family links.
    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.mobiles " +
            "LEFT JOIN FETCH c.addresses a " +
            "LEFT JOIN FETCH a.city ci " +
            "LEFT JOIN FETCH ci.country " +
            "LEFT JOIN FETCH c.familyLinks fl " +
            "LEFT JOIN FETCH fl.familyMember " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    // ── Paginated summary list ────────────────────────────────────────
    // No ORDER BY here — Pageable carries the Sort so the caller controls
    // sort column and direction. Previously this was hardcoded to
    // "ORDER BY c.createdAt DESC" which silently ignored the Sort param.
    @Query(value      = "SELECT c FROM Customer c",
            countQuery = "SELECT COUNT(c) FROM Customer c")
    Page<Customer> findAllSummary(Pageable pageable);

    @Query(value      = "SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))",
            countQuery = "SELECT COUNT(c) FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findAllSummaryByName(@Param("name") String name, Pageable pageable);

    // ── Bulk upload helpers ──────────────────────────────────────────

    // Single DB round-trip to find which NICs in a chunk already exist.
    @Query("SELECT c.nic FROM Customer c WHERE c.nic IN :nicList")
    List<String> findExistingNics(@Param("nicList") List<String> nicList);

    // Fetch full Customer entities for the NICs we need to UPDATE in bulk.
    // Returns only the entities that exist — caller maps nic → entity.
    @Query("SELECT c FROM Customer c WHERE c.nic IN :nicList")
    List<Customer> findAllByNicIn(@Param("nicList") List<String> nicList);

    // Used by CustomerService to look up a single customer by NIC
    // (e.g. to validate uniqueness on update without an extra existsBy call).
    Optional<Customer> findByNic(String nic);
}