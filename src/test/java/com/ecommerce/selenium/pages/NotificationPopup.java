package com.ecommerce.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NotificationPopup {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By popup = By.cssSelector("[data-testid='app-notification']");

    public NotificationPopup(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public String visibleMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(popup)).getText();
    }

    public boolean isVisible() {
        return driver.findElement(popup).isDisplayed();
    }
}
