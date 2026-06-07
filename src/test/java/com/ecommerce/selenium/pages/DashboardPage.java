package com.ecommerce.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By dashboardTitle = By.cssSelector("main h1");
    private final By logoutButton = By.xpath("//button[.//span[normalize-space()='Logout']]");
    private final By browseStoreButton = By.xpath("//button[.//span[normalize-space()='Browse Store']]");
    private final By profileButton = By.xpath("//button[.//span[normalize-space()='Profile']]");

    public DashboardPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public DashboardPage waitUntilLoaded() {
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(dashboardTitle));
        return this;
    }

    public boolean isLoaded() {
        return driver.findElement(dashboardTitle).isDisplayed()
                && driver.findElement(logoutButton).isDisplayed()
                && driver.findElement(profileButton).isDisplayed();
    }

    public DashboardPage openProducts() {
        driver.findElement(browseStoreButton).click();
        wait.until(ExpectedConditions.urlContains("/products"));
        return this;
    }

    public DashboardPage logout() {
        driver.findElement(logoutButton).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        return this;
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
