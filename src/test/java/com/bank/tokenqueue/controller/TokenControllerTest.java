package com.bank.tokenqueue.controller;

import com.bank.tokenqueue.model.Token;
import com.bank.tokenqueue.model.TokenStatus;
import com.bank.tokenqueue.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TokenController.class)
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @Test
    void updateStatus_missingStatusField_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/tokens/1/status")
                        .header("X-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("status is required"));
    }

    @Test
    void updateStatus_blankStatusField_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/tokens/1/status")
                        .header("X-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("status is required"));
    }

    @Test
    void updateStatus_invalidStatusValue_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/tokens/1/status")
                        .header("X-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"UNKNOWN_STATUS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_validStatus_returnsOk() throws Exception {
        Token token = new Token("T-101", "Alice", "GENERAL");
        token.setStatus(TokenStatus.SERVING);
        when(tokenService.updateStatus(eq(1L), eq(TokenStatus.SERVING))).thenReturn(token);

        mockMvc.perform(put("/api/tokens/1/status")
                        .header("X-Role", "TELLER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SERVING\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_unauthorizedRole_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/tokens/1/status")
                        .header("X-Role", "RECEPTIONIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SERVING\"}"))
                .andExpect(status().isForbidden());
    }
}

