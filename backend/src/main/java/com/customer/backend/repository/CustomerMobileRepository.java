package com.customer.backend.repository;


import com.customer.backend.model.CustomerMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerMobileRepository extends JpaRepository<CustomerMobile, Long> {

    @Modifying
    @Query("DELETE FROM CustomerMobile m WHERE m.customer.id = :customerId")
    void deleteAllByCustomerId(@Param("customerId") Long customerId);
}