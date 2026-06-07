package com.ecommerce.selenium.tests;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.DashboardPage;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.pages.NotificationPopup;
import com.ecommerce.selenium.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Instant;

public class AlertAndPopupTest extends BaseTest {

    private static final String TEST_USER_NAME = "Popup Test User";
    private static final String TEST_USER_PASSWORD = "Password@123";

    @Test(priority = 1)
    public void invalidLoginShouldShowStyledErrorPopup() {
        startBackendIfNeeded();

        new LoginPage(driver, wait, frontendBaseUrl)
                .open()
                .loginAs("missing.user." + Instant.now().toEpochMilli() + "@example.com", "wrong-password");

        String message = new NotificationPopup(driver, wait).visibleMessage();
        Assert.assertTrue(message.contains("Incorrect email or password"), "Invalid login popup should be visible.");
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"), "Invalid login should keep user on login page.");
    }

    @Test(priority = 2)
    public void successfulRegistrationShouldShowStyledSuccessPopup() {
        startBackendIfNeeded();

        new RegisterPage(driver, wait, frontendBaseUrl)
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail("popup.user." + Instant.now().toEpochMilli() + "@example.com")
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        String message = new NotificationPopup(driver, wait).visibleMessage();
        Assert.assertTrue(message.contains("Account created successfully"), "Registration success popup should be visible.");
    }

    @Test(priority = 3)
    public void logoutShouldShowStyledInfoPopup() {
        startBackendIfNeeded();
        String email = "popup.logout." + Instant.now().toEpochMilli() + "@example.com";

        new RegisterPage(driver, wait, frontendBaseUrl)
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        new DashboardPage(driver, wait).waitUntilLoaded().logout();
        String message = new NotificationPopup(driver, wait).visibleMessage();
        Assert.assertTrue(message.contains("Logged out successfully"), "Logout popup should be visible.");
    }
}
