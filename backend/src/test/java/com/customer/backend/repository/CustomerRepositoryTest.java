/*package com.customer.backend.repository;

import com.customer.backend.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryTest {

    @Mock
    private CustomerRepository customerRepository;

    // ── existsByNic ──────────────────────────────────────────────────

    @Test
    @DisplayName("existsByNic: returns true when NIC is found")
    void existsByNic_returnsTrue() {
        when(customerRepository.existsByNic("NIC001")).thenReturn(true);

        assertTrue(customerRepository.existsByNic("NIC001"));
        verify(customerRepository, times(1)).existsByNic("NIC001");
    }

    @Test
    @DisplayName("existsByNic: returns false when NIC is not found")
    void existsByNic_returnsFalse() {
        when(customerRepository.existsByNic("UNKNOWN")).thenReturn(false);

        assertFalse(customerRepository.existsByNic("UNKNOWN"));
    }

    // ── findByIdWithDetails ──────────────────────────────────────────

    @Test
    @DisplayName("findByIdWithDetails: returns customer when found")
    void findByIdWithDetails_returnsCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");
        customer.setNic("NIC001");
        customer.setDob(LocalDate.of(1990, 5, 20));

        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(customer));

        Optional<Customer> result = customerRepository.findByIdWithDetails(1L);

        assertTrue(result.isPresent());
        assertEquals("Alice",  result.get().getName());
        assertEquals("NIC001", result.get().getNic());
    }

    @Test
    @DisplayName("findByIdWithDetails: returns empty when not found")
    void findByIdWithDetails_returnsEmpty() {
        when(customerRepository.findByIdWithDetails(99L))
                .thenReturn(Optional.empty());

        Optional<Customer> result = customerRepository.findByIdWithDetails(99L);

        assertFalse(result.isPresent());
    }

    // ── findAllSummary ───────────────────────────────────────────────

    @Test
    @DisplayName("findAllSummary: returns paginated list of customers")
    void findAllSummary_returnsPaginatedList() {
        Customer c1 = new Customer();
        c1.setId(1L);
        c1.setName("Alice");

        Customer c2 = new Customer();
        c2.setId(2L);
        c2.setName("Bob");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(Arrays.asList(c1, c2), pageable, 2);

        when(customerRepository.findAllSummary(pageable)).thenReturn(page);

        Page<Customer> result = customerRepository.findAllSummary(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Alice", result.getContent().get(0).getName());
        assertEquals("Bob",   result.getContent().get(1).getName());
    }

    @Test
    @DisplayName("findAllSummary: returns empty page when no customers")
    void findAllSummary_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(customerRepository.findAllSummary(pageable)).thenReturn(emptyPage);

        Page<Customer> result = customerRepository.findAllSummary(pageable);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    // ── findExistingNics ─────────────────────────────────────────────

    @Test
    @DisplayName("findExistingNics: returns only NICs that exist in DB")
    void findExistingNics_returnsMatchingNics() {
        List<String> input = Arrays.asList("NIC001", "NIC002", "NIC999");

        when(customerRepository.findExistingNics(input))
                .thenReturn(Arrays.asList("NIC001", "NIC002"));

        List<String> result = customerRepository.findExistingNics(input);

        assertEquals(2, result.size());
        assertTrue(result.contains("NIC001"));
        assertTrue(result.contains("NIC002"));
        assertFalse(result.contains("NIC999"));
    }

    @Test
    @DisplayName("findExistingNics: returns empty list when no NICs match")
    void findExistingNics_returnsEmpty() {
        List<String> input = Arrays.asList("NEW001", "NEW002");

        when(customerRepository.findExistingNics(input))
                .thenReturn(new ArrayList<>());

        List<String> result = customerRepository.findExistingNics(input);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findExistingNics: handles empty input list")
    void findExistingNics_emptyInput_returnsEmpty() {
        List<String> emptyInput = new ArrayList<>();

        when(customerRepository.findExistingNics(emptyInput))
                .thenReturn(new ArrayList<>());

        List<String> result = customerRepository.findExistingNics(emptyInput);

        assertTrue(result.isEmpty());
    }

    // ── save / findById (basic JpaRepository) ───────────────────────

    @Test
    @DisplayName("save: persists customer and returns saved entity")
    void save_persistsCustomer() {
        Customer customer = new Customer();
        customer.setName("Carol");
        customer.setNic("NIC_CAROL");
        customer.setDob(LocalDate.of(1995, 8, 15));

        Customer saved = new Customer();
        saved.setId(10L);
        saved.setName("Carol");
        saved.setNic("NIC_CAROL");
        saved.setDob(LocalDate.of(1995, 8, 15));

        when(customerRepository.save(customer)).thenReturn(saved);

        Customer result = customerRepository.save(customer);

        assertNotNull(result.getId());
        assertEquals(10L,       result.getId());
        assertEquals("Carol",   result.getName());
        assertEquals("NIC_CAROL", result.getNic());
    }

    @Test
    @DisplayName("findById: returns empty when customer does not exist")
    void findById_notFound_returnsEmpty() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerRepository.findById(999L);

        assertFalse(result.isPresent());
    }
}*/