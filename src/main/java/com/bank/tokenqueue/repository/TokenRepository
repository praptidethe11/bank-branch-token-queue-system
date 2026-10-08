package com.bank.tokenqueue.repository;

import com.bank.tokenqueue.model.Token;
import com.bank.tokenqueue.model.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenNumber(String tokenNumber);

    List<Token> findByStatus(TokenStatus status);

    List<Token> findByCustomerNameContainingIgnoreCase(String name);

    long countByStatus(TokenStatus status);
}
