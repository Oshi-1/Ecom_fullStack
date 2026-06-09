package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.DashboardPage;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.utils.LoginTestUtils;
import java.time.Instant;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginLogoutTest extends BaseTest {

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
                .enterPassword(LoginTestUtils.TEST_USER_PASSWORD)
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
        LoginTestUtils loginUtils = new LoginTestUtils(driver, wait, frontendBaseUrl);

        DashboardPage dashboardPage = loginUtils.registerAndOpenDashboard(email);
        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard should load after registration.");

        dashboardPage.logout();
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should return user to login page.");

        Assert.assertTrue(loginUtils.loginAndOpenDashboard(email).isLoaded(), "Valid credentials should open dashboard.");
    }

    @Test(priority = 6)
    public void logoutShouldClearAuthSessionAndProtectPrivateRoutes() {
        startBackendIfNeeded();
        String email = "selenium.logout." + Instant.now().toEpochMilli() + "@example.com";
        LoginTestUtils loginUtils = new LoginTestUtils(driver, wait, frontendBaseUrl);

        loginUtils.registerAndOpenDashboard(email);

        Assert.assertFalse(loginUtils.localStorageValue("token").isBlank(), "Token should exist before logout.");
        Assert.assertFalse(loginUtils.localStorageValue("user").isBlank(), "User details should exist before logout.");

        new DashboardPage(driver, wait).logout();

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Logout should redirect to login page.");
        Assert.assertEquals(loginUtils.localStorageValue("token"), "", "Logout should remove JWT token from local storage.");
        Assert.assertEquals(loginUtils.localStorageValue("user"), "", "Logout should remove user details from local storage.");

        driver.get(frontendBaseUrl + "/dashboard");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/login"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout, direct dashboard access should redirect back to login.");
    }

    @Test(priority = 7)
    public void validSessionShouldRemainAvailableAfterRefreshAndDirectNavigation() {
        startBackendIfNeeded();
        String email = "selenium.session." + Instant.now().toEpochMilli() + "@example.com";
        LoginTestUtils loginUtils = new LoginTestUtils(driver, wait, frontendBaseUrl);

        loginUtils.registerAndOpenDashboard(email);

        String tokenBeforeRefresh = loginUtils.localStorageValue("token");
        Assert.assertFalse(tokenBeforeRefresh.isBlank(), "Token should be stored after successful registration.");

        driver.navigate().refresh();
        Assert.assertTrue(new DashboardPage(driver, wait).waitUntilLoaded().isLoaded(),
                "Authenticated dashboard session should survive page refresh.");
        Assert.assertEquals(loginUtils.localStorageValue("token"), tokenBeforeRefresh,
                "Refresh should not clear or replace the current auth token.");

        driver.get(frontendBaseUrl + "/products");
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("/products"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/products"),
                "Authenticated user should access protected product page by direct URL.");
        Assert.assertEquals(loginUtils.localStorageValue("token"), tokenBeforeRefresh,
                "Direct navigation within protected pages should keep the same auth token.");
    }
}
