package com.bank.tokenqueue.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Tag("ui")
@ExtendWith(ScreenshotOnFailureExtension.class)
public abstract class BaseUiTest {

    protected WebDriver driver;
    protected TokenPage page;

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1366,900", "--remote-allowed-origins=*");
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
        }
        driver = new ChromeDriver(options);
        String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8082/");
        page = new TokenPage(driver, baseUrl);
        page.open();
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected static String uniqueName(String prefix) {
        return prefix + " " + System.currentTimeMillis();
    }
}