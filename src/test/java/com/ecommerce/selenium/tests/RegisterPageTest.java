package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.RegisterPage;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RegisterPageTest extends BaseTest {

    private static final String TEST_USER_NAME = "Selenium User";
    private static final String TEST_USER_PASSWORD = "Password@123";

    @Test(priority = 1)
    public void openChromeAndRegisterUserAutomatically() {
        startBackendIfNeeded();
        String email = "selenium.user." + Instant.now().toEpochMilli() + "@example.com";

        new RegisterPage(driver, wait, frontendBaseUrl)
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"), "User should register and redirect to dashboard.");
    }

    @Test(priority = 2)
    public void registrationPageShouldDisplayExpectedFields() {
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();

        Assert.assertTrue(registerPage.isLoaded(), "Registration page fields and actions should be visible.");
    }

    @Test(priority = 3)
    public void passwordToggleShouldSwitchPasswordVisibility() {
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();

        Assert.assertEquals(registerPage.passwordInputType(), "password");

        registerPage.togglePasswordVisibility();
        Assert.assertEquals(registerPage.passwordInputType(), "text");

        registerPage.togglePasswordVisibility();
        Assert.assertEquals(registerPage.passwordInputType(), "password");
    }

    @Test(priority = 4)
    public void invalidEmailShouldShowBrowserValidationMessage() {
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();

        registerPage
                .enterFullName(TEST_USER_NAME)
                .enterEmail("invalid-email")
                .enterPassword(TEST_USER_PASSWORD)
                .submit();

        Assert.assertFalse(registerPage.emailValidationMessage().isBlank(), "Invalid email should trigger HTML5 validation.");
        Assert.assertTrue(registerPage.currentUrl().contains("/register"), "User should stay on registration page.");
    }

    @Test(priority = 5)
    public void emptyRegistrationShouldShowRequiredFieldErrors() {
        startBackendIfNeeded();
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();

        registerPage.submit();

        Assert.assertEquals(registerPage.nameFieldError(), "Name is required");
        Assert.assertEquals(registerPage.emailFieldError(), "Email is required");
        Assert.assertEquals(registerPage.passwordFieldError(), "Password is required");
        Assert.assertTrue(registerPage.currentUrl().contains("/register"), "Validation errors should keep user on register page.");
    }

    @Test(priority = 6)
    public void shortPasswordShouldShowValidationError() {
        startBackendIfNeeded();
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();

        registerPage
                .enterFullName(TEST_USER_NAME)
                .enterEmail("short.password." + Instant.now().toEpochMilli() + "@example.com")
                .enterPassword("123")
                .submit();

        Assert.assertEquals(registerPage.passwordFieldError(), "Password must be at least 6 characters");
        Assert.assertTrue(registerPage.currentUrl().contains("/register"), "Short password should keep user on register page.");
    }

    @Test(priority = 7)
    public void validRegistrationShouldRedirectToDashboard() {
        startBackendIfNeeded();
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();
        String email = "selenium.user." + Instant.now().toEpochMilli() + "@example.com";

        registerPage
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        Assert.assertTrue(registerPage.currentUrl().contains("/dashboard"), "Successful registration should open dashboard.");
    }

    @Test(priority = 8)
    public void signInLinkShouldOpenLoginPage() {
        RegisterPage registerPage = new RegisterPage(driver, wait, frontendBaseUrl).open();

        registerPage.clickSignIn();

        Assert.assertTrue(registerPage.currentUrl().contains("/login"), "Sign in link should navigate to login page.");
    }
}
