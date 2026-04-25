package com.customer.backend.repository;


import com.customer.backend.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByCountryId(Long countryId);

    @Query("SELECT c FROM City c JOIN FETCH c.country WHERE c.id = :id")
    Optional<City> findByIdWithCountry(@Param("id") Long id);
}