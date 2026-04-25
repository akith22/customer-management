package com.customer.backend.model;

import javax.persistence.*;

@Entity
@Table(name = "customer_family")
public class CustomerFamily {

    @EmbeddedId
    private CustomerFamilyId id = new CustomerFamilyId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("customerId")
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("familyMemberId")
    @JoinColumn(name = "family_member_id")
    private Customer familyMember;

    public CustomerFamily() {}

    public CustomerFamily(Customer customer, Customer familyMember) {
        this.customer = customer;
        this.familyMember = familyMember;
        this.id = new CustomerFamilyId(customer.getId(), familyMember.getId());
    }

    public CustomerFamilyId getId() { return id; }
    public void setId(CustomerFamilyId id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Customer getFamilyMember() { return familyMember; }
    public void setFamilyMember(Customer familyMember) { this.familyMember = familyMember; }

    // ── Embedded composite key ──────────────────────────────────────
    @Embeddable
    public static class CustomerFamilyId implements java.io.Serializable {

        @Column(name = "customer_id")
        private Long customerId;

        @Column(name = "family_member_id")
        private Long familyMemberId;

        public CustomerFamilyId() {}

        public CustomerFamilyId(Long customerId, Long familyMemberId) {
            this.customerId = customerId;
            this.familyMemberId = familyMemberId;
        }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public Long getFamilyMemberId() { return familyMemberId; }
        public void setFamilyMemberId(Long familyMemberId) { this.familyMemberId = familyMemberId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CustomerFamilyId)) return false;
            CustomerFamilyId that = (CustomerFamilyId) o;
            return java.util.Objects.equals(customerId, that.customerId)
                    && java.util.Objects.equals(familyMemberId, that.familyMemberId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(customerId, familyMemberId);
        }
    }
}