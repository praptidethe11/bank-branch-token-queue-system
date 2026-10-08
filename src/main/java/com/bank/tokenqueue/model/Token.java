package com.bank.tokenqueue.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenNumber;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String serviceType; // e.g. GENERAL, LOAN, ADVISORY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Token() {
        this.createdAt = LocalDateTime.now();
        this.status = TokenStatus.WAITING;
    }

    public Token(String tokenNumber, String customerName, String serviceType) {
        this();
        this.tokenNumber = tokenNumber;
        this.customerName = customerName;
        this.serviceType = serviceType;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public void setTokenNumber(String tokenNumber) {
        this.tokenNumber = tokenNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public TokenStatus getStatus() {
        return status;
    }

    public void setStatus(TokenStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
