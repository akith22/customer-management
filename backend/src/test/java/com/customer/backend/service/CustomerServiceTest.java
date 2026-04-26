package com.customer.backend.service;

import com.customer.backend.dto.request.AddressRequestDTO;
import com.customer.backend.dto.request.CustomerRequestDTO;
import com.customer.backend.dto.request.MobileRequestDTO;
import com.customer.backend.dto.response.CustomerResponseDTO;
import com.customer.backend.dto.response.CustomerSummaryDTO;
import com.customer.backend.model.City;
import com.customer.backend.model.Country;
import com.customer.backend.model.Customer;
import com.customer.backend.model.CustomerAddress;
import com.customer.backend.model.CustomerFamily;
import com.customer.backend.model.CustomerMobile;
import com.customer.backend.repository.CityRepository;
import com.customer.backend.repository.CustomerAddressRepository;
import com.customer.backend.repository.CustomerFamilyRepository;
import com.customer.backend.repository.CustomerMobileRepository;
import com.customer.backend.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMobileRepository mobileRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    @Mock
    private CustomerFamilyRepository familyRepository;

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_success_returnsFullDTO() {
        CustomerRequestDTO request = baseRequest();

        MobileRequestDTO mobile = new MobileRequestDTO();
        mobile.setMobile("0771234567");
        request.setMobiles(Collections.singletonList(mobile));

        AddressRequestDTO address = new AddressRequestDTO();
        address.setAddressLine1("123 Main Street");
        address.setAddressLine2("Apartment 4B");
        address.setCityName("Colombo");
        address.setCountryName("Sri Lanka");
        request.setAddresses(Collections.singletonList(address));
        request.setFamilyMemberIds(Collections.singletonList(2L));

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("Alice");
        savedCustomer.setDob(LocalDate.of(1990, 1, 15));
        savedCustomer.setNic("199001512345");

        Customer familyMember = new Customer();
        familyMember.setId(2L);
        familyMember.setName("Bob Family");
        familyMember.setDob(LocalDate.of(1985, 6, 20));
        familyMember.setNic("850620123V");

        Country country = new Country();
        country.setId(1L);
        country.setName("Sri Lanka");
        country.setCode("LK");

        City city = new City();
        city.setId(1L);
        city.setName("Colombo");
        city.setCountry(country);

        Customer detailedCustomer = detailedCustomer(savedCustomer, city, familyMember);

        when(customerRepository.existsByNic("199001512345")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(cityRepository.findByCityAndCountry("Colombo", "Sri Lanka")).thenReturn(Optional.of(city));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(familyMember));
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(detailedCustomer));

        CustomerResponseDTO result = customerService.createCustomer(request);

        assertNotNull(result);
        assertEquals("Alice", result.getName());
        assertEquals("199001512345", result.getNic());
        assertEquals(LocalDate.of(1990, 1, 15), result.getDob());
        assertEquals(1, result.getMobiles().size());
        assertTrue(result.getMobiles().contains("0771234567"));
        assertEquals(1, result.getAddresses().size());
        assertEquals("123 Main Street", result.getAddresses().get(0).getAddressLine1());
        assertEquals("Colombo", result.getAddresses().get(0).getCity().getName());
        assertEquals("Sri Lanka", result.getAddresses().get(0).getCity().getCountryName());
        assertEquals(1, result.getFamilyMembers().size());
        assertEquals("Bob Family", result.getFamilyMembers().get(0).getName());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateNic_throwsIllegalArgumentException() {
        CustomerRequestDTO request = baseRequest();
        when(customerRepository.existsByNic(request.getNic())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void createCustomer_withNoMobilesOrAddresses_success() {
        CustomerRequestDTO request = baseRequest();
        request.setMobiles(Collections.emptyList());
        request.setAddresses(Collections.emptyList());
        request.setFamilyMemberIds(Collections.emptyList());

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(request.getName());
        savedCustomer.setDob(request.getDob());
        savedCustomer.setNic(request.getNic());

        when(customerRepository.existsByNic(request.getNic())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(savedCustomer));

        CustomerResponseDTO result = customerService.createCustomer(request);

        assertNotNull(result);
        verify(mobileRepository, never()).saveAll(anyCollection());
        verify(addressRepository, never()).saveAll(anyCollection());
    }

    @Test
    void createCustomer_addressCityNotFound_throwsIllegalArgumentException() {
        CustomerRequestDTO request = baseRequest();

        AddressRequestDTO address = new AddressRequestDTO();
        address.setAddressLine1("123 Main Street");
        address.setCityName("Unknown");
        address.setCountryName("Unknown");
        request.setAddresses(Collections.singletonList(address));

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(request.getName());
        savedCustomer.setDob(request.getDob());
        savedCustomer.setNic(request.getNic());

        when(customerRepository.existsByNic(request.getNic())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(cityRepository.findByCityAndCountry(anyString(), anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertTrue(ex.getMessage().contains("was not found"));
    }

    @Test
    void createCustomer_selfFamilyLink_throwsIllegalArgumentException() {
        CustomerRequestDTO request = baseRequest();
        request.setFamilyMemberIds(Collections.singletonList(1L));

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(request.getName());
        savedCustomer.setDob(request.getDob());
        savedCustomer.setNic(request.getNic());

        when(customerRepository.existsByNic(request.getNic())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertTrue(ex.getMessage().contains("cannot be their own family member"));
    }

    @Test
    void createCustomer_familyMemberNotFound_throwsIllegalArgumentException() {
        CustomerRequestDTO request = baseRequest();
        request.setFamilyMemberIds(Collections.singletonList(99L));

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName(request.getName());
        savedCustomer.setDob(request.getDob());
        savedCustomer.setNic(request.getNic());

        when(customerRepository.existsByNic(request.getNic())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.createCustomer(request)
        );

        assertTrue(ex.getMessage().contains("Family member customer not found"));
    }

    @Test
    void updateCustomer_success_returnsUpdatedDTO() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setDob(LocalDate.of(1990, 1, 15));
        existing.setNic("OLD123456789");

        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Updated Name");
        request.setDob(LocalDate.of(1991, 2, 10));
        request.setNic("OLD123456789");
        request.setMobiles(Collections.emptyList());
        request.setAddresses(Collections.emptyList());
        request.setFamilyMemberIds(Collections.emptyList());

        Customer updated = new Customer();
        updated.setId(1L);
        updated.setName("Updated Name");
        updated.setDob(LocalDate.of(1991, 2, 10));
        updated.setNic("OLD123456789");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenReturn(updated);
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(updated));

        CustomerResponseDTO result = customerService.updateCustomer(1L, request);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(mobileRepository, times(1)).deleteAllByCustomerId(1L);
        verify(addressRepository, times(1)).deleteAllByCustomerId(1L);
        verify(familyRepository, times(1)).deleteAllByCustomerId(1L);
    }

    @Test
    void updateCustomer_notFound_throwsIllegalArgumentException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(999L, baseRequest())
        );

        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void updateCustomer_nicChangedToExistingNic_throwsIllegalArgumentException() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Alice");
        existing.setDob(LocalDate.of(1990, 1, 15));
        existing.setNic("OLD123456789V");

        CustomerRequestDTO request = baseRequest();
        request.setNic("200012345678");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.existsByNic("200012345678")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> customerService.updateCustomer(1L, request)
        );

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void updateCustomer_nicUnchanged_doesNotCheckDuplicate() {
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setName("Alice");
        existing.setDob(LocalDate.of(1990, 1, 15));
        existing.setNic("SAME123456789");

        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Alice Updated");
        request.setDob(LocalDate.of(1990, 1, 15));
        request.setNic("SAME123456789");
        request.setMobiles(Collections.emptyList());
        request.setAddresses(Collections.emptyList());
        request.setFamilyMemberIds(Collections.emptyList());

        Customer updated = new Customer();
        updated.setId(1L);
        updated.setName("Alice Updated");
        updated.setDob(LocalDate.of(1990, 1, 15));
        updated.setNic("SAME123456789");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(any(Customer.class))).thenReturn(updated);
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(updated));

        customerService.updateCustomer(1L, request);

        verify(customerRepository, never()).existsByNic(anyString());
    }

    @Test
    void getCustomerById_success_returnsDTO() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");
        customer.setDob(LocalDate.of(1990, 1, 15));
        customer.setNic("199001512345");

        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getCustomerById_notFound_throwsIllegalArgumentException() {
        when(customerRepository.findByIdWithDetails(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> customerService.getCustomerById(2L));
    }

    @Test
    void getAllCustomers_noNameFilter_callsFindAllSummary() {
        Pageable pageable = PageRequest.of(0, 20);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");
        customer.setDob(LocalDate.of(1990, 1, 15));
        customer.setNic("199001512345");

        Page<Customer> page = new PageImpl<>(Collections.singletonList(customer), pageable, 1);
        when(customerRepository.findAllSummary(pageable)).thenReturn(page);

        Page<CustomerSummaryDTO> result = customerService.getAllCustomers(pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(customerRepository, times(1)).findAllSummary(pageable);
        verify(customerRepository, never()).findAllSummaryByName(anyString(), any(Pageable.class));
    }

    @Test
    void getAllCustomers_withNameFilter_callsFindAllSummaryByName() {
        Pageable pageable = PageRequest.of(0, 20);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setDob(LocalDate.of(1990, 1, 15));
        customer.setNic("199001512345");

        Page<Customer> page = new PageImpl<>(Collections.singletonList(customer), pageable, 1);
        when(customerRepository.findAllSummaryByName("John", pageable)).thenReturn(page);

        Page<CustomerSummaryDTO> result = customerService.getAllCustomers(pageable, "John");

        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getName());
        verify(customerRepository, times(1)).findAllSummaryByName("John", pageable);
        verify(customerRepository, never()).findAllSummary(any(Pageable.class));
    }

    @Test
    void getAllCustomers_blankNameFilter_treatedAsNoFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Alice");
        customer.setDob(LocalDate.of(1990, 1, 15));
        customer.setNic("199001512345");

        Page<Customer> page = new PageImpl<>(Collections.singletonList(customer), pageable, 1);
        when(customerRepository.findAllSummary(pageable)).thenReturn(page);

        Page<CustomerSummaryDTO> result = customerService.getAllCustomers(pageable, "   ");

        assertFalse(result.isEmpty());
        verify(customerRepository, times(1)).findAllSummary(pageable);
        verify(customerRepository, never()).findAllSummaryByName(anyString(), any(Pageable.class));
    }

    private CustomerRequestDTO baseRequest() {
        CustomerRequestDTO dto = new CustomerRequestDTO();
        dto.setName("Alice");
        dto.setDob(LocalDate.of(1990, 1, 15));
        dto.setNic("199001512345");
        dto.setMobiles(new ArrayList<MobileRequestDTO>());
        dto.setAddresses(new ArrayList<AddressRequestDTO>());
        dto.setFamilyMemberIds(new ArrayList<Long>());
        return dto;
    }

    private Customer detailedCustomer(Customer base, City city, Customer familyMember) {
        Customer detailed = new Customer();
        detailed.setId(base.getId());
        detailed.setName(base.getName());
        detailed.setDob(base.getDob());
        detailed.setNic(base.getNic());

        CustomerMobile mobile = new CustomerMobile(detailed, "0771234567");

        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setId(10L);
        customerAddress.setCustomer(detailed);
        customerAddress.setAddressLine1("123 Main Street");
        customerAddress.setAddressLine2("Apartment 4B");
        customerAddress.setCity(city);

        CustomerFamily family = new CustomerFamily(detailed, familyMember);

        Set<CustomerMobile> mobiles = new HashSet<CustomerMobile>(Arrays.asList(mobile));
        Set<CustomerAddress> addresses = new HashSet<CustomerAddress>(Arrays.asList(customerAddress));
        Set<CustomerFamily> families = new HashSet<CustomerFamily>(Arrays.asList(family));

        detailed.setMobiles(mobiles);
        detailed.setAddresses(addresses);
        detailed.setFamilyLinks(families);
        return detailed;
    }
}
