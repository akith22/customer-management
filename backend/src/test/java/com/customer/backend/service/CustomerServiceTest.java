package com.customer.backend.service;

import com.customer.backend.dto.request.AddressRequestDTO;
import com.customer.backend.dto.request.CustomerRequestDTO;
import com.customer.backend.dto.request.MobileRequestDTO;
import com.customer.backend.dto.response.CustomerResponseDTO;
import com.customer.backend.dto.response.CustomerSummaryDTO;
import com.customer.backend.model.*;
import com.customer.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository        customerRepository;
    @Mock private CustomerMobileRepository  mobileRepository;
    @Mock private CustomerAddressRepository addressRepository;
    @Mock private CustomerFamilyRepository  familyRepository;
    @Mock private CityRepository            cityRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer        sampleCustomer;
    private CustomerRequestDTO sampleRequestDTO;

    @BeforeEach
    void setUp() {
        // Build a reusable sample Customer entity
        sampleCustomer = new Customer();
        sampleCustomer.setId(1L);
        sampleCustomer.setName("John Doe");
        sampleCustomer.setDob(LocalDate.of(1990, 1, 15));
        sampleCustomer.setNic("123456789V");

        // Build a reusable request DTO
        sampleRequestDTO = new CustomerRequestDTO();
        sampleRequestDTO.setName("John Doe");
        sampleRequestDTO.setDob(LocalDate.of(1990, 1, 15));
        sampleRequestDTO.setNic("123456789V");
        sampleRequestDTO.setMobiles(new ArrayList<>());
        sampleRequestDTO.setAddresses(new ArrayList<>());
        sampleRequestDTO.setFamilyMemberIds(new ArrayList<>());
    }

    // ── CREATE ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createCustomer: success with mandatory fields only")
    void createCustomer_success() {
        when(customerRepository.existsByNic("123456789V")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleCustomer));

        CustomerResponseDTO result = customerService.createCustomer(sampleRequestDTO);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("123456789V", result.getNic());

        verify(customerRepository, times(1)).existsByNic("123456789V");
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(customerRepository, times(1)).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("createCustomer: throws exception when NIC already exists")
    void createCustomer_duplicateNic_throwsException() {
        when(customerRepository.existsByNic("123456789V")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(sampleRequestDTO)
        );

        assertTrue(ex.getMessage().contains("123456789V"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("createCustomer: saves mobile numbers when provided")
    void createCustomer_withMobiles_savesMobiles() {
        MobileRequestDTO mobileDTO = new MobileRequestDTO();
        mobileDTO.setMobile("0771234567");
        sampleRequestDTO.setMobiles(Collections.singletonList(mobileDTO));

        when(customerRepository.existsByNic(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleCustomer));

        customerService.createCustomer(sampleRequestDTO);

        verify(mobileRepository, times(1)).saveAll(anyCollection());
    }

    @Test
    @DisplayName("createCustomer: saves addresses when provided")
    void createCustomer_withAddresses_savesAddresses() {
        Country country = new Country();
        country.setId(1L);
        country.setName("Sri Lanka");
        country.setCode("LK");

        City city = new City();
        city.setId(1L);
        city.setName("Colombo");
        city.setCountry(country);

        AddressRequestDTO addressDTO = new AddressRequestDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setCityId(1L);
        sampleRequestDTO.setAddresses(Collections.singletonList(addressDTO));

        when(customerRepository.existsByNic(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleCustomer));

        customerService.createCustomer(sampleRequestDTO);

        verify(addressRepository, times(1)).saveAll(anyCollection());
    }

    @Test
    @DisplayName("createCustomer: throws exception when city not found")
    void createCustomer_cityNotFound_throwsException() {
        AddressRequestDTO addressDTO = new AddressRequestDTO();
        addressDTO.setAddressLine1("123 Main St");
        addressDTO.setCityId(99L);
        sampleRequestDTO.setAddresses(Collections.singletonList(addressDTO));

        when(customerRepository.existsByNic(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(cityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(sampleRequestDTO)
        );
    }

    @Test
    @DisplayName("createCustomer: throws when customer set as their own family member")
    void createCustomer_selfFamilyMember_throwsException() {
        sampleRequestDTO.setFamilyMemberIds(Collections.singletonList(1L));

        when(customerRepository.existsByNic(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(sampleRequestDTO)
        );

        verify(familyRepository, never()).saveAll(anyCollection());
    }

    @Test
    @DisplayName("createCustomer: throws when family member customer not found")
    void createCustomer_familyMemberNotFound_throwsException() {
        sampleRequestDTO.setFamilyMemberIds(Collections.singletonList(99L));

        when(customerRepository.existsByNic(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(sampleRequestDTO)
        );
    }

    // ── UPDATE ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateCustomer: success — same NIC, fields updated")
    void updateCustomer_success_sameNic() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleCustomer));

        CustomerResponseDTO result = customerService.updateCustomer(1L, sampleRequestDTO);

        assertNotNull(result);
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(mobileRepository,  times(1)).deleteAllByCustomerId(1L);
        verify(addressRepository, times(1)).deleteAllByCustomerId(1L);
        verify(familyRepository,  times(1)).deleteAllByCustomerId(1L);
    }

    @Test
    @DisplayName("updateCustomer: success — NIC changed to a non-existing NIC")
    void updateCustomer_success_nicChanged() {
        sampleRequestDTO.setNic("NEW_NIC_999");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.existsByNic("NEW_NIC_999")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(sampleCustomer);
        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleCustomer));

        CustomerResponseDTO result = customerService.updateCustomer(1L, sampleRequestDTO);

        assertNotNull(result);
        verify(customerRepository, times(1)).existsByNic("NEW_NIC_999");
    }

    @Test
    @DisplayName("updateCustomer: throws when customer not found")
    void updateCustomer_customerNotFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(99L, sampleRequestDTO)
        );

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCustomer: throws when new NIC belongs to another customer")
    void updateCustomer_duplicateNic_throwsException() {
        sampleRequestDTO.setNic("TAKEN_NIC");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(sampleCustomer));
        when(customerRepository.existsByNic("TAKEN_NIC")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(1L, sampleRequestDTO)
        );

        verify(customerRepository, never()).save(any());
    }

    // ── GET BY ID ────────────────────────────────────────────────────

    @Test
    @DisplayName("getCustomerById: returns full DTO when found")
    void getCustomerById_success() {
        when(customerRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleCustomer));

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    @DisplayName("getCustomerById: throws when not found")
    void getCustomerById_notFound_throwsException() {
        when(customerRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> customerService.getCustomerById(99L)
        );
    }

    // ── GET ALL ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCustomers: returns paginated summary list")
    void getAllCustomers_returnsPaginatedList() {
        Customer c1 = new Customer();
        c1.setId(1L);
        c1.setName("Alice");
        c1.setDob(LocalDate.of(1992, 3, 10));
        c1.setNic("NIC001");

        Customer c2 = new Customer();
        c2.setId(2L);
        c2.setName("Bob");
        c2.setDob(LocalDate.of(1988, 7, 22));
        c2.setNic("NIC002");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Customer> page = new PageImpl<>(Arrays.asList(c1, c2), pageable, 2);

        when(customerRepository.findAllSummary(pageable)).thenReturn(page);

        Page<CustomerSummaryDTO> result = customerService.getAllCustomers(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Alice", result.getContent().get(0).getName());
        assertEquals("Bob",   result.getContent().get(1).getName());
    }

    @Test
    @DisplayName("getAllCustomers: returns empty page when no customers exist")
    void getAllCustomers_emptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(customerRepository.findAllSummary(pageable)).thenReturn(emptyPage);

        Page<CustomerSummaryDTO> result = customerService.getAllCustomers(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }
}