package com.bank.tokenqueue.service;

import com.bank.tokenqueue.model.Token;
import com.bank.tokenqueue.model.TokenStatus;
import com.bank.tokenqueue.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

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
                .orElseThrow(() -> new NoSuchElementException("Token not found with id: " + id));
    }

    public Token updateStatus(Long id, TokenStatus newStatus) {
        Token token = getTokenById(id);
        if (!isValidTransition(token.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Cannot change status from " + token.getStatus() + " to " + newStatus);
        }
        token.setStatus(newStatus);
        return tokenRepository.save(token);
    }

    private boolean isValidTransition(TokenStatus from, TokenStatus to) {
        return switch (from) {
            case WAITING -> to == TokenStatus.SERVING || to == TokenStatus.CANCELLED;
            case SERVING -> to == TokenStatus.COMPLETED || to == TokenStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    public List<Token> searchByStatus(TokenStatus status) {
        return tokenRepository.findByStatus(status);
    }

    public List<Token> searchByName(String name) {
        return tokenRepository.findByCustomerNameContainingIgnoreCase(name);
    }

    public Token searchByTokenNumber(String tokenNumber) {
        return tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new NoSuchElementException("Token not found: " + tokenNumber));
    }

    public Map<String, Object> getDashboardSummary() {        
        Map<String, Object> summary = new LinkedHashMap<>();
        long total = 0;
        for (TokenStatus status : TokenStatus.values()) {
            long count = tokenRepository.countByStatus(status);
            summary.put(status.name(), count);
            total += count;
        }
        summary.put("TOTAL", total);
        return summary;
    }
}