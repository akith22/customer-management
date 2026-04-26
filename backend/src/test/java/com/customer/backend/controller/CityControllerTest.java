package com.customer.backend.controller;

import com.customer.backend.model.City;
import com.customer.backend.model.Country;
import com.customer.backend.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CityController.class)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityRepository cityRepository;

    @Test
    void searchCities_validTerm_returns200WithResults() throws Exception {
        Country country = new Country();
        country.setId(1L);
        country.setName("Sri Lanka");
        country.setCode("LK");

        City city = new City();
        city.setId(1L);
        city.setName("Colombo");
        city.setCountry(country);

        when(cityRepository.searchByNameOrCountry(eq("Colombo"), any(Pageable.class)))
                .thenReturn(Collections.singletonList(city));

        mockMvc.perform(get("/api/cities").param("search", "Colombo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Colombo"))
                .andExpect(jsonPath("$[0].countryName").value("Sri Lanka"));
    }

    @Test
    void searchCities_emptySearch_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/cities").param("search", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(cityRepository, never()).searchByNameOrCountry(any(String.class), any(Pageable.class));
    }

    @Test
    void searchCities_blankSearch_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/cities").param("search", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(cityRepository, never()).searchByNameOrCountry(any(String.class), any(Pageable.class));
    }

    @Test
    void searchCities_limitCappedAt20_callsRepoWithSafeLimit() throws Exception {
        when(cityRepository.searchByNameOrCountry(eq("col"), any(Pageable.class)))
                .thenReturn(Collections.<City>emptyList());

        mockMvc.perform(get("/api/cities")
                        .param("search", "col")
                        .param("limit", "999"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cityRepository).searchByNameOrCountry(eq("col"), captor.capture());
        assertEquals(20, captor.getValue().getPageSize());
    }

    @Test
    void searchCities_limitBelowOne_clampedTo1() throws Exception {
        when(cityRepository.searchByNameOrCountry(eq("col"), any(Pageable.class)))
                .thenReturn(Collections.<City>emptyList());

        mockMvc.perform(get("/api/cities")
                        .param("search", "col")
                        .param("limit", "0"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cityRepository).searchByNameOrCountry(eq("col"), captor.capture());
        assertTrue(captor.getValue().getPageSize() >= 1);
        assertEquals(1, captor.getValue().getPageSize());
    }

    @Test
    void searchCities_defaultLimit10_works() throws Exception {
        when(cityRepository.searchByNameOrCountry(eq("col"), any(Pageable.class)))
                .thenReturn(Collections.<City>emptyList());

        mockMvc.perform(get("/api/cities").param("search", "col"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(cityRepository).searchByNameOrCountry(eq("col"), captor.capture());
        assertEquals(10, captor.getValue().getPageSize());
    }
}
