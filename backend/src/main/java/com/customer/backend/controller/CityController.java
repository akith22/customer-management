package com.customer.backend.controller;

import com.customer.backend.dto.response.CityResponseDTO;
import com.customer.backend.model.City;
import com.customer.backend.repository.CityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides city lookup for the frontend address form.
 *
 * The assignment says cities/countries don't need their own management UI,
 * but the frontend still needs to resolve a city name → cityId when a user
 * types an address. This endpoint fulfils that without exposing any
 * create/update/delete operations on master data.
 *
 * GET /api/cities?search=colombo&limit=10
 */
@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = "*")
public class CityController {

    // Hard cap — never return more than this regardless of what the caller
    // asks for. Protects against someone passing limit=100000.
    private static final int MAX_RESULTS = 20;

    private final CityRepository cityRepository;

    public CityController(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    /**
     * Search cities by name or country name (case-insensitive, partial match).
     *
     * @param search  The search term. Empty string returns an empty list
     *                rather than the entire city table — avoids accidental
     *                full-table dumps on the frontend.
     * @param limit   Max results to return. Capped at {@value #MAX_RESULTS}.
     */
    @GetMapping
    public ResponseEntity<List<CityResponseDTO>> searchCities(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "10") int limit) {

        // Don't run a query on an empty search — it would match every row.
        if (search.trim().isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        int safeLimit = Math.min(Math.max(limit, 1), MAX_RESULTS);

        List<City> cities = cityRepository.searchByNameOrCountry(
                search.trim(),
                PageRequest.of(0, safeLimit)
        );

        List<CityResponseDTO> result = cities.stream()
                .map(c -> new CityResponseDTO(
                        c.getId(),
                        c.getName(),
                        c.getCountry().getName(),
                        c.getCountry().getCode()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}