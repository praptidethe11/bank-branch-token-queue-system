package com.bank.tokenqueue.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TokenPage {

    private final WebDriver driver;
    private final String baseUrl;
    private final WebDriverWait wait;

    public TokenPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open() {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createBtn")));
    }

    public void selectRole(String role) {
        new Select(driver.findElement(By.id("role"))).selectByValue(role);
    }

    public void createToken(String name, String service) {
        WebElement nameField = driver.findElement(By.id("customerName"));
        nameField.clear();
        nameField.sendKeys(name);
        new Select(driver.findElement(By.id("serviceType"))).selectByValue(service);
        driver.findElement(By.id("createBtn")).click();
    }

    public String waitForMessageContaining(String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("message"), text));
        return driver.findElement(By.id("message")).getText();
    }

    public String waitForDashboardMessageContaining(String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("dashboardMessage"), text));
        return driver.findElement(By.id("dashboardMessage")).getText();
    }

    private By rowLocator(String customerName) {
        return By.xpath("//tbody[@id='tokenBody']/tr[td[contains(@class,'name') "
                + "and normalize-space()='" + customerName + "']]");
    }

    public WebElement waitForRow(String customerName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(rowLocator(customerName)));
    }

    public boolean hasRow(String customerName) {
        return !driver.findElements(rowLocator(customerName)).isEmpty();
    }

    public int rowCount() {
        return driver.findElements(By.cssSelector("#tokenBody tr")).size();
    }

    public String tokenNumberOf(String customerName) {
        return driver.findElement(rowLocator(customerName)).findElement(By.className("number")).getText();
    }

    public String statusOf(String customerName) {
        return driver.findElement(rowLocator(customerName)).findElement(By.className("status")).getText();
    }

    private String statusOfSafe(String customerName) {
        List<WebElement> rows = driver.findElements(rowLocator(customerName));
        if (rows.isEmpty()) {
            return "";
        }
        try {
            return rows.get(0).findElement(By.className("status")).getText();
        } catch (StaleElementReferenceException e) {
            return "";
        }
    }

    public void waitForStatus(String customerName, String status) {
        wait.until(d -> statusOfSafe(customerName).equals(status));
    }

    /** action is one of: serving, completed, cancelled */
    public void clickAction(String customerName, String action) {
        driver.findElement(rowLocator(customerName)).findElement(By.cssSelector(".btn-" + action)).click();
    }

    /** by is one of: tokenNumber, name, status */
    public void search(String by, String value) {
        new Select(driver.findElement(By.id("searchBy"))).selectByValue(by);
        WebElement input = driver.findElement(By.id("searchInput"));
        input.clear();
        input.sendKeys(value);
        driver.findElement(By.id("searchBtn")).click();
    }

    public void refreshDashboard() {
        driver.findElement(By.id("refreshDashboard")).click();
    }

    public int dashboardTotal() {
        wait.until(d -> !d.findElement(By.id("countTotal")).getText().trim().equals("-")
                && !d.findElement(By.id("countTotal")).getText().trim().isEmpty());
        return Integer.parseInt(driver.findElement(By.id("countTotal")).getText().trim());
    }
}