package com.bank.tokenqueue.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfSystemProperty(named = "demoFailure", matches = "true")
class FailureDemoTest extends BaseUiTest {

    @Test
    @DisplayName("DEMO: deliberate failure to prove the screenshot mechanism")
    void deliberateFailure() {
        assertEquals("This title does not exist", driver.getTitle());
    }
}