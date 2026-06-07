package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.DashboardPage;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.pages.RegisterPage;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginLogoutTest extends BaseTest {

    private static final String TEST_USER_NAME = "Selenium Login User";
    private static final String TEST_USER_PASSWORD = "Password@123";

    @Test(priority = 1)
    public void loginPageShouldDisplayExpectedFields() {
        LoginPage loginPage = new LoginPage(driver, wait, frontendBaseUrl).open();

        Assert.assertTrue(loginPage.isLoaded(), "Login page fields and navigation links should be visible.");
    }

    @Test(priority = 2)
    public void passwordToggleShouldSwitchLoginPasswordVisibility() {
        LoginPage loginPage = new LoginPage(driver, wait, frontendBaseUrl).open();

        Assert.assertEquals(loginPage.passwordInputType(), "password");

        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.passwordInputType(), "text");

        loginPage.togglePasswordVisibility();
        Assert.assertEquals(loginPage.passwordInputType(), "password");
    }

    @Test(priority = 3)
    public void invalidLoginEmailShouldShowBrowserValidationMessage() {
        LoginPage loginPage = new LoginPage(driver, wait, frontendBaseUrl).open();

        loginPage
                .enterEmail("not-an-email")
                .enterPassword(TEST_USER_PASSWORD)
                .submit();

        Assert.assertFalse(loginPage.emailValidationMessage().isBlank(), "Invalid email should trigger browser validation.");
        Assert.assertTrue(loginPage.currentUrl().contains("/login"), "Invalid email should keep user on login page.");
    }

    @Test(priority = 4)
    public void invalidCredentialsShouldShowErrorMessage() {
        startBackendIfNeeded();
        LoginPage loginPage = new LoginPage(driver, wait, frontendBaseUrl).open();

        loginPage.loginAs("missing.user." + Instant.now().toEpochMilli() + "@example.com", "wrong-password");

        Assert.assertEquals(loginPage.visibleErrorMessage(), "Invalid email or password");
        Assert.assertTrue(loginPage.currentUrl().contains("/login"), "Invalid credentials should not open dashboard.");
    }

    @Test(priority = 5)
    public void validUserShouldLoginAndLogoutSuccessfully() {
        startBackendIfNeeded();
        String email = "selenium.login." + Instant.now().toEpochMilli() + "@example.com";

        new RegisterPage(driver, wait, frontendBaseUrl)
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        DashboardPage dashboardPage = new DashboardPage(driver, wait).waitUntilLoaded();
        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard should load after registration.");

        dashboardPage.logout();
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should return user to login page.");

        new LoginPage(driver, wait, frontendBaseUrl)
                .loginAs(email, TEST_USER_PASSWORD)
                .waitForDashboardRedirect();

        Assert.assertTrue(new DashboardPage(driver, wait).waitUntilLoaded().isLoaded(),
                "Valid credentials should open dashboard.");
    }
}
