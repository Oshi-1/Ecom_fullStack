package com.ecommerce.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By heading = By.xpath("//h1[normalize-space()='Sign up']");
    private final By fullNameInput = By.name("name");
    private final By emailInput = By.cssSelector("input[name='email']");
    private final By passwordInput = By.cssSelector("input[name='password']");
    private final By passwordToggle = By.cssSelector("button[aria-label='Show password'], button[aria-label='Hide password']");
    private final By createAccountButton = By.xpath("//button[normalize-space()='Create account' or normalize-space()='Creating...']");
    private final By signInLink = By.linkText("Sign in");
    private final By errorBanner = By.cssSelector("p[class*='errorBanner']");
    private final By nameError = By.xpath("//input[@name='name']/following-sibling::small");
    private final By emailError = By.xpath("//input[@name='email']/following-sibling::small");
    private final By passwordError = By.xpath("//input[@name='password']/ancestor::label/small");

    public RegisterPage(WebDriver driver, WebDriverWait wait, String baseUrl) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    public RegisterPage open() {
        driver.get(baseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
        return this;
    }

    public boolean isLoaded() {
        return driver.findElement(heading).isDisplayed()
                && driver.findElement(fullNameInput).isDisplayed()
                && driver.findElement(emailInput).isDisplayed()
                && driver.findElement(passwordInput).isDisplayed()
                && driver.findElement(createAccountButton).isDisplayed()
                && driver.findElement(signInLink).isDisplayed();
    }

    public RegisterPage enterFullName(String name) {
        type(fullNameInput, name);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public RegisterPage submit() {
        driver.findElement(createAccountButton).click();
        return this;
    }

    public RegisterPage togglePasswordVisibility() {
        driver.findElement(passwordToggle).click();
        return this;
    }

    public String passwordInputType() {
        return driver.findElement(passwordInput).getAttribute("type");
    }

    public String emailValidationMessage() {
        return driver.findElement(emailInput).getAttribute("validationMessage");
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String visibleErrorMessage() {
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorBanner));
        return error.getText();
    }

    public String nameFieldError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(nameError)).getText();
    }

    public String emailFieldError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emailError)).getText();
    }

    public String passwordFieldError() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(passwordError)).getText();
    }

    public void waitForDashboardRedirect() {
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    public void clickSignIn() {
        driver.findElement(signInLink).click();
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    private void type(By locator, String value) {
        WebElement element = driver.findElement(locator);
        element.clear();
        element.sendKeys(value);
    }
}
