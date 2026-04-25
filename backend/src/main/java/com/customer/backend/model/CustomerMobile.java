package com.customer.backend.model;


import javax.persistence.*;

@Entity
@Table(name = "customer_mobile")
public class CustomerMobile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "mobile", nullable = false, length = 20)
    private String mobile;

    public CustomerMobile() {}

    public CustomerMobile(Customer customer, String mobile) {
        this.customer = customer;
        this.mobile = mobile;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
}