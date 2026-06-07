package com.ecommerce.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By heading = By.xpath("//h1[normalize-space()='Sign in']");
    private final By emailInput = By.id("login-email");
    private final By passwordInput = By.id("login-password");
    private final By passwordToggle = By.cssSelector("button[aria-label='Show password'], button[aria-label='Hide password']");
    private final By signInButton = By.xpath("//button[normalize-space()='Sign in' or normalize-space()='Signing in...']");
    private final By createAccountLink = By.linkText("Create account");
    private final By forgotPasswordLink = By.linkText("Forgot password?");
    private final By errorBanner = By.cssSelector("p[class*='errorBanner']");
    private final By emailError = By.xpath("//input[@id='login-email']/following-sibling::span");
    private final By passwordError = By.xpath("//input[@id='login-password']/ancestor::div[contains(@class,'field')]/span");

    public LoginPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    public LoginPage open() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        return this;
    }

    public boolean isLoaded() {
        return driver.findElement(heading).isDisplayed()
                && driver.findElement(emailInput).isDisplayed()
                && driver.findElement(passwordInput).isDisplayed()
                && driver.findElement(signInButton).isDisplayed()
                && driver.findElement(createAccountLink).isDisplayed()
                && driver.findElement(forgotPasswordLink).isDisplayed();
    }

    public LoginPage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public LoginPage submit() {
        driver.findElement(signInButton).click();
        return this;
    }

    public LoginPage loginAs(String email, String password) {
        return enterEmail(email).enterPassword(password).submit();
    }

    public LoginPage togglePasswordVisibility() {
        driver.findElement(passwordToggle).click();
        return this;
    }

    public String passwordInputType() {
        return driver.findElement(passwordInput).getAttribute("type");
    }

    public String emailValidationMessage() {
        return driver.findElement(emailInput).getAttribute("validationMessage");
    }

    public String visibleErrorMessage() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorBanner));
        return error.getText();
    }

    public String emailFieldError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emailError)).getText();
    }

    public String passwordFieldError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordError)).getText();
    }

    public LoginPage clickCreateAccount() {
        driver.findElement(createAccountLink).click();
        wait.until(ExpectedConditions.urlContains("/register"));
        return this;
    }

    public LoginPage clickForgotPassword() {
        driver.findElement(forgotPasswordLink).click();
        wait.until(ExpectedConditions.urlContains("/forgot-password"));
        return this;
    }

    public void waitForDashboardRedirect() {
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private void type(By locator, String value) {
        WebElement element = driver.findElement(locator);
        element.clear();
        element.sendKeys(value);
    }
}
