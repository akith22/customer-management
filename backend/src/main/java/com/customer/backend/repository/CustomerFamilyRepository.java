package com.customer.backend.repository;


import com.customer.backend.model.CustomerFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerFamilyRepository extends JpaRepository<CustomerFamily, CustomerFamily.CustomerFamilyId> {

    @Modifying
    @Query("DELETE FROM CustomerFamily f WHERE f.customer.id = :customerId")
    void deleteAllByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT f FROM CustomerFamily f JOIN FETCH f.familyMember WHERE f.customer.id = :customerId")
    java.util.List<CustomerFamily> findByCustomerIdWithMember(@Param("customerId") Long customerId);
}