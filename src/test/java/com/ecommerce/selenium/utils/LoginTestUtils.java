package com.ecommerce.selenium.utils;

import com.ecommerce.selenium.base.BaseTest;
import com.ecommerce.selenium.pages.DashboardPage;
import com.ecommerce.selenium.pages.LoginPage;
import com.ecommerce.selenium.pages.RegisterPage;
import java.time.Instant;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTestUtils extends BaseTest {

    public static final String TEST_USER_NAME = "Selenium Login User";
    public static final String TEST_USER_PASSWORD = "Password@123";

    private WebDriver testDriver;
    private WebDriverWait testWait;
    private String testFrontendBaseUrl;

    public LoginTestUtils() {
    }

    public LoginTestUtils(WebDriver driver, WebDriverWait wait, String frontendBaseUrl) {
        this.testDriver = driver;
        this.testWait = wait;
        this.testFrontendBaseUrl = frontendBaseUrl;
    }

    @Test
    public void loginUtilityShouldRegisterLoginAndLogoutUser() {
        startBackendIfNeeded();
        String email = "selenium.utility." + Instant.now().toEpochMilli() + "@example.com";

        DashboardPage dashboardPage = registerAndOpenDashboard(email);
        Assert.assertTrue(dashboardPage.isLoaded(), "Dashboard should load after registration.");

        dashboardPage.logout();
        Assert.assertTrue(activeDriver().getCurrentUrl().contains("/login"), "Logout should return user to login page.");

        Assert.assertTrue(loginAndOpenDashboard(email).isLoaded(), "Utility login should open dashboard.");
    }

    public DashboardPage registerAndOpenDashboard(String email) {
        new RegisterPage(activeDriver(), activeWait(), activeFrontendBaseUrl())
                .open()
                .enterFullName(TEST_USER_NAME)
                .enterEmail(email)
                .enterPassword(TEST_USER_PASSWORD)
                .submit()
                .waitForDashboardRedirect();

        return new DashboardPage(activeDriver(), activeWait()).waitUntilLoaded();
    }

    public DashboardPage loginAndOpenDashboard(String email) {
        new LoginPage(activeDriver(), activeWait(), activeFrontendBaseUrl())
                .open()
                .loginAs(email, TEST_USER_PASSWORD)
                .waitForDashboardRedirect();

        return new DashboardPage(activeDriver(), activeWait()).waitUntilLoaded();
    }

    public String localStorageValue(String key) {
        Object value = ((JavascriptExecutor) activeDriver()).executeScript(
                "return window.localStorage.getItem(arguments[0]) || '';", key);
        return String.valueOf(value);
    }

    private WebDriver activeDriver() {
        return testDriver != null ? testDriver : driver;
    }

    private WebDriverWait activeWait() {
        return testWait != null ? testWait : wait;
    }

    private String activeFrontendBaseUrl() {
        return testFrontendBaseUrl != null ? testFrontendBaseUrl : frontendBaseUrl;
    }
}
