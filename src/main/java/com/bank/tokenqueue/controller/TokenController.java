package com.bank.tokenqueue.controller;

import com.bank.tokenqueue.model.Token;
import com.bank.tokenqueue.model.TokenStatus;
import com.bank.tokenqueue.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private static final String RECEPTIONIST = "RECEPTIONIST";
    private static final String TELLER = "TELLER";
    private static final String MANAGER = "MANAGER";

    @Autowired
    private TokenService tokenService;

    // Returns a 403 response if the role is not allowed, otherwise null
    private ResponseEntity<String> denyUnless(String role, String... allowed) {
        if (role != null) {
            for (String a : allowed) {
                if (a.equalsIgnoreCase(role)) {
                    return null;
                }
            }
        }
        return ResponseEntity.status(403)
                .body("Role not allowed. Allowed: " + String.join(", ", allowed));
    }

    // CREATE - POST /api/tokens
    @PostMapping
    public ResponseEntity<?> createToken(
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestBody Map<String, String> request) {
        ResponseEntity<String> denied = denyUnless(role, RECEPTIONIST);
        if (denied != null) return denied;

        String customerName = request.get("customerName");
        String serviceType = request.get("serviceType");

        if (customerName == null || customerName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("customerName is required");
        }
        if (serviceType == null || serviceType.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("serviceType is required");
        }

        Token token = tokenService.createToken(customerName, serviceType);
        return ResponseEntity.ok(token);
    }

    // VIEW ALL - GET /api/tokens
    @GetMapping
    public ResponseEntity<?> getAllTokens(
            @RequestHeader(value = "X-Role", required = false) String role) {
        ResponseEntity<String> denied = denyUnless(role, RECEPTIONIST, TELLER, MANAGER);
        if (denied != null) return denied;
        List<Token> tokens = tokenService.getAllTokens();
        return ResponseEntity.ok(tokens);
    }

    // VIEW ONE - GET /api/tokens/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getToken(
            @RequestHeader(value = "X-Role", required = false) String role,
            @PathVariable Long id) {
        ResponseEntity<String> denied = denyUnless(role, RECEPTIONIST, TELLER, MANAGER);
        if (denied != null) return denied;
        return ResponseEntity.ok(tokenService.getTokenById(id));
    }

    // UPDATE STATUS - PUT /api/tokens/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @RequestHeader(value = "X-Role", required = false) String role,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        ResponseEntity<String> denied = denyUnless(role, TELLER);
        if (denied != null) return denied;

        TokenStatus newStatus = TokenStatus.valueOf(request.get("status").toUpperCase());
        return ResponseEntity.ok(tokenService.updateStatus(id, newStatus));
    }

    // SEARCH - GET /api/tokens/search?tokenNumber= or status= or name=
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestParam(required = false) String tokenNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name) {
        ResponseEntity<String> denied = denyUnless(role, RECEPTIONIST, TELLER, MANAGER);
        if (denied != null) return denied;

        if (tokenNumber != null) {
            return ResponseEntity.ok(tokenService.searchByTokenNumber(tokenNumber));
        }
        if (status != null) {
            return ResponseEntity.ok(tokenService.searchByStatus(TokenStatus.valueOf(status.toUpperCase())));
        }
        if (name != null) {
            return ResponseEntity.ok(tokenService.searchByName(name));
        }
        return ResponseEntity.badRequest().body("Provide tokenNumber, status, or name to search");
    }

    // DASHBOARD - GET /api/tokens/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(
            @RequestHeader(value = "X-Role", required = false) String role) {
        ResponseEntity<String> denied = denyUnless(role, MANAGER);
        if (denied != null) return denied;
        return ResponseEntity.ok(tokenService.getDashboardSummary());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleBadTransition(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadValue(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid value: " + e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }
}