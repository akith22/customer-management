package com.customer.backend.repository;

import com.customer.backend.model.City;
import com.customer.backend.model.Country;
import com.customer.backend.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository-layer tests using an H2 in-memory database.
 *
 * @TestPropertySource explicitly loads the application-test.properties
 * to configure the embedded H2 database and hibernate properties.
 * @DirtiesContext ensures a clean context after this class completes.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@EntityScan(basePackages = "com.customer.backend.model")
@DirtiesContext
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Long aliceId;

    @BeforeEach
    void setUp() {
        Country country = new Country();
        country.setName("Sri Lanka");
        country.setCode("LK");
        entityManager.persist(country);

        City city = new City();
        city.setName("Colombo");
        city.setCountry(country);
        entityManager.persist(city);

        Customer alice = new Customer();
        alice.setName("Alice");
        alice.setNic("199001512345");
        alice.setDob(LocalDate.of(1990, 1, 15));
        entityManager.persist(alice);

        Customer bob = new Customer();
        bob.setName("Bob");
        bob.setNic("850620123V");
        bob.setDob(LocalDate.of(1985, 6, 20));
        entityManager.persist(bob);

        entityManager.flush();
        entityManager.clear();

        aliceId = alice.getId();
    }

    @Test
    void existsByNic_existingNic_returnsTrue() {
        assertTrue(customerRepository.existsByNic("199001512345"));
    }

    @Test
    void existsByNic_unknownNic_returnsFalse() {
        assertFalse(customerRepository.existsByNic("000000000000"));
    }

    @Test
    void findByIdWithDetails_existingId_returnsCustomerWithAssociations() {
        Optional<Customer> result = customerRepository.findByIdWithDetails(aliceId);
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    void findByIdWithDetails_unknownId_returnsEmpty() {
        Optional<Customer> result = customerRepository.findByIdWithDetails(99999L);
        assertFalse(result.isPresent());
    }

    @Test
    void findAllSummary_returnsPagedResults() {
        Page<Customer> page = customerRepository.findAllSummary(
                PageRequest.of(0, 10, Sort.by("name").ascending())
        );
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void findAllSummaryByName_partialMatch_returnsMatchingCustomers() {
        Page<Customer> page = customerRepository.findAllSummaryByName("ali", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("Alice", page.getContent().get(0).getName());
    }

    @Test
    void findAllSummaryByName_noMatch_returnsEmpty() {
        Page<Customer> page = customerRepository.findAllSummaryByName("xyz", PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void findAllSummaryByName_caseInsensitive_returnsMatch() {
        Page<Customer> page = customerRepository.findAllSummaryByName("ALICE", PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void findExistingNics_partialList_returnsOnlyMatching() {
        List<String> nics = customerRepository.findExistingNics(
                Arrays.asList("199001512345", "DOESNOTEXIST")
        );
        assertEquals(1, nics.size());
        assertTrue(nics.contains("199001512345"));
    }

    @Test
    void findAllByNicIn_multipleNics_returnsEntities() {
        List<Customer> customers = customerRepository.findAllByNicIn(
                Arrays.asList("199001512345", "850620123V")
        );
        assertEquals(2, customers.size());
    }

    @Test
    void findByNic_existing_returnsCustomer() {
        Optional<Customer> result = customerRepository.findByNic("199001512345");
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getName());
    }

    @Test
    void findByNic_unknown_returnsEmpty() {
        Optional<Customer> result = customerRepository.findByNic("000000000000");
        assertFalse(result.isPresent());
    }

    @Test
    void save_newCustomer_persistsAndAssignsId() {
        Customer customer = new Customer();
        customer.setName("Carol");
        customer.setNic("200003101234");
        customer.setDob(LocalDate.of(2000, 3, 10));

        Customer saved = customerRepository.saveAndFlush(customer);

        assertNotNull(saved.getId());
        assertTrue(customerRepository.existsByNic("200003101234"));
    }
}