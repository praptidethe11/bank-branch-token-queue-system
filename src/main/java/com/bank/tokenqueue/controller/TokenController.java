package com.bank.tokenqueue.controller;

import com.bank.tokenqueue.model.Token;
import com.bank.tokenqueue.model.TokenStatus;
import com.bank.tokenqueue.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    // CREATE - POST /api/tokens
    @PostMapping
    public ResponseEntity<Token> createToken(@RequestBody Map<String, String> request) {
        Token token = tokenService.createToken(
                request.get("customerName"),
                request.get("serviceType")
        );
        return ResponseEntity.ok(token);
    }

    // VIEW ALL - GET /api/tokens
    @GetMapping
    public ResponseEntity<List<Token>> getAllTokens() {
        return ResponseEntity.ok(tokenService.getAllTokens());
    }

    // VIEW ONE - GET /api/tokens/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Token> getToken(@PathVariable Long id) {
        return ResponseEntity.ok(tokenService.getTokenById(id));
    }

    // UPDATE STATUS - PUT /api/tokens/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<Token> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        TokenStatus newStatus = TokenStatus.valueOf(request.get("status").toUpperCase());
        Token updated = tokenService.updateStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }

    // SEARCH - GET /api/tokens/search?tokenNumber=T-101
    //          GET /api/tokens/search?status=WAITING
    //          GET /api/tokens/search?name=John
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String tokenNumber,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name) {

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
    public ResponseEntity<Map<TokenStatus, Long>> dashboard() {
        return ResponseEntity.ok(tokenService.getDashboardSummary());
    }
}
