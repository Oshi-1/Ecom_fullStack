package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.DashboardPage;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.pages.RegisterPage;
import java.time.Instant;
import org.openqa.selenium.JavascriptExecutor;
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

    @Test(priority = 6)
    public void logoutShouldClearAuthSessionAndProtectPrivateRoutes() {
        startBackendIfNeeded();
        String email = "selenium.logout." + Instant.now().toEpochMilli() + "@example.com";

        registerAndOpenDashboard(email);

        Assert.assertFalse(localStorageValue("token").isBlank(), "Token should exist before logout.");
        Assert.assertFalse(localStorageValue("user").isBlank(), "User details should exist before logout.");

        new DashboardPage(driver, wait).logout();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should redirect to login page.");
        Assert.assertEquals(localStorageValue("token"), "", "Logout should remove JWT token from local storage.");
        Assert.assertEquals(localStorageValue("user"), "", "Logout should remove user details from local storage.");

        driver.get(frontendBaseUrl + "/dashboard");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout, direct dashboard access should redirect back to login.");
    }

    @Test(priority = 7)
    public void validSessionShouldRemainAvailableAfterRefreshAndDirectNavigation() {
        startBackendIfNeeded();
        String email = "selenium.session." + Instant.now().toEpochMilli() + "@example.com";

        registerAndOpenDashboard(email);

        String tokenBeforeRefresh = localStorageValue("token");
        Assert.assertFalse(tokenBeforeRefresh.isBlank(), "Token should be stored after successful registration.");

        driver.navigate().refresh();
        Assert.assertTrue(new DashboardPage(driver, wait).waitUntilLoaded().isLoaded(),
                "Authenticated dashboard session should survive page refresh.");
        Assert.assertEquals(localStorageValue("token"), tokenBeforeRefresh,
                "Refresh should not clear or replace the current auth token.");

        driver.get(frontendBaseUrl + "/products");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/products"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/products"),
                "Authenticated user should access protected product page by direct URL.");
        Assert.assertEquals(localStorageValue("token"), tokenBeforeRefresh,
                "Direct navigation within protected pages should keep the same auth token.");
    }

    private DashboardPage registerAndOpenDashboard(String email) {
        new RegisterPage(driver, wait, frontendBaseUrl)
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        return new DashboardPage(driver, wait).waitUntilLoaded();
    }

    private String localStorageValue(String key) {
        Object value = ((JavascriptExecutor) driver).executeScript(
                "return window.localStorage.getItem(arguments[0]) || '';", key);
        return String.valueOf(value);
    }
}
