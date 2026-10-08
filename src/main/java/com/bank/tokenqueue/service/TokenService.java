package com.bank.tokenqueue.service;

import com.bank.tokenqueue.model.Token;
import com.bank.tokenqueue.model.TokenStatus;
import com.bank.tokenqueue.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    // simple in-memory counter for generating readable token numbers like T-101
    private final AtomicInteger counter = new AtomicInteger(100);

    public Token createToken(String customerName, String serviceType) {
        String tokenNumber = "T-" + counter.incrementAndGet();
        Token token = new Token(tokenNumber, customerName, serviceType);
        return tokenRepository.save(token);
    }

    public List<Token> getAllTokens() {
        return tokenRepository.findAll();
    }

    public Token getTokenById(Long id) {
        return tokenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Token not found with id: " + id));
    }

    public Token updateStatus(Long id, TokenStatus newStatus) {
        Token token = getTokenById(id);
        token.setStatus(newStatus);
        return tokenRepository.save(token);
    }

    public List<Token> searchByStatus(TokenStatus status) {
        return tokenRepository.findByStatus(status);
    }

    public List<Token> searchByName(String name) {
        return tokenRepository.findByCustomerNameContainingIgnoreCase(name);
    }

    public Token searchByTokenNumber(String tokenNumber) {
        return tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token not found: " + tokenNumber));
    }

    public Map<TokenStatus, Long> getDashboardSummary() {
        Map<TokenStatus, Long> summary = new EnumMap<>(TokenStatus.class);
        for (TokenStatus status : TokenStatus.values()) {
            summary.put(status, tokenRepository.countByStatus(status));
        }
        return summary;
    }
}
