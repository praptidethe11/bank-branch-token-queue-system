package com.bank.tokenqueue.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenQueueJourneysTest extends BaseUiTest {

    @Test
    @DisplayName("TC1: Receptionist creates a token and it appears as WAITING")
    void receptionistCreatesToken() {
        String name = uniqueName("Create");
        page.selectRole("RECEPTIONIST");
        page.createToken(name, "LOAN");

        String message = page.waitForMessageContaining("created");
        assertTrue(message.contains("T-"), "Message should show the new token number but was: " + message);

        page.waitForRow(name);
        assertEquals("WAITING", page.statusOf(name));
        assertTrue(page.tokenNumberOf(name).startsWith("T-"));
    }

    @Test
    @DisplayName("TC2: Teller cannot create a token")
    void tellerCannotCreateToken() {
        String name = uniqueName("Blocked");
        page.selectRole("TELLER");
        page.createToken(name, "GENERAL");

        String message = page.waitForMessageContaining("Role not allowed");
        assertTrue(message.contains("RECEPTIONIST"), "Message should name the allowed role but was: " + message);
        assertFalse(page.hasRow(name), "No token should be created for a Teller");
    }

    @Test
    @DisplayName("TC3: Teller moves a token through the status workflow")
    void tellerMovesTokenThroughWorkflow() {
        String name = uniqueName("Flow");
        page.selectRole("RECEPTIONIST");
        page.createToken(name, "GENERAL");
        page.waitForRow(name);

        page.selectRole("TELLER");
        page.clickAction(name, "serving");
        page.waitForStatus(name, "SERVING");
        assertEquals("SERVING", page.statusOf(name));

        page.clickAction(name, "completed");
        page.waitForStatus(name, "COMPLETED");
        assertEquals("COMPLETED", page.statusOf(name));

        page.clickAction(name, "serving");
        String message = page.waitForMessageContaining("Cannot change status");
        assertTrue(message.contains("COMPLETED"), "Message should mention COMPLETED but was: " + message);
        assertEquals("COMPLETED", page.statusOf(name));
    }

    @Test
    @DisplayName("TC4: Search by token number, name and status")
    void searchFindsTokens() {
        String name = uniqueName("Search");
        page.selectRole("RECEPTIONIST");
        page.createToken(name, "ADVISORY");
        page.waitForRow(name);
        String number = page.tokenNumberOf(name);

        page.search("tokenNumber", number);
        page.waitForMessageContaining("Search by tokenNumber " + number);
        assertEquals(1, page.rowCount());
        assertTrue(page.hasRow(name));

        page.search("name", name);
        page.waitForMessageContaining("Search by name " + name);
        assertEquals(1, page.rowCount());

        page.search("status", "WAITING");
        page.waitForMessageContaining("Search by status WAITING");
        assertTrue(page.hasRow(name), "New token should be listed under WAITING");

        page.search("status", "CANCELLED");
        page.waitForMessageContaining("Search by status CANCELLED");
        assertFalse(page.hasRow(name), "New token must not be listed under CANCELLED");

        page.search("tokenNumber", "T-99999");
        page.waitForMessageContaining("not found");
        assertEquals(0, page.rowCount());
    }

    @Test
    @DisplayName("TC5: Manager dashboard total grows after a new token, Teller is refused")
    void managerDashboardShowsTotal() {
        page.selectRole("MANAGER");
        page.refreshDashboard();
        int before = page.dashboardTotal();

        String name = uniqueName("Dash");
        page.selectRole("RECEPTIONIST");
        page.createToken(name, "GENERAL");
        page.waitForRow(name);

        page.selectRole("MANAGER");
        page.refreshDashboard();
        int after = page.dashboardTotal();
        assertEquals(before + 1, after, "TOTAL should increase by one after a new token");

        page.selectRole("TELLER");
        page.refreshDashboard();
        String message = page.waitForDashboardMessageContaining("Role not allowed");
        assertTrue(message.contains("MANAGER"), "Message should name the allowed role but was: " + message);
    }
}