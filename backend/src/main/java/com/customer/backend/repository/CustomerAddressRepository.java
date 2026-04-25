package com.customer.backend.repository;

import com.customer.backend.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    @Modifying
    @Query("DELETE FROM CustomerAddress a WHERE a.customer.id = :customerId")
    void deleteAllByCustomerId(@Param("customerId") Long customerId);
}