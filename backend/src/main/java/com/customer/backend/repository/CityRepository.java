package com.customer.backend.repository;

import com.customer.backend.model.City;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByCountryId(Long countryId);

    // Eager-fetch country to avoid a second query when building AddressResponseDTO.
    @Query("SELECT c FROM City c JOIN FETCH c.country WHERE c.id = :id")
    Optional<City> findByIdWithCountry(@Param("id") Long id);;

    /**
     * Look up a city by city name AND country name (both case-insensitive).
     * Used when the frontend submits an address as plain text — the service
     * resolves the name pair to a managed City entity before persisting.
     *
     * JOIN FETCH country so the caller gets a fully-loaded entity in one
     * query with no lazy-load follow-up.
     */
    @Query("SELECT c FROM City c JOIN FETCH c.country co " +
            "WHERE LOWER(c.name) = LOWER(:cityName) " +
            "AND LOWER(co.name) = LOWER(:countryName)")
    Optional<City> findByCityAndCountry(@Param("cityName")    String cityName,
                                        @Param("countryName") String countryName);

    /**
     * Case-insensitive partial-match search on city or country name.
     * Used by CityController for the frontend search-as-you-type endpoint.
     * Pageable is used only for row limiting (PageRequest.of(0, n)).
     */
    @Query("SELECT c FROM City c JOIN FETCH c.country co " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(co.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<City> searchByNameOrCountry(@Param("search") String search,
                                     Pageable pageable);
}