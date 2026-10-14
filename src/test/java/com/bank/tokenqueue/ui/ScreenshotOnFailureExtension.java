package com.bank.tokenqueue.ui;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Runs right after a test method and before the browser is closed.
 * If the test failed, a PNG of the browser window is saved under target/screenshots.
 */
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Object instance = context.getRequiredTestInstance();
        if (!(instance instanceof BaseUiTest)) {
            return;
        }
        WebDriver driver = ((BaseUiTest) instance).getDriver();
        if (!(driver instanceof TakesScreenshot)) {
            return;
        }
        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String fileName = context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getRequiredTestMethod().getName() + "_" + time + ".png";
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path file = dir.resolve(fileName);
            Files.write(file, png);
            System.out.println("Failure screenshot saved: " + file.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Could not save failure screenshot: " + e.getMessage());
        }
    }
}