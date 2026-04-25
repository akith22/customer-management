package com.customer.backend.service;

import com.customer.backend.dto.request.AddressRequestDTO;
import com.customer.backend.dto.request.CustomerRequestDTO;
import com.customer.backend.dto.request.MobileRequestDTO;
import com.customer.backend.dto.response.AddressResponseDTO;
import com.customer.backend.dto.response.CityResponseDTO;
import com.customer.backend.dto.response.CustomerResponseDTO;
import com.customer.backend.dto.response.CustomerSummaryDTO;
import com.customer.backend.model.*;
import com.customer.backend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CustomerService {

    private final CustomerRepository        customerRepository;
    private final CustomerMobileRepository  mobileRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerFamilyRepository  familyRepository;
    private final CityRepository            cityRepository;

    public CustomerService(CustomerRepository customerRepository,
                           CustomerMobileRepository mobileRepository,
                           CustomerAddressRepository addressRepository,
                           CustomerFamilyRepository familyRepository,
                           CityRepository cityRepository) {
        this.customerRepository = customerRepository;
        this.mobileRepository   = mobileRepository;
        this.addressRepository  = addressRepository;
        this.familyRepository   = familyRepository;
        this.cityRepository     = cityRepository;
    }

    // ── CREATE ───────────────────────────────────────────────────────

    @Transactional
    public CustomerResponseDTO createCustomer(CustomerRequestDTO dto) {

        if (customerRepository.existsByNic(dto.getNic())) {
            throw new IllegalArgumentException(
                    "A customer with NIC '" + dto.getNic() + "' already exists.");
        }

        Customer customer = new Customer();
        customer.setName(dto.getName());
        customer.setDob(dto.getDob());
        customer.setNic(dto.getNic());

        customer = customerRepository.save(customer);

        persistMobiles(customer, dto.getMobiles());
        persistAddresses(customer, dto.getAddresses());
        persistFamilyLinks(customer, dto.getFamilyMemberIds());

        return toFullDTO(customerRepository.findByIdWithDetails(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found after save.")));
    }

    // ── UPDATE ───────────────────────────────────────────────────────

    @Transactional
    public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO dto) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found with id: " + id));

        if (!customer.getNic().equals(dto.getNic())
                && customerRepository.existsByNic(dto.getNic())) {
            throw new IllegalArgumentException(
                    "A customer with NIC '" + dto.getNic() + "' already exists.");
        }

        customer.setName(dto.getName());
        customer.setDob(dto.getDob());
        customer.setNic(dto.getNic());
        customerRepository.save(customer);

        mobileRepository.deleteAllByCustomerId(id);
        persistMobiles(customer, dto.getMobiles());

        addressRepository.deleteAllByCustomerId(id);
        persistAddresses(customer, dto.getAddresses());

        familyRepository.deleteAllByCustomerId(id);
        persistFamilyLinks(customer, dto.getFamilyMemberIds());

        return toFullDTO(customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Customer not found after update.")));
    }

    // ── GET BY ID ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Customer not found with id: " + id));
        return toFullDTO(customer);
    }

    // ── GET ALL (paginated table view) ───────────────────────────────

    @Transactional(readOnly = true)
    public Page<CustomerSummaryDTO> getAllCustomers(Pageable pageable) {
        return customerRepository.findAllSummary(pageable)
                .map(c -> new CustomerSummaryDTO(
                        c.getId(),
                        c.getName(),
                        c.getDob(),
                        c.getNic(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()
                ));
    }

    // ── PRIVATE HELPERS ──────────────────────────────────────────────

    private void persistMobiles(Customer customer, List<MobileRequestDTO> mobileDTOs) {
        if (mobileDTOs == null || mobileDTOs.isEmpty()) return;

        Set<CustomerMobile> mobiles = new HashSet<>();
        for (MobileRequestDTO m : mobileDTOs) {
            if (m.getMobile() != null && !m.getMobile().trim().isEmpty()) {
                mobiles.add(new CustomerMobile(customer, m.getMobile().trim()));
            }
        }

        if (!mobiles.isEmpty()) {
            mobileRepository.saveAll(mobiles);
        }
    }

    private void persistAddresses(Customer customer, List<AddressRequestDTO> addressDTOs) {
        if (addressDTOs == null || addressDTOs.isEmpty()) return;

        Set<CustomerAddress> addresses = new HashSet<>();

        for (AddressRequestDTO dto : addressDTOs) {
            if (dto.getAddressLine1() == null
                    || dto.getAddressLine1().trim().isEmpty()) continue;

            City city = cityRepository.findById(dto.getCityId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "City not found with id: " + dto.getCityId()));

            CustomerAddress address = new CustomerAddress();
            address.setCustomer(customer);
            address.setAddressLine1(dto.getAddressLine1().trim());
            address.setAddressLine2(dto.getAddressLine2() != null
                    ? dto.getAddressLine2().trim() : null);
            address.setCity(city);

            addresses.add(address);
        }

        if (!addresses.isEmpty()) {
            addressRepository.saveAll(addresses);
        }
    }

    private void persistFamilyLinks(Customer customer, List<Long> familyMemberIds) {
        if (familyMemberIds == null || familyMemberIds.isEmpty()) return;

        List<CustomerFamily> links = new ArrayList<>();

        for (Long memberId : familyMemberIds) {
            if (memberId.equals(customer.getId())) {
                throw new IllegalArgumentException(
                        "A customer cannot be their own family member.");
            }

            Customer member = customerRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Family member customer not found with id: " + memberId));

            links.add(new CustomerFamily(customer, member));
        }

        if (!links.isEmpty()) {
            familyRepository.saveAll(links);
        }
    }

    // ── MAPPING ──────────────────────────────────────────────────────

    private CustomerResponseDTO toFullDTO(Customer c) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDob(c.getDob());
        dto.setNic(c.getNic());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        // Mobiles
        List<String> mobiles = new ArrayList<>();
        for (CustomerMobile m : c.getMobiles()) {
            mobiles.add(m.getMobile());
        }
        dto.setMobiles(mobiles);

        // Addresses
        List<AddressResponseDTO> addresses = new ArrayList<>();
        for (CustomerAddress a : c.getAddresses()) {
            addresses.add(toAddressDTO(a));
        }
        dto.setAddresses(addresses);

        // Family members
        List<CustomerResponseDTO.FamilyMemberDTO> familyMembers = new ArrayList<>();
        for (CustomerFamily fl : c.getFamilyLinks()) {
            familyMembers.add(new CustomerResponseDTO.FamilyMemberDTO(
                    fl.getFamilyMember().getId(),
                    fl.getFamilyMember().getName(),
                    fl.getFamilyMember().getNic()
            ));
        }
        dto.setFamilyMembers(familyMembers);

        return dto;
    }

    private AddressResponseDTO toAddressDTO(CustomerAddress a) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(a.getId());
        dto.setAddressLine1(a.getAddressLine1());
        dto.setAddressLine2(a.getAddressLine2());

        CityResponseDTO cityDTO = new CityResponseDTO(
                a.getCity().getId(),
                a.getCity().getName(),
                a.getCity().getCountry().getName(),
                a.getCity().getCountry().getCode()
        );
        dto.setCity(cityDTO);

        return dto;
    }
}